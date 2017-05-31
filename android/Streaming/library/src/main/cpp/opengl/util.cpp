//
// Created by wlanjie on 2017/5/31.
//

#include "util.h"

GLuint wlanjie::Util::createShader(GLenum type, const char *source) {
    GLuint shader = glCreateShader(type);
    if (shader == 0) {
        return (GLuint) CREATE_SHADER_ERROR;
    }
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    return shader;
}
