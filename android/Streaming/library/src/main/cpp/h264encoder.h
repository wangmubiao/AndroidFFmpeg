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

        bool openH264Encoder();

        void closeH264Encoder();

    private:
        ISVCEncoder *encoder;
    };

}

#endif //STREAMING_H264ENCODER_H
