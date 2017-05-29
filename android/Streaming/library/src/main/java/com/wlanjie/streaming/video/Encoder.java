package com.wlanjie.streaming.video;

import com.wlanjie.streaming.configuration.VideoConfiguration;

/**
 * Created by wlanjie on 2017/5/25.
 */

public interface Encoder {

  void setOnVideoEncoderListener(OnVideoEncoderListener l);

  void setVideoConfiguration(VideoConfiguration configuration);

  void prepareEncoder();

  boolean firstTimeSetup();

  void makeCurrent();

  void swapBuffers();

  void startEncoder();

  void releaseEncoder();
}
