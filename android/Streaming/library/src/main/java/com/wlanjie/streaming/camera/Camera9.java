package com.wlanjie.streaming.camera;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.v4.util.SparseArrayCompat;

import java.util.Set;
import java.util.SortedSet;

/**
 * Created by wlanjie on 2017/5/21.
 */
@SuppressWarnings("deprecation")
public class Camera9 implements Camera {

    private static final int INVALID_CAMERA_ID = -1;

    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, android.hardware.Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, android.hardware.Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, android.hardware.Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, android.hardware.Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private android.hardware.Camera mCamera;

    private final android.hardware.Camera.CameraInfo mCameraInfo = new android.hardware.Camera.CameraInfo();

    private android.hardware.Camera.Parameters mCameraParamters;

    private final SizeMap mPreviewSizes = new SizeMap();

    private int mCameraId;

    private int mFacing;

    private AspectRatio mAspectRatio;

    private int mSurfaceWidth;

    private int mSurfaceHeight;

    private boolean mShowingPreview = false;

    private int mDisplayOrientation;

    private CameraCallback mCallback;

    public Camera9(CameraCallback callback) {
        mCallback = callback;
    }

    @Override
    public void start(SurfaceTexture texture) {
        chooseCamera();
        openCamera();
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    private void chooseCamera() {
        int cameraNumber = android.hardware.Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNumber; i++) {
            android.hardware.Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    private void openCamera() {
        if (mCamera != null) {
            mCamera.release();
        }

        mCamera = android.hardware.Camera.open(mCameraId);
        mCameraParamters = mCamera.getParameters();
        mPreviewSizes.clear();
        for (android.hardware.Camera.Size size : mCameraParamters.getSupportedPreviewSizes()) {
            mPreviewSizes.add(new Size(size.width, size.height));
        }
        if (mAspectRatio == null) {
            mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }
        adjustCameraParameters();
        mCamera.setDisplayOrientation(calcCameraRotation(mDisplayOrientation));
        android.hardware.Camera.Size size = mCamera.getParameters().getPreviewSize();
        mCallback.onCameraOpened(size.width, size.height);
    }

    private int calcCameraRotation(int rotation) {
        if (mCameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + rotation) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - rotation + 360) % 360;
        }
    }

    private void adjustCameraParameters() {
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) {
            mAspectRatio = chooseAspectRatio();
            sizes = mPreviewSizes.sizes(mAspectRatio);
        }
        Size size = chooseOptimalSize(sizes);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = mSurfaceWidth;
        final int surfaceHeight = mSurfaceHeight;
        if (mDisplayOrientation == 90 || mDisplayOrientation == 270) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) { // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;

            }
            result = size;
        }
        return result;
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                return ratio;
            }
        }
        return r;
    }

    @Override
    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
        }
        if (mCallback != null) {
            mCallback.onCameraClosed();
        }
    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            stop();
//            start();
        }
    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        return null;
    }

    @Override
    public void setAspectRatio(AspectRatio ratio) {
        if (mAspectRatio == null || !isCameraOpened()) {
            mAspectRatio = ratio;
        } else if (!mAspectRatio.equals(ratio)) {
            final Set<Size> sizes = mPreviewSizes.sizes(ratio);
            if (sizes == null) {
                throw new UnsupportedOperationException(ratio + " is not supported");
            } else {
                mAspectRatio = ratio;
                adjustCameraParameters();
            }
        }
    }

    @Override
    public AspectRatio getAspectRatio() {
        return null;
    }

    @Override
    public void setAutoFocus(boolean autoFocus) {

    }

    @Override
    public boolean getAutoFocus() {
        return false;
    }

    @Override
    public void setFlash(int flash) {

    }

    @Override
    public int getFlash() {
        return 0;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        if (mDisplayOrientation == displayOrientation) {
            return;
        }
        mDisplayOrientation = displayOrientation;
        if (isCameraOpened()) {
            int cameraRotation = calcCameraRotation(displayOrientation);
            mCameraParamters.setRotation(cameraRotation);
            mCamera.setParameters(mCameraParamters);
            final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(cameraRotation);
            if (needsToStopPreview) {
                mCamera.startPreview();
            }
        }
    }
}
