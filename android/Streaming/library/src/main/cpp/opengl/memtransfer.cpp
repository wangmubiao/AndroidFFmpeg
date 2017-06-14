//
// Created by wlanjie on 2017/5/30.
//

#include "memtransfer.h"
#include "../log.h"

#include <dlfcn.h>
#include <malloc.h>
#include <string.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define ANDROID_GRAPHIC_BUFFER_SIZE 1024


enum {
    /* buffer is never read in software */
            GRALLOC_USAGE_SW_READ_NEVER   = 0x00000000,
    /* buffer is rarely read in software */
            GRALLOC_USAGE_SW_READ_RARELY  = 0x00000002,
    /* buffer is often read in software */
            GRALLOC_USAGE_SW_READ_OFTEN   = 0x00000003,
    /* mask for the software read values */
            GRALLOC_USAGE_SW_READ_MASK    = 0x0000000F,

    /* buffer is never written in software */
            GRALLOC_USAGE_SW_WRITE_NEVER  = 0x00000000,
    /* buffer is never written in software */
            GRALLOC_USAGE_SW_WRITE_RARELY = 0x00000020,
    /* buffer is never written in software */
            GRALLOC_USAGE_SW_WRITE_OFTEN  = 0x00000030,
    /* mask for the software write values */
            GRALLOC_USAGE_SW_WRITE_MASK   = 0x000000F0,

    /* buffer will be used as an OpenGL ES texture */
            GRALLOC_USAGE_HW_TEXTURE      = 0x00000100,
    /* buffer will be used as an OpenGL ES render target */
            GRALLOC_USAGE_HW_RENDER       = 0x00000200,
    /* buffer will be used by the 2D hardware blitter */
            GRALLOC_USAGE_HW_2D           = 0x00000400,
    /* buffer will be used with the framebuffer device */
            GRALLOC_USAGE_HW_FB           = 0x00001000,
    /* mask for the software usage bit-mask */
            GRALLOC_USAGE_HW_MASK         = 0x00001F00,
};

enum {
    HAL_PIXEL_FORMAT_RGBA_8888          = 1,
    HAL_PIXEL_FORMAT_RGBX_8888          = 2,
    HAL_PIXEL_FORMAT_RGB_888            = 3,
    HAL_PIXEL_FORMAT_RGB_565            = 4,
    HAL_PIXEL_FORMAT_BGRA_8888          = 5,
    HAL_PIXEL_FORMAT_RGBA_5551          = 6,
    HAL_PIXEL_FORMAT_RGBA_4444          = 7,
};

#define DL_FUNC(hndl, fn, type) (type)dlsym(hndl, fn)
#define DL_FUNC_CHECK(hndl, fn_ptr, fn) if (!fn_ptr) { LOGE("could not dynamically link func '%s': %s", fn, dlerror()); dlclose(hndl); return false; }

wlanjie::GraphicBufferFnCtor wlanjie::Memtransfer::graBufCreate = NULL;
wlanjie::GraphicBufferFnDtor wlanjie::Memtransfer::graBufDestroy = NULL;
wlanjie::GraphicBufferFnGetNativeBuffer wlanjie::Memtransfer::graBufGetNativeBuffer = NULL;
wlanjie::GraphicBufferFnLock wlanjie::Memtransfer::graBufLock = NULL;
wlanjie::GraphicBufferFnUnlock wlanjie::Memtransfer::graBufUnlock = NULL;

wlanjie::EGLExtFnCreateImage wlanjie::Memtransfer::imageKHRCreate = NULL;
wlanjie::EGLExtFnDestroyImage wlanjie::Memtransfer::imageKHRDestroy = NULL;

wlanjie::Memtransfer::Memtransfer() {
}

wlanjie::Memtransfer::~Memtransfer() {
    releaseInput();
    releaseOutput();
}

