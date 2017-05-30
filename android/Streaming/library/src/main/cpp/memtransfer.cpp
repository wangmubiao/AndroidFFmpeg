//
// Created by wlanjie on 2017/5/30.
//

#include "memtransfer.h"
#include "log.h"

#include <dlfcn.h>
#include <malloc.h>

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
#define DL_FUNC_CHECK(hndl, fn_ptr, fn) if (!fn_ptr) { LOGE("could not dynamically link func '%s': %s", fn, dlerror()); dlclose(hndl); }

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

void wlanjie::Memtransfer::init() {
    void *dlEGLHndl = dlopen("libEGL.so", RTLD_LAZY);
    if (!dlEGLHndl) {
        return;
    }
    imageKHRCreate = DL_FUNC(dlEGLHndl, "eglCreateImageKHR", EGLExtFnCreateImage);
    DL_FUNC_CHECK(dlEGLHndl, imageKHRCreate, "eglCreateImageKHR");

    imageKHRDestroy= DL_FUNC(dlEGLHndl, "eglDestroyImageKHR", EGLExtFnDestroyImage);
    DL_FUNC_CHECK(dlEGLHndl, imageKHRDestroy, "eglDestroyImageKHR");

    dlclose(dlEGLHndl);

    void *dlUIHndl = dlopen("libui.so", RTLD_LAZY);
    if (!dlUIHndl) {
        return;
    }
    graBufCreate = DL_FUNC(dlUIHndl, "_ZN7android13GraphicBufferC1Ejjij", GraphicBufferFnCtor);
    DL_FUNC_CHECK(dlUIHndl, graBufCreate, "_ZN7android13GraphicBufferC1Ejjij");
    graBufDestroy = DL_FUNC(dlUIHndl, "_ZN7android13GraphicBufferD1Ev", GraphicBufferFnDtor);
    DL_FUNC_CHECK(dlUIHndl, graBufDestroy, "_ZN7android13GraphicBufferD1Ev");
    graBufGetNativeBuffer = DL_FUNC(dlUIHndl, "_ZNK7android13GraphicBuffer15getNativeBufferEv", GraphicBufferFnGetNativeBuffer);
    DL_FUNC_CHECK(dlUIHndl, graBufGetNativeBuffer, "_ZNK7android13GraphicBuffer15getNativeBufferEv");
    graBufLock = DL_FUNC(dlUIHndl, "_ZN7android13GraphicBuffer4lockEjPPv", GraphicBufferFnLock);
    DL_FUNC_CHECK(dlUIHndl, graBufLock, "_ZN7android13GraphicBuffer4lockEjPPv");
    graBufUnlock = DL_FUNC(dlUIHndl, "_ZN7android13GraphicBuffer6unlockEv", GraphicBufferFnUnlock);
    DL_FUNC_CHECK(dlUIHndl, graBufUnlock, "_ZN7android13GraphicBuffer6unlockEv");
    dlclose(dlUIHndl);
}

GLuint wlanjie::Memtransfer::prepareInput(int width, int height, GLenum inputPxFormat,
                                          void *inputDataPtr) {
    return 0;
}

GLuint wlanjie::Memtransfer::prepareOutput(int width, int height) {
    if (outputWidth == width && outputHeight == height) {
        return outputTextureId;
    }
    outputWidth = width;
    outputHeight = height;

    glGenTextures(1, &outputTextureId);
    if (outputTextureId <= 0) {
        return 0;
    }
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    outputGraBufHndl = malloc(ANDROID_GRAPHIC_BUFFER_SIZE);
    graBufCreate(outputGraBufHndl, (uint32_t) width, (uint32_t) height, HAL_PIXEL_FORMAT_RGB_888, GRALLOC_USAGE_HW_RENDER | GRALLOC_USAGE_SW_READ_OFTEN);
    outputNativeBuf = (struct ANativeWindowBuffer *) graBufGetNativeBuffer(outputGraBufHndl);
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

}

void wlanjie::Memtransfer::fromGPU(unsigned char *buf) {

}

void *wlanjie::Memtransfer::lockBufferAndGetPtr(wlanjie::BufType bufType) {
    return nullptr;
}

void wlanjie::Memtransfer::unlockBuffer(wlanjie::BufType bufType) {

}