//
// Created by wlanjie on 2017/6/4.
//

#ifndef STREAMING_H264ENCODER_H
#define STREAMING_H264ENCODER_H

#include "wels/codec_api.h"

namespace wlanjie {

    class H264encoder {
    public:
        H264encoder();
        ~H264encoder();

        void setFrameSize(int width, int height);

        bool openH264Encoder();

        void closeH264Encoder();

        uint8_t* startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride);

        int getEncoderImageLength();
    private:
        SEncParamExt createEncoderParams() const;

    private:
        ISVCEncoder *encoder;

        int frameWidth;
        int frameHeight;
        int encoded_image_length;
    };

}

#endif //STREAMING_H264ENCODER_H
