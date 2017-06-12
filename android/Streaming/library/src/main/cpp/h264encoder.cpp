//
// Created by wlanjie on 2017/6/4.
//

#include <wels/codec_app_def.h>
#include <string.h>
#include <cstdint>
#include <time.h>
#include "libyuv.h"
#include "h264encoder.h"
#include "log.h"

extern void logEncode(void *context, int level, const char *message);

void logEncode(void *context, int level, const char *message) {
    __android_log_print(ANDROID_LOG_VERBOSE, "wlanjie", message, 1);
}

wlanjie::H264encoder::H264encoder() {

}

wlanjie::H264encoder::~H264encoder() {

}

bool wlanjie::H264encoder::openH264Encoder() {
    WelsCreateSVCEncoder(&encoder_);
    SEncParamExt encoder_params = createEncoderParams();
    int ret = 0;
    if ((ret = encoder_->InitializeExt(&encoder_params)) != 0) {
        LOGE("initial h264 error = %d", ret);
        return false;
    }

    int level = WELS_LOG_DETAIL;
    encoder_->SetOption(ENCODER_OPTION_TRACE_LEVEL, &level);
    void (*func)(void *, int, const char *) = &logEncode;
    encoder_->SetOption(ENCODER_OPTION_TRACE_CALLBACK, &func);
    int video_format = videoFormatI420;
    encoder_->SetOption(ENCODER_OPTION_DATAFORMAT, &video_format);

    memset(&_sourcePicture, 0, sizeof(Source_Picture_s));

    uint8_t *data_ = NULL;
    _sourcePicture.iPicWidth = frameWidth;
    _sourcePicture.iPicHeight = frameHeight;
    _sourcePicture.iStride[0] = frameWidth;
    _sourcePicture.iStride[1] = _sourcePicture.iStride[2] = frameWidth >> 1;
    data_ = static_cast<uint8_t  *> (realloc(data_, frameWidth * frameHeight * 3 / 2));
    _sourcePicture.pData[0] = data_;
    _sourcePicture.pData[1] = _sourcePicture.pData[0] + frameWidth * frameHeight;
    _sourcePicture.pData[2] = _sourcePicture.pData[1] + (frameWidth * frameHeight >> 2);
    _sourcePicture.iColorFormat = videoFormatI420;

    memset(&info, 0, sizeof(SFrameBSInfo));
    _outputStream.open("/sdcard/wlanjie.h264", std::ios_base::binary | std::ios_base::out);
    return false;
}

void wlanjie::H264encoder::closeH264Encoder() {
    if (encoder_) {
        encoder_->Uninitialize();
        WelsDestroySVCEncoder(encoder_);
        encoder_ = nullptr;
    }
}

SEncParamExt wlanjie::H264encoder::createEncoderParams() const {
    SEncParamExt encoder_params;
    encoder_->GetDefaultParams(&encoder_params);
    encoder_params.iUsageType = CAMERA_VIDEO_REAL_TIME;
    encoder_params.iPicWidth = frameWidth;
    encoder_params.iPicHeight = frameHeight;
    // uses bit/s kbit/s
    encoder_params.iTargetBitrate = 800 * 1000;
    // max bit/s
    encoder_params.iMaxBitrate = 1300 * 1000;
    encoder_params.iRCMode = RC_BITRATE_MODE;
    encoder_params.fMaxFrameRate = 30;
    encoder_params.bEnableFrameSkip = true;
    encoder_params.bEnableDenoise = true;
    encoder_params.uiIntraPeriod = 2;
    encoder_params.uiMaxNalSize = 1500;
    encoder_params.iTemporalLayerNum = 3;
    encoder_params.iSpatialLayerNum = 1;
    encoder_params.bEnableLongTermReference = 0;
    encoder_params.bEnableSceneChangeDetect = 0;
    encoder_params.iMultipleThreadIdc = 4;
    encoder_params.sSpatialLayers[0].iVideoHeight = 640;
    encoder_params.sSpatialLayers[0].iVideoWidth = 360;
    encoder_params.sSpatialLayers[0].fFrameRate = 30;
    encoder_params.sSpatialLayers[0].iSpatialBitrate = 800 * 1000;
//    encoder_params.sSpatialLayers[0].iMaxSpatialBitrate = 0;
    encoder_params.sSpatialLayers[0].sSliceArgument.uiSliceMode = SM_SINGLE_SLICE;
    encoder_params.eSpsPpsIdStrategy = CONSTANT_ID;
    return encoder_params;
}

uint8_t* wlanjie::H264encoder::startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride) {
    return NULL;
}

void wlanjie::H264encoder::setFrameSize(int width, int height) {
    this->frameHeight = height;
    this->frameWidth = width;
}

int wlanjie::H264encoder::getEncoderImageLength() {
    return encoded_image_length;
}

uint8_t *wlanjie::H264encoder::encoder(char *rgba, long pts) {
    if (present_time_us == 0) {
        present_time_us = time(NULL) / 1000;
    }
    LOGE("%d", time(NULL));
//    _sourcePicture.uiTimeStamp = time(NULL) / 1000 - present_time_us;
    _sourcePicture.uiTimeStamp = pts;
    libyuv::ABGRToI420((const uint8 *) rgba, frameWidth * 4,
                       _sourcePicture.pData[0], _sourcePicture.iStride[0],
                       _sourcePicture.pData[1], _sourcePicture.iStride[1],
                       _sourcePicture.pData[2], _sourcePicture.iStride[2],
                       frameWidth, frameHeight);

    int ret = encoder_->EncodeFrame(&_sourcePicture, &info);
    if (!ret) {
        if (info.eFrameType != videoFrameTypeSkip) {
            int len = 0;
            for (int layer = 0; layer < info.iLayerNum; ++layer) {
                const SLayerBSInfo &layerInfo = info.sLayerInfo[layer];
                for (int nal = 0; nal < layerInfo.iNalCount; ++nal) {
                    len += layerInfo.pNalLengthInByte[nal];
                }
            }
            uint8_t *encoded_image_buffer = new uint8_t[1024 * 1024];
            encoded_image_length = len;
            int image_length = 0;
            for (int layer = 0; layer < info.iLayerNum; ++layer) {
                SLayerBSInfo layerInfo = info.sLayerInfo[layer];
                int layerSize = 0;
                for (int nal = 0; nal < layerInfo.iNalCount; ++nal) {
                    layerSize += layerInfo.pNalLengthInByte[nal];
                }
                _outputStream.write((const char *) layerInfo.pBsBuf, layerSize);
                memcpy(encoded_image_buffer + image_length, layerInfo.pBsBuf, layerSize);
                image_length += layerSize;
            }
            return encoded_image_buffer;
        }
    }
    return nullptr;
}
