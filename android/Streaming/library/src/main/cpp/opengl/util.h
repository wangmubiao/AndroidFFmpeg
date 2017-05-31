//
// Created by wlanjie on 2017/5/31.
//

#ifndef STREAMING_UTIL_H
#define STREAMING_UTIL_H

#include <GLES2/gl2.h>

#define CREATE_SHADER_ERROR -1

namespace wlanjie {

    class Util {
    public:
        GLuint createShader(GLenum type, const char *source);
    };

}

#endif //STREAMING_UTIL_H
