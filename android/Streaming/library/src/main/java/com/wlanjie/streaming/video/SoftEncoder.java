package com.wlanjie.streaming.video;

import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.rtmp.Rtmp;

/**
 * Created by wlanjie on 2017/5/25.
 */

public class SoftEncoder implements Encoder {

  private Rtmp rtmp = new Rtmp();

  @Override
  public void setOnVideoEncoderListener(OnVideoEncoderListener l) {

  }

  @Override
  public void setVideoConfiguration(VideoConfiguration configuration) {

  }

  @Override
  public void prepareEncoder() {
    rtmp.connect("rtmp://www.ossrs.net:1935/live/demo");
    openH264Encoder();
  }

  @Override
  public boolean firstTimeSetup() {
    return false;
  }

  @Override
  public void makeCurrent() {

  }

  @Override
  public void swapBuffers() {

  }

  @Override
  public void startEncoder() {
    new Thread(){
      @Override
      public void run() {
        super.run();
        rtmp.startPublish();
      }
    }.start();
    encoderH264();
  }

  @Override
  public void releaseEncoder() {
    closeH264Encoder();
  }

  private native void openH264Encoder();
  private native void closeH264Encoder();
  private native void encoderH264();
}
