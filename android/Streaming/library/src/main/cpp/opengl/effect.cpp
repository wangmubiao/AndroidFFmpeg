//
// Created by CaoWu15 on 2017/6/1.
//

#include <GLES2/gl2ext.h>
#include "effect.h"

auto effectVertex = "attribute vec4 position;\n"
        "attribute vec4 inputTextureCoordinate;\n"
        "varying vec2 textureCoordinate;\n"
        "uniform mat4 textureTransform;\n"
        "void main() {\n"
        "   textureCoordinate = (textureTransform * inputTextureCoordinate).xy;\n"
        "   gl_Position = position;\n"
        "}\n";

auto effectFragment = "#extension GL_OES_EGL_image_external : require\n"
        "precision mediump float;\n"
        "varying mediump vec2 textureCoordinate;\n"
        "uniform samplerExternalOES inputImageTexture;\n"
        "void main() {\n"
        "   gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
        "}\n";

const GLfloat effectVertexBuffer[] = {
        -1, -1, 0,
        1, -1, 0,
        -1, 1, 0,
        1, 1, 0};

const GLfloat effectTextureCoordinateBuffer[] = {
        0, 0,
        1, 0,
        0, 1,
        1, 1};

wlanjie::Effect::Effect() {
    util = new Util();
    memtransfer = new Memtransfer();
}

wlanjie::Effect::~Effect() {
    delete util;
    delete memtransfer;
}

void wlanjie::Effect::init(int width, int height) {
    glGenFramebuffers(1, &frameBufferId);
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
    glActiveTexture(GL_TEXTURE0);
    outputTextureId = memtransfer->prepareOutput(width, height);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outputTextureId, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void wlanjie::Effect::draw() {
    glUseProgram(programId);
    GLint uniform = glGetUniformLocation(programId, "inputImageTexture");
    glUniform1i(uniform, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);

    GLint position = glGetAttribLocation(programId, "position");
    glEnableVertexAttribArray((GLuint) position);
    glVertexAttribPointer((GLuint) position, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GL_FLOAT), effectVertexBuffer);
    GLint inputTextureCoordinate = glGetAttribLocation(programId, "inputTextureCoordinate");
    glEnableVertexAttribArray((GLuint) inputTextureCoordinate);
    glVertexAttribPointer(inputTextureCoordinate, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GL_FLOAT), effectTextureCoordinateBuffer);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(position);
    glDisableVertexAttribArray(inputTextureCoordinate);

    glBindTexture(GL_TEXTURE_EXTERNAL_OES, inputTextureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void wlanjie::Effect::attachShaderSource(const char *vertexSource, const char *fragmentSource) {
    GLuint vertexShader = util->createShader(GL_VERTEX_SHADER, vertexSource == NULL ? effectVertex : vertexSource);
    GLuint fragmentShader = util->createShader(GL_FRAGMENT_SHADER, fragmentSource == NULL ? effectFragment : fragmentSource);

    programId = glCreateProgram();
    if (programId == 0) {
        return;
    }
    glAttachShader(programId, vertexShader);
    glAttachShader(programId, fragmentShader);
    glLinkProgram(programId);
}

GLuint wlanjie::Effect::getOutputTextureId() {
    return outputTextureId;
}

void wlanjie::Effect::setInputTextureId(GLuint textureId) {
    this->inputTextureId = textureId;
}

void wlanjie::Effect::relase() {
    glDeleteProgram(programId);
    glDeleteFramebuffers(1, &frameBufferId);
    if (memtransfer) {
        memtransfer->releaseOutput();
    }
}
