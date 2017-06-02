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
    effect->init(width, height);
    display->init(width, height);
    display->setTextureId(effect->getOutputTextureId());
}

void wlanjie::OpenGL::draw() {
    effect->draw();
    display->draw();
}

void wlanjie::OpenGL::setInputPixels(uint8_t *data) {

}

void wlanjie::OpenGL::setInputTexture(int textureId) {
    effect->setInputTextureId(textureId);
}

void wlanjie::OpenGL::release() {
    effect->relase();
    display->release();
}