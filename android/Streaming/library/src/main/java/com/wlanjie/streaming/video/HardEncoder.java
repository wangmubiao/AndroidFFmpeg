package com.wlanjie.streaming.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.wlanjie.streaming.configuration.VideoConfiguration;

/**
 * Created by wlanjie on 2017/5/25.
 */

public class HardEncoder implements Encoder {

  private VideoConfiguration mVideoConfiguration;
  private MediaCodec mMediaCodec;
  private InputSurface mInputSurface;
  private HandlerThread mHandlerThread;
  private Handler mEncoderHandler;
  private MediaCodec.BufferInfo mBufferInfo;

  public HardEncoder(VideoConfiguration configuration) {
    mVideoConfiguration = configuration;
  }

  @Override
  public void prepareEncoder() {
    if (mMediaCodec != null || mInputSurface != null) {
      throw new IllegalStateException("prepareEncoder already called.");
    }
    mMediaCodec = getVideoMediaCodec(mVideoConfiguration);
    mHandlerThread = new HandlerThread("HardEncoder");
    mHandlerThread.start();
    mEncoderHandler = new Handler(mHandlerThread.getLooper());
    mBufferInfo = new MediaCodec.BufferInfo();
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private MediaCodec getVideoMediaCodec(VideoConfiguration videoConfiguration) {
    int videoWidth = getVideoSize(videoConfiguration.width);
    int videoHeight = getVideoSize(videoConfiguration.height);
    MediaFormat format = MediaFormat.createVideoFormat(videoConfiguration.mime, videoWidth, videoHeight);
    format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
      MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    format.setInteger(MediaFormat.KEY_BIT_RATE, videoConfiguration.maxBps * 1024);
    int fps = videoConfiguration.fps;
    //设置摄像头预览帧率
//    if(BlackListHelper.deviceInFpsBlacklisted()) {
//      SopCastLog.d(SopCastConstant.TAG, "Device in fps setting black list, so set mediacodec fps 15");
//      fps = 15;
//    }
    format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfiguration.ifi);
    format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
    format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
    MediaCodec mediaCodec = null;

    try {
      mediaCodec = MediaCodec.createEncoderByType(videoConfiguration.mime);
      mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }catch (Exception e) {
      e.printStackTrace();
      if (mediaCodec != null) {
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
      }
    }
    return mediaCodec;
  }

  // We avoid the device-specific limitations on width and height by using values that
  // are multiples of 16, which all tested devices seem to be able to handle.
  private int getVideoSize(int size) {
    int multiple = (int)Math.ceil(size/16.0);
    return multiple*16;
  }

  @Override
  public void releaseEncoder() {

  }
}
