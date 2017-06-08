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
//    SEncParamBase encoder_params;
//    memset (&encoder_params, 0, sizeof (SEncParamBase));
//    encoder_params.iUsageType = CAMERA_VIDEO_REAL_TIME;
//    encoder_params.fMaxFrameRate = 24;
//    encoder_params.iPicWidth = frameWidth;
//    encoder_params.iPicHeight = frameHeight;
//    encoder_params.iTargetBitrate = 5000000;
    int ret = 0;
    if ((ret = encoder->InitializeExt(&encoder_params)) != 0) {
        LOGE("initial h264 error = %d", ret);
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
//    SEncParamExt encoder_params;
//    encoder->GetDefaultParams(&encoder_params);
//    encoder_params.iUsageType = CAMERA_VIDEO_REAL_TIME;
//    encoder_params.iPicWidth = frameWidth;
//    encoder_params.iPicHeight = frameHeight;
//    // uses bit/s kbit/s
//    encoder_params.iTargetBitrate = 800 * 1000;
//    // max bit/s
////    encoder_params.iMaxBitrate = 1300 * 1000;
////    encoder_params.iRCMode = RC_BITRATE_MODE;
//    //TODO
//    encoder_params.fMaxFrameRate = 24;
////    TODO
//    encoder_params.bEnableFrameSkip = true;
//    //TODO
////    encoder_params.uiIntraPeriod = 2;
////    encoder_params.uiMaxNalSize = 0;
////    encoder_params.iMultipleThreadIdc = 1;
////    encoder_params.sSpatialLayers[0].iVideoHeight = 640;
////    encoder_params.sSpatialLayers[0].iVideoWidth = 480;
////    encoder_params.sSpatialLayers[0].fFrameRate = 24;
////    encoder_params.sSpatialLayers[0].iSpatialBitrate = 0;
////    encoder_params.sSpatialLayers[0].iMaxSpatialBitrate = 0;
////    encoder_params.sSpatialLayers[0].sSliceArgument.uiSliceMode = SM_SINGLE_SLICE;
//    encoder_params.eSpsPpsIdStrategy = CONSTANT_ID;
//    return encoder_params;

    SEncParamExt param;
    encoder->GetDefaultParams (&param);
    param.iUsageType = CAMERA_VIDEO_REAL_TIME;
    param.fMaxFrameRate = 24;
    param.iPicWidth = frameWidth;
    param.iPicHeight = frameHeight;
    param.iTargetBitrate = 5000000;
    param.bEnableDenoise = true;
    param.bEnableFrameSkip = true;
//    param.iSpatialLayerNum = layers;
    param.iMultipleThreadIdc = 2;
    param.sSpatialLayers[0].iVideoWidth = 640;
    param.sSpatialLayers[0].iVideoHeight = 480;
    param.sSpatialLayers[0].fFrameRate = 24;
    param.sSpatialLayers[0].iSpatialBitrate = param.iTargetBitrate;

//    param.sSpatialLayers[0].sSliceCfg.uiSliceMode = sliceMode;
//    if (sliceMode == SM_DYN_SLICE) {
//        param.sSpatialLayers[i].sSliceCfg.sSliceArgument.uiSliceSizeConstraint = 600;
//        param.uiMaxNalSize = 1500;
//    }
    param.iTargetBitrate *= param.iSpatialLayerNum;
    return param;
}

uint8_t* wlanjie::H264encoder::startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride) {
    int trace_level = WELS_LOG_DETAIL;
    encoder->SetOption(ENCODER_OPTION_TRACE_LEVEL, &trace_level);
    int video_format = videoFormatI420;
    encoder->SetOption(ENCODER_OPTION_DATAFORMAT, &video_format);
    SSourcePicture picture;
    memset(&picture, 0, sizeof(SSourcePicture));
    picture.iPicWidth = frameWidth;
    picture.iPicHeight = frameHeight;
    picture.iColorFormat = videoFormatI420;
//    picture.uiTimeStamp = 0;
//    picture.iStride[0] = yStride; // y
//    picture.iStride[1] = uStride; // u
//    picture.iStride[2] = vStride; // v
//    picture.pData[0] = yData; // y
//    picture.pData[1] = uData; // u
//    picture.pData[2] = vData; // v
    picture.iStride[0] = picture.iPicWidth;

    SFrameBSInfo info;
    memset(&info, 0, sizeof(SFrameBSInfo));
    encoder->EncodeFrame(&picture, &info);

    size_t required_size = 0;
    size_t fragments_count = 0;
    for (int layer = 0; layer < info.iLayerNum; ++layer) {
        const SLayerBSInfo& layerInfo = info.sLayerInfo[layer];
        for (int nal = 0; nal < layerInfo.iNalCount; ++nal, ++fragments_count) {
            if (layerInfo.pNalLengthInByte[nal] == 0) {
                continue;
            }
            required_size += layerInfo.pNalLengthInByte[nal];
        }
    }
    int half_width = (frameWidth + 1) >> 1;
    int half_height = (frameHeight + 1) >> 1;
    encoded_image_length = frameWidth * frameHeight + half_width * half_height * 2;
    if (encoded_image_length < required_size) {

    }
    uint8_t *encoded_image_buffer = new uint8_t[encoded_image_length];
    int image_length = 0;
    for (int layer = 0; layer < info.iLayerNum; ++layer) {
        const SLayerBSInfo& layerInfo = info.sLayerInfo[layer];
        size_t layer_len = 0;
        for (int nal = 0; nal < layerInfo.iNalCount; ++nal) {
            layer_len += layerInfo.pNalLengthInByte[nal];
        }
        memcpy(encoded_image_buffer + image_length, layerInfo.pBsBuf, layer_len);
        image_length += layer_len;
    }
    return encoded_image_buffer;
}

void wlanjie::H264encoder::setFrameSize(int width, int height) {
    this->frameHeight = height;
    this->frameWidth = width;
}

int wlanjie::H264encoder::getEncoderImageLength() {
    return encoded_image_length;
}
