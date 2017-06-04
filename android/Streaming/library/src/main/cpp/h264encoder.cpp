//
// Created by wlanjie on 2017/6/4.
//

#include "h264encoder.h"

wlanjie::H264encoder::H264encoder() {

}

wlanjie::H264encoder::~H264encoder() {

}

bool wlanjie::H264encoder::openH264Encoder() {
    WelsCreateSVCEncoder(&encoder);
    return false;
}

void wlanjie::H264encoder::closeH264Encoder() {

}
