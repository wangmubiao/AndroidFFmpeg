//
// Created by CaoWu15 on 2017/6/1.
//

#include <GLES2/gl2ext.h>
#include <malloc.h>
#include "effect.h"
#include "../log.h"

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
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
};

const GLfloat effectTextureCoordinateBuffer[] = {
//        0.0f, 1.0f,
//        1.0f, 1.0f,
//        0.0f, 0.0f,
//        1.0f, 0.0f,
        1.0f, 0.04858932f,
        1.0f, 0.95141065f,
        0.0f, 0.04858932f,
        0.0f, 0.95141065f
};

wlanjie::Effect::Effect() {
    util = new Util();
    memtransfer = new Memtransfer();
}

wlanjie::Effect::~Effect() {
    delete util;
    delete memtransfer;
}

void wlanjie::Effect::init(int width, int height) {
    this->width = width;
    this->height = height;

//    glViewport(0, 0, width, height);
    buffer = (unsigned char *) malloc((size_t) (width * height * 4));

    glGenFramebuffers(1, &frameBufferId);
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    memtransfer->init();
    memtransfer->prepareOutput(width, height, textureId);
}

GLuint wlanjie::Effect::draw(int textureId) {
//    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(programId);
    GLint position = glGetAttribLocation(programId, "position");
    glEnableVertexAttribArray((GLuint) position);
    glVertexAttribPointer((GLuint) position, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GL_FLOAT), effectVertexBuffer);

    GLint inputTextureCoordinate = glGetAttribLocation(programId, "inputTextureCoordinate");
    glEnableVertexAttribArray((GLuint) inputTextureCoordinate);
    glVertexAttribPointer((GLuint) inputTextureCoordinate, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GL_FLOAT), effectTextureCoordinateBuffer);

    glActiveTexture(GL_TEXTURE0);
    GLint uniform = glGetUniformLocation(programId, "inputImageTexture");
    glUniform1i(uniform, 0);

    GLint textureTransform = glGetUniformLocation(programId, "textureTransform");
    glUniformMatrix4fv(textureTransform, 1, GL_FALSE, textureTransformMatrix);

    glBindTexture(GL_TEXTURE_EXTERNAL_OES, (GLuint) textureId);
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

    glDisableVertexAttribArray((GLuint) position);
    glDisableVertexAttribArray((GLuint) inputTextureCoordinate);

    return this->textureId;
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

void wlanjie::Effect::release() {
    glDeleteProgram(programId);
    glDeleteFramebuffers(1, &frameBufferId);
    if (memtransfer) {
        memtransfer->releaseOutput();
    }
    free(buffer);
}

void wlanjie::Effect::setTextureTransformMatrix(GLfloat *textureTransformMatrix) {
    this->textureTransformMatrix = textureTransformMatrix;
}

unsigned char *wlanjie::Effect::getBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
    memtransfer->fromGPU(buffer, textureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    return buffer;
}

unsigned char *wlanjie::Effect::getBuffer(unsigned char *buffer) {
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId);
    memtransfer->fromGPU(buffer, textureId);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    return nullptr;
}
