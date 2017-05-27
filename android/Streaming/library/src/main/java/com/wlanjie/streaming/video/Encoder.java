package com.wlanjie.streaming.video;

/**
 * Created by wlanjie on 2017/5/25.
 */

public interface Encoder {

  void prepareEncoder();

  boolean firstTimeSetup();

  void makeCurrent();

  void swapBuffers();

  void startEncoder();

  void releaseEncoder();
}
