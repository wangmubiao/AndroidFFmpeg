package com.wlanjie.streaming.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import com.wlanjie.streaming.callback.*;
import com.wlanjie.streaming.callback.CameraCallback;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.setting.CameraSetting;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by wlanjie on 2017/5/23.
 */
@SuppressWarnings("deprecation")
public class Camera9 implements LivingCamera {
  private static final int INVALID_CAMERA_ID = -1;

  private CameraSetting mCameraSetting;
  private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
  private CameraSetting.CameraFacingId mFacing;
  private int mCameraId;
  private Camera mCamera;
  private Camera.Parameters mCameraParameters;
  private final List<Size> mPreviewSizes = new LinkedList<>();
  private AspectRatio mAspectRatio;
  private boolean mShowingPreview;
  private CameraCallback mCameraCallback;

  public Camera9(CameraSetting cameraSetting) {
    mCameraSetting = cameraSetting;
    mFacing = mCameraSetting.getFacing();
  }

  @Override
  public void start() {
    chooseCamera();
    openCamera();
    if (mCamera != null) {
      try {
        mCamera.setPreviewTexture(mCameraSetting.getSurfaceTexture());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void chooseCamera() {
    for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
      Camera.getCameraInfo(i, mCameraInfo);
      if (mCameraInfo.facing == mFacing.ordinal()) {
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
    adjustCameraParameters();
    mCamera.setDisplayOrientation(calcCameraRotation(mCameraSetting.getDisplayOrientation()));
  }

  private void adjustCameraParameters() {
    Size size;
    if (mCameraCallback != null) {
      size = mCameraCallback.onPreviewSizeSelected(mPreviewSizes);
    } else {
      size = mPreviewSizes.get(mPreviewSizes.size() / 2);
    }
    if (mShowingPreview) {
      mCamera.stopPreview();
    }

    mCameraParameters.setRotation(calcCameraRotation(mCameraSetting.getDisplayOrientation()));
    int[] fps = chooseFpsRange();
    mCameraParameters.setPreviewFpsRange(fps[0], fps[1]);
    mCameraParameters.setPreviewFormat(ImageFormat.NV21);
    if (mShowingPreview) {
      mCamera.stopPreview();
    }
    mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
    mCamera.setParameters(mCameraParameters);
    mCamera.setDisplayOrientation(calcCameraRotation(mCameraSetting.getDisplayOrientation()));
    mCamera.startPreview();
    mShowingPreview = true;
  }


  private int[] chooseFpsRange() {
    int expectedFps = 24 * 1000;
    int[] closestRange = mCameraParameters.getSupportedPreviewFpsRange().get(0);
    int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
    for (int[] range : mCameraParameters.getSupportedPreviewFpsRange()) {
      if (range[0] <= expectedFps && range[1] >= expectedFps) {
        int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
        if (curMeasure < measure) {
          closestRange = range;
          measure = curMeasure;
        }
      }
    }
    return closestRange;
  }
//
//  @SuppressWarnings("SuspiciousNameCombination")
//  private Size chooseOptimalSize(SortedSet<Size> sizes) {
//    int desiredWidth;
//    int desiredHeight;
//    final int surfaceWidth = mCameraConfiguration.width;
//    final int surfaceHeight = mCameraConfiguration.height;
//    if (mCameraSetting.getDisplayOrientation() == 90 || mCameraSetting.getDisplayOrientation() == 270) {
//      desiredWidth = surfaceHeight;
//      desiredHeight = surfaceWidth;
//    } else {
//      desiredWidth = surfaceWidth;
//      desiredHeight = surfaceHeight;
//    }
//    Size result = null;
//    for (Size size : sizes) { // Iterate from small to large
//      if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
//        return size;
//
//      }
//      result = size;
//    }
//    return result;
//  }

  private int calcCameraRotation(int rotation) {
    if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      return (360 - (mCameraInfo.orientation + rotation) % 360) % 360;
    } else {  // back-facing
      return (mCameraInfo.orientation - rotation + 360) % 360;
    }
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.setPreviewCallbackWithBuffer(null);
      mCamera.release();
      mCamera = null;
      if (mCameraCallback != null) {
        mCameraCallback.onCloseCamera();
      }
    }
  }

  @Override
  public void stop() {
    if (mCamera != null) {
      mCamera.stopPreview();
    }
    mShowingPreview = false;
    releaseCamera();
  }

  @Override
  public boolean isCameraOpened() {
    return mCamera != null;
  }

  @Override
  public void setFacing(CameraSetting.CameraFacingId facing) {
    if (facing == mFacing) {
      return;
    }
    mFacing  = facing;
    if (isCameraOpened()) {
      stop();
      start();
    }
  }

  @Override
  public CameraSetting.CameraFacingId getFacing() {
    return mFacing;
  }

  @Override
  public void updateCameraConfiguration(CameraConfiguration configuration) {
//    mCameraConfiguration = configuration;
  }

  @Override
  public void setCameraCallback(CameraCallback callback) {
    mCameraCallback = callback;
  }
}
