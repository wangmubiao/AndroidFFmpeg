package com.wlanjie.streaming.camera;

import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.setting.CameraSetting;

/**
 * Created by wlanjie on 2017/5/23.
 */
public interface LivingCamera {
  void start();

  void stop();

  boolean isCameraOpened();

  void setFacing(CameraSetting.CameraFacingId facing);

  CameraSetting.CameraFacingId getFacing();

  void updateCameraConfiguration(CameraConfiguration configuration);

  void setCameraCallback(com.wlanjie.streaming.callback.CameraCallback callback);
}