bool wlanjie::Memtransfer::init() {
    void *dlEGLhndl = dlopen("libEGL.so", RTLD_LAZY);
    if (!dlEGLhndl) {
        return false;
    }

    imageKHRCreate = DL_FUNC(dlEGLhndl, "eglCreateImageKHR", EGLExtFnCreateImage);
    DL_FUNC_CHECK(dlEGLhndl, imageKHRCreate, "eglCreateImageKHR");

    imageKHRDestroy = DL_FUNC(dlEGLhndl, "eglDestroyImageKHR", EGLExtFnDestroyImage);
    DL_FUNC_CHECK(dlEGLhndl, imageKHRDestroy, "eglDestroyImageKHR");

    dlclose(dlEGLhndl);

    // load necessary Android GraphicBuffer functions
    void *dlUIhndl = dlopen("libui.so", RTLD_LAZY);
    if (!dlUIhndl) {
        return false;
    }

    graBufCreate = DL_FUNC(dlUIhndl, "_ZN7android13GraphicBufferC1Ejjij", GraphicBufferFnCtor);
    DL_FUNC_CHECK(dlUIhndl, graBufCreate, "_ZN7android13GraphicBufferC1Ejjij");

    graBufDestroy = DL_FUNC(dlUIhndl, "_ZN7android13GraphicBufferD1Ev", GraphicBufferFnDtor);
    DL_FUNC_CHECK(dlUIhndl, graBufDestroy, "_ZN7android13GraphicBufferD1Ev");

    graBufGetNativeBuffer = DL_FUNC(dlUIhndl, "_ZNK7android13GraphicBuffer15getNativeBufferEv", GraphicBufferFnGetNativeBuffer);
    DL_FUNC_CHECK(dlUIhndl, graBufGetNativeBuffer, "_ZNK7android13GraphicBuffer15getNativeBufferEv");

    graBufLock = DL_FUNC(dlUIhndl, "_ZN7android13GraphicBuffer4lockEjPPv", GraphicBufferFnLock);
    DL_FUNC_CHECK(dlUIhndl, graBufLock, "_ZN7android13GraphicBuffer4lockEjPPv");

    graBufUnlock = DL_FUNC(dlUIhndl, "_ZN7android13GraphicBuffer6unlockEv", GraphicBufferFnUnlock);
    DL_FUNC_CHECK(dlUIhndl, graBufUnlock, "_ZN7android13GraphicBuffer6unlockEv");

    dlclose(dlUIhndl);
    return true;
}

GLuint wlanjie::Memtransfer::prepareInput(int width, int height, GLenum inputPxFormat,
                                          void *inputDataPtr) {
    if (width == inputWidth && height == inputHeight) {
        return inputTextureId;
    }
    inputWidth = width;
    inputHeight = height;

    glGenTextures(1, &inputTextureId);
    // handle input pixel format
    int nativePxFmt = HAL_PIXEL_FORMAT_RGBA_8888;
    if (inputPixelFormat != GL_RGBA) {
        LOGE("warning: only GL_RGBA is valid as input pixel format");
    }

    // create graphic buffer
    inputGraBufHndl = malloc(ANDROID_GRAPHIC_BUFFER_SIZE);
    graBufCreate(inputGraBufHndl, inputWidth, inputHeight, nativePxFmt,
                 GRALLOC_USAGE_HW_TEXTURE | GRALLOC_USAGE_SW_WRITE_OFTEN);  // is used as OpenGL texture and will be written often

    // get window buffer
    inputNativeBuf = (struct ANativeWindowBuffer *)graBufGetNativeBuffer(inputGraBufHndl);

    if (!inputNativeBuf) {
        LOGE("error getting native window buffer for input");
        return 0;
    }

    // create image for reading back the results
    EGLint eglImgAttrs[] = { EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE, EGL_NONE };
    inputImage = imageKHRCreate(eglGetDisplay(EGL_DEFAULT_DISPLAY),
                                EGL_NO_CONTEXT,
                                EGL_NATIVE_BUFFER_ANDROID,
                                (EGLClientBuffer)inputNativeBuf,
                                eglImgAttrs);	// or NULL as last param?

    if (!inputImage) {
        LOGE("error creating image KHR for input");
        return 0;
    }
    return inputTextureId;
}

