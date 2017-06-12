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
    encoder_params.uiIntraPeriod = 2;
    encoder_params.uiMaxNalSize = 0;
    encoder_params.iTemporalLayerNum = 3;
    encoder_params.iSpatialLayerNum = 1;
    encoder_params.bEnableLongTermReference = 0;
    encoder_params.bEnableSceneChangeDetect = 0;
    encoder_params.iMultipleThreadIdc = 1;
    encoder_params.sSpatialLayers[0].iVideoHeight = 640;
    encoder_params.sSpatialLayers[0].iVideoWidth = 360;
    encoder_params.sSpatialLayers[0].fFrameRate = 30;
    encoder_params.sSpatialLayers[0].iSpatialBitrate = 800 * 1000;
//    encoder_params.sSpatialLayers[0].iMaxSpatialBitrate = 0;
    encoder_params.sSpatialLayers[0].sSliceArgument.uiSliceMode = SM_FIXEDSLCNUM_SLICE;
//    encoder_params.eSpsPpsIdStrategy = SPS_LISTING;
    return encoder_params;
}

uint8_t* wlanjie::H264encoder::startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride) {
    SSourcePicture picture;
    memset(&picture, 0, sizeof(SSourcePicture));
    picture.iPicWidth = frameWidth;
    picture.iPicHeight = frameHeight;
    picture.iColorFormat = videoFormatI420;
    picture.uiTimeStamp = time(NULL) / 1000;
    picture.iStride[0] = picture.iPicWidth;
    picture.iStride[1] = picture.iPicWidth >> 1;
    picture.iStride[2] = picture.iPicWidth >> 1;
    picture.pData[0] = yData;
    picture.pData[1] = picture.pData[0] + picture.iPicWidth * picture.iPicHeight;
    picture.pData[2] = picture.pData[1] + (picture.iPicWidth * picture.iPicHeight >> 2);

    SFrameBSInfo info;
    memset(&info, 0, sizeof(SFrameBSInfo));
    int ret = encoder_->EncodeFrame(&picture, &info);
    if (!ret) {
//        size_t required_size = 0;
//        size_t fragments_count = 0;
//        for (int layer = 0; layer < info.iLayerNum; ++layer) {
//            const SLayerBSInfo& layerInfo = info.sLayerInfo[layer];
//            for (int nal = 0; nal < layerInfo.iNalCount; ++nal, ++fragments_count) {
//                if (layerInfo.pNalLengthInByte[nal] == 0) {
//                    continue;
//                }
//                required_size += layerInfo.pNalLengthInByte[nal];
//            }
//        }
//        int half_width = (frameWidth + 1) >> 1;
//        int half_height = (frameHeight + 1) >> 1;
//        encoded_image_length = frameWidth * frameHeight + half_width * half_height * 2;
//        if (encoded_image_length < required_size) {
//
//        }
//        uint8_t *encoded_image_buffer = new uint8_t[required_size];
//        int image_length = 0;
//        for (int layer = 0; layer < info.iLayerNum; ++layer) {
//            const SLayerBSInfo& layerInfo = info.sLayerInfo[layer];
//            size_t layer_len = 0;
//            for (int nal = 0; nal < layerInfo.iNalCount; ++nal) {
//                layer_len += layerInfo.pNalLengthInByte[nal];
//            }
//            memcpy(encoded_image_buffer + image_length, layerInfo.pBsBuf, layer_len);
//            image_length += layer_len;
//        }
//        encoded_image_length = image_length;
//        return encoded_image_buffer;

        uint8_t *encoded_image_buffer = new uint8_t[1024 * 1024];
        int image_length = 0;
        if (info.eFrameType != videoFrameTypeSkip) {
            encoded_image_length = 0;
            for (int layer = 0; layer < info.iLayerNum; ++layer) {
                const SLayerBSInfo &layerInfo = info.sLayerInfo[layer];
                for (int nal = 0; nal < layerInfo.iNalCount; ++nal) {
                    encoded_image_length += layerInfo.pNalLengthInByte[nal];
                }
                LOGE("encoded_image_length = %d", encoded_image_length);
                _outputStream.write((const char *) layerInfo.pBsBuf, encoded_image_length);
//                memcpy(encoded_image_buffer, layerInfo.pBsBuf, encoded_image_length);
//                image_length += encoded_image_length;
            }
        }
        return encoded_image_buffer;
    }
    return NULL;
}

void wlanjie::H264encoder::setFrameSize(int width, int height) {
    this->frameHeight = height;
    this->frameWidth = width;
}

int wlanjie::H264encoder::getEncoderImageLength() {
    return encoded_image_length;
}

uint8_t *wlanjie::H264encoder::encoder(char *rgba) {
    _sourcePicture.uiTimeStamp = time(NULL) / 1000;
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
