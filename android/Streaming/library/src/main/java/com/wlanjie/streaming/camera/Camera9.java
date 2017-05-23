package com.wlanjie.streaming.camera;

import android.hardware.Camera;

import com.wlanjie.streaming.configuration.CameraConfiguration;

/**
 * Created by wlanjie on 2017/5/23.
 */
@SuppressWarnings("deprecation")
public class Camera9 implements LivingCamera {
  private static final int INVALID_CAMERA_ID = -1;

  private final CameraConfiguration mCameraConfiguration;
  private Camera.CameraInfo mCameraInfo;
  private int mFacing;
  private int mCameraId;
  private Camera mCamera;
  private Camera.Parameters mCameraParameters;
  private final SizeMap mPreviewSizes = new SizeMap();
  private AspectRatio mAspectRatio;

  public Camera9(CameraConfiguration configuration) {
    mCameraConfiguration = configuration;
  }

  @Override
  public void start() {
    chooseCamera();
    openCamera();
  }

  private void chooseCamera() {
    for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
      Camera.getCameraInfo(i, mCameraInfo);
      if (mCameraInfo.facing == mFacing) {
        mCameraId = i;
        return;
      }
    }
    mCameraId = INVALID_CAMERA_ID;
  }

  private void openCamera() {
    if (mCamera != null) {
      releaseCamera();
    }
    mCamera = Camera.open(mCameraId);
    mCameraParameters = mCamera.getParameters();
    mPreviewSizes.clear();
    for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
      mPreviewSizes.add(new Size(size.width, size.height));
    }
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.setPreviewCallbackWithBuffer(null);
      mCamera.release();
      mCamera = null;
      mCameraConfiguration.cameraCallback.onCameraClosed();
    }
  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isCameraOpened() {
    return false;
  }

  @Override
  public void setFacing(int facing) {

  }

  @Override
  public void getFacing() {

  }
}
