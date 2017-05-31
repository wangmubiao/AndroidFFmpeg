//
// Created by wlanjie on 2017/5/30.
//

#ifndef STREAMING_MEMTRANSFER_H
#define STREAMING_MEMTRANSFER_H

#include <EGL/eglext.h>
#include <GLES2/gl2.h>

namespace wlanjie {

    /**
     * Buffer type (input/output) definition.
     */
    typedef enum {
        BUF_TYPE_INPUT = 0,
        BUF_TYPE_OUTPUT
    } BufType;

    // constructor
    typedef void (*GraphicBufferFnCtor)(void *graphicBufHndl, uint32_t w, uint32_t h,
                                        uint32_t format, uint32_t usage);

    // deconstructor
    typedef void (*GraphicBufferFnDtor)(void *graphicBufHndl);

    // getNativeBuffer
    typedef void *(*GraphicBufferFnGetNativeBuffer)(void *graphicBufHndl);

    // lock
    typedef int (*GraphicBufferFnLock)(void *graphicBufHndl, uint32_t usage, unsigned char **addr);

    // unlock
    typedef int (*GraphicBufferFnUnlock)(void *graphicBufHndl);

    /**
     * typedefs to EGL extension functions for ImageKHR extension
     */

    // create ImageKHR
    typedef EGLImageKHR (*EGLExtFnCreateImage)(EGLDisplay dpy, EGLContext ctx, EGLenum target,
                                               EGLClientBuffer buffer, const EGLint *attribList);

    // destroy ImageKHR
    typedef EGLBoolean (*EGLExtFnDestroyImage)(EGLDisplay dpy, EGLImageKHR image);


    class Memtransfer {
        virtual Memtransfer();

        virtual ~Memtransfer();

        virtual void init();

        /**
         * Prepare for input frames of size <width>x<height>. Return a texture id for the input frames.
         */
        virtual GLuint  prepareInput(int width, int height, GLenum inputPxFormat = GL_RGBA, void *inputDataPtr = NULL);


        /**
         * Prepare for output frames of size <width>x<height>. Return a texture id for the output frames.
         */
        virtual GLuint prepareOutput(int width, int height);

        /**
         * Delete Input texture.
         */
        virtual void releaseInput();

        /**
         * Delete output texture.
         */
        virtual void releaseOutput();

        /**
          * Map data in <buf> to GPU.
          */
        virtual void toGPU(const unsigned char *buf);

        /**
         * Map data from GPU to <buf>
         */
        virtual void fromGPU(unsigned char *buf);

        /**
         * Lock the input or output buffer and return its base address.
         * The input buffer will be locked for reading AND writing, while the
         * output buffer will be locked for reading only.
         */
        virtual void *lockBufferAndGetPtr(BufType bufType);

        /**
         * Unlock the input or output buffer.
         */
        virtual void unlockBuffer(BufType bufType);

    private:
        static GraphicBufferFnCtor graBufCreate;        // function pointer to GraphicBufferFnCtor
        static GraphicBufferFnDtor graBufDestroy;       // function pointer to GraphicBufferFnDtor
        static GraphicBufferFnGetNativeBuffer graBufGetNativeBuffer;  // function pointer to GraphicBufferFnGetNativeBuffer
        static GraphicBufferFnLock graBufLock;          // function pointer to GraphicBufferFnLock
        static GraphicBufferFnUnlock graBufUnlock;      // function pointer to GraphicBufferFnUnlock

        static EGLExtFnCreateImage  imageKHRCreate;     // function pointer to EGLExtFnCreateImage
        static EGLExtFnDestroyImage  imageKHRDestroy;   // function pointer to EGLExtFnDestroyImage

        void *inputGraBufHndl;      // Android GraphicBuffer handle for input
        void *outputGraBufHndl;     // Android GraphicBuffer handle for output

        struct ANativeWindowBuffer *inputNativeBuf;     // pointer to native window buffer for input (weak ref - do not free()!)
        struct ANativeWindowBuffer *outputNativeBuf;	// pointer to native window buffer for output (weak ref - do not free()!)

        EGLImageKHR inputImage;     // ImageKHR handle for input
        EGLImageKHR outputImage;    // ImageKHR handle for output

        int inputWidth;
        int inputHeight;

        int outputWidth;
        int outputHeight;

        GLuint inputTextureId;
        GLuint outputTextureId;
        GLenum inputPixelFormat;
    };

}

#endif //STREAMING_MEMTRANSFER_H
