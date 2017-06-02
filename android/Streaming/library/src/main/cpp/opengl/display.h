//
// Created by wlanjie on 2017/5/31.
//

#ifndef STREAMING_DISPLAY_H
#define STREAMING_DISPLAY_H

#include "util.h"

namespace wlanjie {

    class Display {

    public:
        Display();

        ~Display();

    public:
        virtual void init(int width, int height);

        virtual void attachShaderSource(const char *vertexSource = NULL, const char *fragmentSource = NULL);

        virtual void setTextureId(GLuint textureId);

        virtual void draw();

        virtual void release();

    private:
        Util *util;

    private:
        GLuint programId;
        GLuint textureId;
        GLuint frameBufferId;
    };

}

#endif //STREAMING_DISPLAY_H
