//
// Created by wlanjie on 16/5/3.
//


#ifndef _Included_com_wlanjie_ffmpeg_library_FFmpeg
#define _Included_com_wlanjie_ffmpeg_library_FFmpeg

#include <jni.h>
#include <queue>
#include <time.h>
#include "srs_librtmp.hpp"
#include "log.h"
#include "h264encoder.h"
#include "audioencoder.h"
#include "opengl/opengl.h"
#include "libyuv.h"
#include "VideoEncode.h"

#ifndef NELEM
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif
#define SOFT_CLASS_NAME "com/wlanjie/streaming/video/SoftEncoder"
#define RTMP_CLASS_NAME "com/wlanjie/streaming/rtmp/Rtmp"
#define OPENGL_CLASS_NAME "com/wlanjie/streaming/OpenGL"
#ifdef __cplusplus
extern "C" {
#endif

#define AUDIO_TYPE 0
#define VIDEO_TYPE 1

struct Frame {
    char *data;
    int size;
    int packet_type;
    int pts;
};

wlanjie::OpenGL *openGL;

std::queue<Frame> q;

//wlanjie::H264encoder h264encoder;
AudioEncode audioEncode;
srs_rtmp_t rtmp;
bool is_stop = false;
std::ofstream _outputStream;
VideoEncode videoEncode;

static jobject outputBuffer;
static jlong outputPixelBytes = 0;
static unsigned char *outputPixelBufferData = NULL;

void Android_JNI_startPublish(JNIEnv *env, jobject object) {
    LOGE("Android_JNI_startPublish");
    while (!is_stop) {
        while (!q.empty()) {
            Frame frame = q.front();
            LOGE("q.size = %d", q.size());
            q.pop();
            if (frame.packet_type == VIDEO_TYPE) {
                _outputStream.write(frame.data, frame.size);
                int ret = srs_h264_write_raw_frames(rtmp, frame.data, frame.size, frame.pts, frame.pts);
                LOGE("write h264 ret = %d", ret);
            }
            if (frame.data) {
                free(frame.data);
            }
        }
        usleep(1000 * 100);
    }
}

jboolean Android_JNI_openH264Encoder(JNIEnv* env, jobject object) {
     return (jboolean) (videoEncode.open_h264_encode() >= 0 ? JNI_TRUE : JNI_FALSE);
//    return (jboolean) h264encoder.openH264Encoder();
}

void Android_JNI_closeH264Encoder(JNIEnv* env, jobject object) {
//    h264encoder.closeH264Encoder();
     videoEncode.close_h264_encode();
}

void Android_JNI_encoderH264(JNIEnv *env, jobject object) {
     unsigned char *buffer = openGL->getBuffer();

     int width = openGL->getWidth();
     int height = openGL->getHeight();
     int ySize = openGL->getWidth() * openGL->getHeight();
     uint8_t *data = (uint8_t *) malloc((size_t) (ySize * 3 / 2));
     uint8_t *u = data + ySize;
     uint8_t *v = u + ySize / 4;
     libyuv::ConvertToI420(buffer, ySize, data, width, u, width / 2, v, width / 2, 0, 0, width, height, width, height, libyuv::kRotate0, libyuv::FOURCC_BPP_RGBA);
//     int size = videoEncode.x264_encode(data, u, v, width);
//     uint8_t *h264 = videoEncode.get_h264();
//    h264encoder.startEncoder(data, ySize, u, width / 2, v, width / 2);

//     char *frame_data = (char *) malloc((size_t) size);
//     memcpy(frame_data, h264, size);
//     Frame f;
//     f.data = frame_data;
//     f.size = size;
//     f.pts = (int) time(NULL) / 1000;
//     f.packet_type = VIDEO_TYPE;
//     q.push(f);
}

jboolean Android_JNI_openAacEncode(JNIEnv *env, jobject object, jint channels, jint sample_rate, jint bitrate) {
    return (jboolean) audioEncode.open_aac_encode(channels, sample_rate, bitrate);
}

jint Android_JNI_encoderPcmToAac(JNIEnv *env, jobject object, jbyteArray pcm, int pts) {
    jbyte *pcm_frame = env->GetByteArrayElements(pcm, NULL);
    int pcm_length = env->GetArrayLength(pcm);
    int aac_size = audioEncode.encode_pcm_to_aac((char *) pcm_frame, pcm_length);
    env->ReleaseByteArrayElements(pcm, pcm_frame, NULL);
    if (aac_size > 0) {
//        muxer_aac_success((char *) audioEncode.getAac(), aac_size, pts);
    }
    return 0;
}

void Android_JNI_closeAacEncoder() {
    audioEncode.close_aac_encode();
}

jint Android_JNI_connect(JNIEnv *env, jobject object, jstring url) {
    if (rtmp != NULL) {
        return -1;
    }
    const char *rtmp_url = env->GetStringUTFChars(url, 0);
    rtmp = srs_rtmp_create(rtmp_url);
    if (srs_rtmp_handshake(rtmp) != 0) {
        return -1;
    }
    if (srs_rtmp_connect_app(rtmp) != 0) {
        return -1;
    }
    if (srs_rtmp_publish_stream(rtmp) != 0) {
        return -1;
    }
    env->ReleaseStringUTFChars(url, rtmp_url);

    return 0;
}

int Android_JNI_write_video_sample(JNIEnv *env, jobject object, jlong timestamp, jbyteArray frame) {
    jbyte *data = env->GetByteArrayElements(frame, NULL);
    jsize data_size = env->GetArrayLength(frame);

    char *frame_data = (char *) malloc((size_t) data_size);
    memcpy(frame_data, data, data_size);
    Frame f;
    f.data = frame_data;
    f.size = data_size;
    f.pts = (int) timestamp;
    f.packet_type = VIDEO_TYPE;
    q.push(f);
    env->ReleaseByteArrayElements(frame, data, NULL);
    return 0;
}

jint Android_JNI_write_audio_sample(JNIEnv *env, jobject object, jlong timestamp, jbyteArray frame, jint sampleRate, jint channel) {
    jbyte *data = env->GetByteArrayElements(frame, NULL);
    jsize data_size = env->GetArrayLength(frame);
    int ret = srs_rtmp_write_packet(rtmp, SRS_RTMP_TYPE_AUDIO, timestamp, (char *) data, data_size);
    env->ReleaseByteArrayElements(frame, data, NULL);
    return ret;
}

void Android_JNI_destroy(JNIEnv *env, jobject object) {
    is_stop = true;
    srs_rtmp_destroy(rtmp);
    rtmp = NULL;
}

void Android_JNI_opengl_init(JNIEnv *env, jobject object, jint width, jint height) {
    videoEncode.setEncodeResolution(width, height);
    videoEncode.open_h264_encode();
    _outputStream.open("/sdcard/test.h264", std::ios_base::binary | std::ios_base::out);
    outputPixelBytes = width * height * 4;
    outputPixelBufferData = new unsigned char[outputPixelBytes];
    outputBuffer = env->NewDirectByteBuffer(outputPixelBufferData, outputPixelBytes);
    outputBuffer = env->NewGlobalRef(outputBuffer);

//    h264encoder.setFrameSize(width, height);
    openGL->init(width, height);
//    Android_JNI_openH264Encoder(env, object);
}

jint Android_JNI_opengl_draw(JNIEnv *env, jobject object, jint inputTextureId, jint pts) {
    int textureId = openGL->draw(inputTextureId);
    unsigned char* buffer = openGL->getBuffer();

//    int width = openGL->getWidth();
//    int height = openGL->getHeight();
//    size_t ySize = (size_t) (openGL->getWidth() * openGL->getHeight());
//    uint8_t *y = new uint8_t[ySize * 3 / 2];
//    uint8_t *u = y + ySize;
//    uint8_t *v = u + ySize / 4;
//    libyuv::ConvertToI420(buffer, ySize, y, width, u, width / 2, v, width / 2, 0, 0, width, height, width, height, libyuv::kRotate0, libyuv::FOURCC_RGBA);
//    uint8_t *encoded_image_buffer = h264encoder.startEncoder(y, ySize, u, width / 2, v, width / 2);
//
//    uint8_t *encoded_image_buffer = h264encoder.encoder((char *) buffer, pts);
    int h264_size = videoEncode.rgba_encode_to_h264((char *) buffer, openGL->getWidth(), openGL->getHeight(), false, 0, pts);
    if (h264_size > 0 && videoEncode.get_h264() != NULL) {
        char *frame_data = new char[h264_size];
        memcpy(frame_data, videoEncode.get_h264(), (size_t) h264_size);
        Frame f;
        f.data = frame_data;
        f.size = h264_size;
        f.pts = pts;
        f.packet_type = VIDEO_TYPE;
        q.push(f);
//
    }

//    if (encoded_image_buffer != NULL && h264encoder.getEncoderImageLength() > 0) {
//        LOGE("encoded_image_buffer_length = %d", h264encoder.getEncoderImageLength());
//        char *frame_data = new char[h264encoder.getEncoderImageLength()];
//        memcpy(frame_data, encoded_image_buffer, (size_t) h264encoder.getEncoderImageLength());
//        Frame f;
//        f.data = frame_data;
//        f.size = h264encoder.getEncoderImageLength();
//        f.pts = (int) time(NULL) / 1000;
//        f.packet_type = VIDEO_TYPE;
//        q.push(f);
//
//        free(encoded_image_buffer);
//    }

    return textureId;
}

void Android_JNI_opengl_setInputTexture(JNIEnv *env, jobject object, jint textureId) {
    openGL->setInputTexture(textureId);
}

void Android_JNI_opengl_setInputPixels(JNIEnv *env, jobject object, jbyteArray pixels) {
    jbyte *data =  env->GetByteArrayElements(pixels, JNI_FALSE);
    openGL->setInputPixels((uint8_t *) data);
}

jobject Android_JNI_opengl_getOutputPixels(JNIEnv *env, jobject object) {
    openGL->getBuffer(outputPixelBufferData);
    return outputBuffer;
}

void Android_JNI_opengl_setTextureTransformMatrix(JNIEnv *env, jobject object, jfloatArray matrix) {
    jfloat *data = env->GetFloatArrayElements(matrix, JNI_FALSE);
    openGL->setTextureTransformMatrix(data);
//    env->ReleaseFloatArrayElements(matrix, data, 0);
}

void Android_JNI_opengl_release(JNIEnv *env, jobject object) {
    videoEncode.close_h264_encode();
    openGL->release();
    if (outputBuffer && outputPixelBufferData) {
        env->DeleteGlobalRef(outputBuffer);
        delete outputPixelBufferData;

        outputBuffer = NULL;
        outputPixelBufferData = NULL;
    }
}

static JNINativeMethod soft_encoder_methods[] = {
        { "openH264Encoder",        "()V",                      (void *) Android_JNI_openH264Encoder },
        { "closeH264Encoder",       "()V",                      (void *) Android_JNI_closeH264Encoder },
        { "encoderH264",            "()V",                      (void *) Android_JNI_encoderH264 }
};

static JNINativeMethod rtmp_methods[] = {
        { "startPublish",           "()V",                      (void *) Android_JNI_startPublish },
        { "connect",                "(Ljava/lang/String;)I",    (void *) Android_JNI_connect },
        { "writeVideo",             "(J[B)I",                   (void *) Android_JNI_write_video_sample },
        { "writeAudio",             "(J[BII)I",                 (void *) Android_JNI_write_audio_sample },
        { "destroy",                "()V",                      (void *) Android_JNI_destroy },
};

static JNINativeMethod opengl_methods[] = {
        { "init",                   "(II)V",                    (void *) Android_JNI_opengl_init },
        { "draw",                   "(II)I",                     (void *) Android_JNI_opengl_draw },
        { "setInputTexture",        "(I)V",                     (void *) Android_JNI_opengl_setInputTexture },
        { "setInputPixels",         "([B)V",                    (void *) Android_JNI_opengl_setInputPixels },
        { "getOutputPixels",        "()Ljava/nio/ByteBuffer;",  (void *) Android_JNI_opengl_getOutputPixels },
        { "setTextureTransformMatrix","([F)V",                  (void *) Android_JNI_opengl_setTextureTransformMatrix },
        { "release",                "()V",                      (void *) Android_JNI_opengl_release },
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if ((vm)->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_FALSE;
    }

    openGL = new wlanjie::OpenGL();

    jclass soft_clazz = env->FindClass(SOFT_CLASS_NAME);
    env->RegisterNatives(soft_clazz, soft_encoder_methods, NELEM(soft_encoder_methods));
    env->DeleteLocalRef(soft_clazz);
    jclass rtmp_class = env->FindClass(RTMP_CLASS_NAME);
    env->RegisterNatives(rtmp_class, rtmp_methods, NELEM(rtmp_methods));
    env->DeleteLocalRef(rtmp_class);
    jclass opengl_class = env->FindClass(OPENGL_CLASS_NAME);
    env->RegisterNatives(opengl_class, opengl_methods, NELEM(opengl_methods));
    env->DeleteLocalRef(opengl_class);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    delete openGL;
}

#ifdef __cplusplus
}
#endif
#endif
