//
// Created by CaoWu15 on 2017/6/1.
//

#include "opengl.h"

wlanjie::OpenGL::OpenGL() {
    effect = new Effect();
    display = new Display();
}

wlanjie::OpenGL::~OpenGL() {
    delete effect;
    delete display;
}

void wlanjie::OpenGL::init(int width, int height) {
    this->width = width;
    this->height = height;
    effect->attachShaderSource();
    display->attachShaderSource();
    effect->init(width, height);
    display->init(width, height);
//    display->setTextureId(effect->getOutputTextureId());
}

int wlanjie::OpenGL::draw(int inputTextureId) {
    GLuint textureId = effect->draw(inputTextureId);
    display->draw(textureId);
    return textureId;
}

void wlanjie::OpenGL::setInputPixels(uint8_t *data) {

}

void wlanjie::OpenGL::setInputTexture(int textureId) {
//    effect->setInputTextureId(textureId);
}

void wlanjie::OpenGL::release() {
    effect->release();
    display->release();
}

void wlanjie::OpenGL::setTextureTransformMatrix(GLfloat *textureTransformMatrix) {
    effect->setTextureTransformMatrix(textureTransformMatrix);
}

unsigned char *wlanjie::OpenGL::getBuffer() {
    return effect->getBuffer();
}

int wlanjie::OpenGL::getWidth() {
    return width;
}

int wlanjie::OpenGL::getHeight() {
    return height;
}
