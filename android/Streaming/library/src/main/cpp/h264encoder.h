//
// Created by wlanjie on 2017/6/4.
//

#ifndef STREAMING_H264ENCODER_H
#define STREAMING_H264ENCODER_H

#include <iosfwd>
//#include "wels/codec_api.h"
#include <fstream>
#include <iostream>

namespace wlanjie {

    class H264encoder {
    public:
        H264encoder();
        ~H264encoder();

        void setFrameSize(int width, int height);

        bool openH264Encoder();

        void closeH264Encoder();

        uint8_t* startEncoder(uint8_t *yData, int yStride, uint8_t *uData, int uStride, uint8_t *vData, int vStride);

        uint8_t* encoder(char *rgba, long pts);

        int getEncoderImageLength();
    private:
//        SEncParamExt createEncoderParams() const;

    private:
//        ISVCEncoder *encoder_;
//        SFrameBSInfo info;
//        Source_Picture_s _sourcePicture;
        std::ofstream _outputStream;

        int frameWidth;
        int frameHeight;
        int encoded_image_length;
        time_t present_time_us;
    };

}

#endif //STREAMING_H264ENCODER_H
