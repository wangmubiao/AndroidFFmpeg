//
// Created by wlanjie on 2017/6/4.
//

#include <wels/codec_app_def.h>
#include <string.h>
#include <cstdint>
#include "h264encoder.h"
#include "log.h"

wlanjie::H264encoder::H264encoder() {

}

wlanjie::H264encoder::~H264encoder() {

}

bool wlanjie::H264encoder::openH264Encoder() {
    WelsCreateSVCEncoder(&encoder);
    SEncParamExt encoder_params = createEncoderParams();
    if (encoder->InitializeExt(&encoder_params) != 0) {
        LOGE("initial h264 error");
        return false;
    }

    return false;
}

void wlanjie::H264encoder::closeH264Encoder() {
    if (encoder) {
        encoder->Uninitialize();
        WelsDestroySVCEncoder(encoder);
        encoder = nullptr;
    }
}

SEncParamExt wlanjie::H264encoder::createEncoderParams() const {
    SEncParamExt encoder_params;
    encoder->GetDefaultParams(&encoder_params);
    encoder_params.iUsageType = CAMERA_VIDEO_REAL_TIME;
    encoder_params.iPicWidth = frameWidth;
    encoder_params.iPicHeight = frameHeight;
    // uses bit/s kbit/s
    encoder_params.iTargetBitrate = 800 * 1000;
    // max bit/s
    encoder_params.iMaxBitrate = 1300 * 1000;
    encoder_params.iRCMode = RC_BITRATE_MODE;
    //TODO
    encoder_params.fMaxFrameRate = 24;
    //TODO
    encoder_params.bEnableFrameSkip = true;
    //TODO
    encoder_params.uiIntraPeriod = 2;
    encoder_params.uiMaxNalSize = 0;
    encoder_params.iMultipleThreadIdc = 1;
    encoder_params.sSpatialLayers[0].iVideoHeight = 640;
    encoder_params.sSpatialLayers[0].iVideoWidth = 480;
    encoder_params.sSpatialLayers[0].fFrameRate = 24;
    encoder_params.sSpatialLayers[0].iSpatialBitrate = 0;
    encoder_params.sSpatialLayers[0].iMaxSpatialBitrate = 0;
//    encoder_params.sSpatialLayers[0].sSliceArgument.uiSliceMode = SM_SINGLE_SLICE;
    encoder_params.eSpsPpsIdStrategy = CONSTANT_ID;
    return encoder_params;
}

void wlanjie::H264encoder::startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride) {
    int trace_level = WELS_LOG_DETAIL;
    encoder->SetOption(ENCODER_OPTION_TRACE_LEVEL, &trace_level);
    int video_format = videoFormatI420;
    encoder->SetOption(ENCODER_OPTION_DATAFORMAT, &video_format);
    SSourcePicture picture;
    memset(&picture, 0, sizeof(SSourcePicture));
    picture.iPicWidth = frameWidth;
    picture.iPicHeight = frameHeight;
    picture.iColorFormat = videoFormatI420;
    picture.uiTimeStamp = 0;
    picture.iStride[0] = yStride; // y
    picture.iStride[1] = uStride; // u
    picture.iStride[2] = vStride; // v
    picture.pData[0] = yData; // y
    picture.pData[1] = uData; // u
    picture.pData[2] = vData; // v

    SFrameBSInfo info;
    memset(&info, 0, sizeof(SFrameBSInfo));
    encoder->EncodeFrame(&picture, &info);
}

void wlanjie::H264encoder::setFrameSize(int width, int height) {
    this->frameHeight = height;
    this->frameWidth = width;
}
