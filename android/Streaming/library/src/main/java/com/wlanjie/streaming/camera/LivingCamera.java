package com.wlanjie.streaming.camera;

import com.wlanjie.streaming.configuration.CameraConfiguration;

/**
 * Created by wlanjie on 2017/5/23.
 */
public interface LivingCamera {
  void start();

  void stop();

  boolean isCameraOpened();

  void setFacing(int facing);

  int getFacing();

  void updateCameraConfiguration(CameraConfiguration configuration);
}
