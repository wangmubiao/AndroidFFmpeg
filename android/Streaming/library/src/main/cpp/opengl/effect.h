//
// Created by wlanjie on 2017/6/1.
//

#ifndef STREAMING_EFFECT_H
#define STREAMING_EFFECT_H

#include "util.h"
#include "memtransfer.h"

namespace wlanjie {

    class Effect {
    public:

        Effect();

        ~Effect();

        void init(int width, int height);

        GLuint draw(int textureId);

        void attachShaderSource(const char *vertexSource = NULL, const char *fragmentSource = NULL);

        GLuint getOutputTextureId();

        void setInputTextureId(GLuint textureId);

        void setTextureTransformMatrix(GLfloat *textureTransformMatrix);

        unsigned char* getBuffer();

        unsigned char* getBuffer(unsigned char* buffer);

        void release();

    private:
        Util *util;
        Memtransfer *memtransfer;

    private:
        GLuint programId;
        GLuint frameBufferId;
        GLuint outputTextureId;
        GLuint inputTextureId;
        GLuint textureId;
        int width;
        int height;
        GLfloat *textureTransformMatrix;
        unsigned char *buffer;
    };

}

#endif //STREAMING_EFFECT_H
