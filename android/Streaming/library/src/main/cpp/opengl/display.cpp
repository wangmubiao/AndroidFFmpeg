//
// Created by wlanjie on 2017/5/31.
//

#include <GLES2/gl2ext.h>
#include "display.h"
#include "../log.h"

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

GLfloat vertexBuffer[] = {
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
};

GLfloat textureCoordinateBuffer[] = {
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
};

wlanjie::Display::Display() {
    util = new Util();
}

wlanjie::Display::~Display() {
    delete util;
}

void wlanjie::Display::init(int width, int height) {
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

void wlanjie::Display::draw(int textureId) {
    LOGE("display draw");
    glUseProgram(programId);
    GLint position = glGetAttribLocation(programId, "position");
    glEnableVertexAttribArray((GLuint) position);
    glVertexAttribPointer((GLuint) position, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GL_FLOAT), vertexBuffer);

    GLint textureCoordinate = glGetAttribLocation(programId, "inputTextureCoordinate");
    glEnableVertexAttribArray((GLuint) textureCoordinate);
    glVertexAttribPointer((GLuint) textureCoordinate, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GL_FLOAT), textureCoordinateBuffer);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, (GLuint) textureId);

    GLint inputTextureUniform = glGetUniformLocation(programId, "inputImageTexture");
    glUniform1i(inputTextureUniform, 0);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glBindTexture(GL_TEXTURE_2D, 0);

    glDisableVertexAttribArray((GLuint) position);
    glDisableVertexAttribArray((GLuint) textureCoordinate);
}

void wlanjie::Display::release() {

}

void wlanjie::Display::setTextureId(GLuint textureId) {
//    this->textureId = textureId;
}
