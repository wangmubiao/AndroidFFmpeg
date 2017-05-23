package com.wlanjie.streaming.camera;

/**
 * Created by wlanjie on 2017/5/23.
 */
public interface LivingCamera {
  void start();

  void stop();

  boolean isCameraOpened();

  void setFacing(int facing);

  void getFacing();
}
