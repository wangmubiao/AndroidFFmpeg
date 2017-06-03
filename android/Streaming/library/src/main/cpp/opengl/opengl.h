//
// Created by wlanjie on 2017/6/1.
//

#ifndef STREAMING_OPENGL_H
#define STREAMING_OPENGL_H

#include <stdint.h>
#include "effect.h"
#include "display.h"

namespace wlanjie {

    class OpenGL {

    public:
        OpenGL();

        ~OpenGL();

        void init(int width, int height);

        int draw(int inputTextureId);

        void setInputPixels(uint8_t *data);

        void setInputTexture(int textureId);

        void setTextureTransformMatrix(GLfloat *textureTransformMatrix);

        unsigned char* getBuffer();

        int getWidth();

        int getHeight();

        void release();

    private:
        Effect *effect;
        Display *display;
        int width;
        int height;
    };

}

#endif //STREAMING_OPENGL_H