GLuint wlanjie::Memtransfer::prepareOutput(int width, int height, int textureId) {
    if (outputWidth == width && outputHeight == height) {
        return 0;
    }
    outputWidth = width;
    outputHeight = height;

//    glViewport(0, 0, width, height);
    glBindTexture(GL_TEXTURE_2D, (GLuint) textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    outputGraBufHndl = malloc(ANDROID_GRAPHIC_BUFFER_SIZE);
    graBufCreate(outputGraBufHndl, (uint32_t) width, (uint32_t) height, HAL_PIXEL_FORMAT_RGBA_8888, GRALLOC_USAGE_HW_RENDER | GRALLOC_USAGE_SW_READ_OFTEN);
    outputNativeBuf = (struct ANativeWindowBuffer *) graBufGetNativeBuffer(outputGraBufHndl);
    EGLint eglImgAttrs[] = { EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE, EGL_NONE };
    outputImage = imageKHRCreate(eglGetDisplay(EGL_DEFAULT_DISPLAY),
                                 EGL_NO_CONTEXT,
                                 EGL_NATIVE_BUFFER_ANDROID,
                                 (EGLClientBuffer)outputNativeBuf,
                                 eglImgAttrs);	// or NULL as last param?

    return 0;
}

void wlanjie::Memtransfer::releaseInput() {
    if (inputImage) {
        imageKHRDestroy(EGL_DEFAULT_DISPLAY, inputImage);
        free(inputImage);
        inputImage = NULL;
    }

    if (inputGraBufHndl) {
        graBufDestroy(inputGraBufHndl);
        free(inputGraBufHndl);
        inputGraBufHndl = NULL;
        inputNativeBuf = NULL;
    }
}

void wlanjie::Memtransfer::releaseOutput() {
    if (outputImage) {
        imageKHRDestroy(EGL_DEFAULT_DISPLAY, outputImage);
        free(outputImage);
        outputImage = NULL;
    }
    if (outputGraBufHndl) {
        graBufDestroy(outputGraBufHndl);
        free(outputGraBufHndl);
        outputGraBufHndl = NULL;
        outputNativeBuf = NULL;
    }
}

void wlanjie::Memtransfer::toGPU(const unsigned char *buf) {
    // bind the input texture
    glBindTexture(GL_TEXTURE_2D, inputTextureId);

    // activate the image KHR for the input
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, inputImage);

//    Tools::checkGLErr("MemTransferAndroid", "call to glEGLImageTargetTexture2DOES() for input");

    // lock the graphics buffer at graphicsPtr
    unsigned char *graphicsPtr = (unsigned char *)lockBufferAndGetPtr(BUF_TYPE_INPUT);

    // copy whole image from "buf" to "graphicsPtr"
    memcpy(graphicsPtr, buf, (size_t) (inputWidth * inputHeight * 4));

    // unlock the graphics buffer again
    unlockBuffer(BUF_TYPE_INPUT);
}

void wlanjie::Memtransfer::fromGPU(unsigned char *buf, int textureId) {
    // bind the output texture
    glBindTexture(GL_TEXTURE_2D, textureId);

    // activate the image KHR for the output
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, outputImage);

//    Tools::checkGLErr("MemTransferAndroid", "call to glEGLImageTargetTexture2DOES() for output");

    // lock the graphics buffer at graphicsPtr
    const unsigned char *graphicsPtr = (const unsigned char *)lockBufferAndGetPtr(BUF_TYPE_OUTPUT);

    // copy whole image from "graphicsPtr" to "buf"
    memcpy(buf, graphicsPtr, (size_t) (outputWidth * outputHeight * 4));

    // unlock the graphics buffer again
    unlockBuffer(BUF_TYPE_OUTPUT);
}

void *wlanjie::Memtransfer::lockBufferAndGetPtr(wlanjie::BufType bufType) {
    void *hndl;
    int usage;
    unsigned char *memPtr;

    if (bufType == BUF_TYPE_INPUT) {
        hndl = inputGraBufHndl;
        usage = GRALLOC_USAGE_SW_WRITE_OFTEN;
    } else {
        hndl = outputGraBufHndl;
        usage = GRALLOC_USAGE_SW_READ_OFTEN;
    }

    // lock and get pointer
    graBufLock(hndl, usage, &memPtr);

    // check for valid pointer
    if (!memPtr) {
        LOGE("GraphicBuffer lock returned invalid pointer");
    }
    return (void *)memPtr;
}

void wlanjie::Memtransfer::unlockBuffer(wlanjie::BufType bufType) {
    void *hndl = (bufType == BUF_TYPE_INPUT) ? inputGraBufHndl : outputGraBufHndl;
    graBufUnlock(hndl);
}