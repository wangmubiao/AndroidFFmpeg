//
// Created by wlanjie on 2017/5/31.
//

#include <GLES2/gl2ext.h>
#include "display.h"

auto vertex = "attribute vec4 position;\n"
        "attribute vec2 inputTextureCoordinate;\n"
        "varying vec2 textureCoordinate;\n"
        "void main() {\n"
        "   textureCoordinate = inputTextureCoordinate.xy;\n"
        "   gl_Position = position;\n"
        "}\n";

auto fragment = "precision mediump float;\n"
        "varying vec2 textureCoordinate;\n"
        "uniform sampler2D inputImageTexture;\n"
        "void main() {\n"
        "   gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
        "}\n";

wlanjie::Display::Display() {
    util = new Util();
}

wlanjie::Display::~Display() {
    delete util;
}

void wlanjie::Display::init(int width, int height) {
    glActiveTexture(GL_TEXTURE0);
    glGenTextures(1, &textureId);
    if (textureId == 0) {
        return;
    }
    glBindTexture(GL_TEXTURE_2D, textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    // check status
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glGenFramebuffers(1, &frameBufferId);
}

void wlanjie::Display::attachShaderSource(const char *vertexSource, const char *fragmentSource) {
    GLuint vertexShader = util->createShader(GL_VERTEX_SHADER, vertexSource == NULL ? vertex : vertexSource);
    GLuint fragmentShader = util->createShader(GL_FRAGMENT_SHADER, fragmentSource == NULL ? fragment : fragmentSource);

    programId = glCreateProgram();
    if (programId == 0) {
        return;
    }
    glAttachShader(programId, vertexShader);
    glAttachShader(programId, fragmentShader);
    glLinkProgram(programId);
}

void wlanjie::Display::draw() {
//    glActiveTexture(GL_TEXTURE0);
//    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
    glUseProgram(programId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    GLint inputTextureUniform = glGetUniformLocation(programId, "inputImageTexture");
    glUniform1i(inputTextureUniform, 0);

}

void wlanjie::Display::release() {

}
