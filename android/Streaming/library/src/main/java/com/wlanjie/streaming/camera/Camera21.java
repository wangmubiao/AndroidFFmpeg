package com.wlanjie.streaming.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.wlanjie.streaming.callback.*;
import com.wlanjie.streaming.callback.CameraCallback;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.setting.CameraSetting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by wlanjie on 2017/5/23.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera21 implements LivingCamera {
  private static final String TAG = Camera21.class.getSimpleName();
  private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();
  private CameraCallback mCameraCallback;

  static {
    INTERNAL_FACINGS.put(Constants.FACING_BACK, CameraCharacteristics.LENS_FACING_BACK);
    INTERNAL_FACINGS.put(Constants.FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT);
  }

  private final CameraManager mCameraManager;

  private CameraSetting mCameraSetting;
//  private CameraConfiguration mCameraConfiguration;
  private HandlerThread mBackgroundThread;
  private Handler mBackgroundHandler;
  private String mCameraId;
  private CameraCharacteristics mCameraCharacteristics;
  private CameraSetting.CameraFacingId mFacing;
  private final List<Size> mPreviewSizes = new LinkedList<>();
  private AspectRatio mAspectRatio;
  private CaptureRequest.Builder mPreviewRequestBuilder;
  private CameraDevice mCamera;
  private CameraCaptureSession mCaptureSession;
  private ImageReader mImageReader;

  public Camera21(Context context, CameraSetting cameraSetting) {
    mCameraSetting = cameraSetting;
    mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
  }

  @Override
  public void start() {
    startBackgroundThread();
    chooseCameraIdByFacing();
    collectCameraInfo();
    try {
      startOpeningCamera();
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private void startBackgroundThread() {
    mBackgroundThread = new HandlerThread("CameraBackground");
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  private void chooseCameraIdByFacing() {
    try {
      int internalFacing = INTERNAL_FACINGS.get(CameraSetting.CameraFacingId.CAMERA_FACING_FRONT.ordinal());
      final String[] ids = mCameraManager.getCameraIdList();
      for (String id : ids) {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
        Integer internal = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (internal == null) {
          throw new NullPointerException("Unexpected state: LENS_FACING null");
        }
        if (internal == internalFacing) {
          mCameraId = id;
          mCameraCharacteristics = characteristics;
          return;
        }
      }
      // Not found
      mCameraId = ids[0];
      mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
      Integer internal = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
      if (internal == null) {
        throw new NullPointerException("Unexpected state: LENS_FACING null");
      }
//      for (int i = 0, count = INTERNAL_FACINGS.size(); i < count; i++) {
//        if (INTERNAL_FACINGS.valueAt(i) == internal) {
//          mFacing = INTERNAL_FACINGS.keyAt(i);
//          return;
//        }
//      }
//      // The operation can reach here when the only camera device is an external one.
//      // We treat it as facing back.
//      mFacing = Constants.FACING_BACK;

      mFacing = CameraSetting.CameraFacingId.CAMERA_FACING_FRONT;
    } catch (CameraAccessException e) {
      throw new RuntimeException("Failed to get a list of camera devices", e);
    }
  }

  private void collectCameraInfo() {
    StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    if (map == null) {
      throw new IllegalStateException("Failed to get configuration map: " + mCameraId);
    }
    mPreviewSizes.clear();
    for (android.util.Size size : map.getOutputSizes(SurfaceTexture.class)) {
      mPreviewSizes.add(new Size(size.getWidth(), size.getHeight()));
    }
  }

  private void startOpeningCamera() throws CameraAccessException {
    mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
      @Override
      public void onOpened(@NonNull CameraDevice camera) {
        mCamera = camera;
        startPreview();
      }

      @Override
      public void onDisconnected(@NonNull CameraDevice camera) {
        mCamera = null;
      }

      @Override
      public void onError(@NonNull CameraDevice camera, int error) {
        mCamera = null;
      }

      @Override
      public void onClosed(@NonNull CameraDevice camera) {
        super.onClosed(camera);
      }
    }, mBackgroundHandler);
  }

  private void startPreview() {
    if (!isCameraOpened()) {
      return;
    }
    Size previewSize;
    if (mCameraCallback != null) {
      previewSize = mCameraCallback.onPreviewSizeSelected(mPreviewSizes);
    } else {
      previewSize = mPreviewSizes.get(mPreviewSizes.size() / 2);
    }
    mCameraSetting.getSurfaceTexture().setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

    try {
      mPreviewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

      List<Surface> surfaces = new ArrayList<>();
      Surface surface = new Surface(mCameraSetting.getSurfaceTexture());
      surfaces.add(surface);
      mPreviewRequestBuilder.addTarget(surface);
      mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
          if (mCamera == null) {
            return;
          }
          mCaptureSession = session;
          updatePreview();
//          updateAutoFocus();
//          updateFlash();
          try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
              new CameraCaptureSession.CaptureCallback() { }, null);
          } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e);
          } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to start camera preview.", e);
          }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
          if (mCaptureSession != null && mCaptureSession.equals(session)) {
            mCaptureSession = null;
          }
        }
      }, null);
    } catch (CameraAccessException e) {
      throw new RuntimeException("Failed to start camera session");
    }
  }

  private void updatePreview() {
    if (!isCameraOpened()) {
      return;
    }
    try {
      mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() { }, null);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() {
    if (mCaptureSession != null) {
      mCaptureSession.close();
      mCaptureSession = null;
    }
    if (mCamera != null) {
      mCamera.close();
      mCamera = null;
    }
    if (mImageReader != null) {
      mImageReader.close();
      mImageReader = null;
    }
    if (mBackgroundHandler != null && mBackgroundThread != null) {
      mBackgroundThread.quitSafely();
      try {
        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler = null;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean isCameraOpened() {
    return mCamera != null;
  }

  @Override
  public void setFacing(CameraSetting.CameraFacingId facing) {
    if (mFacing == facing) {
      return;
    }
    mFacing = facing;
    if (isCameraOpened()) {
      stop();
      start();
    }
  }

  @Override
  public Size getPreviewSize() {
    return null;
  }

  @Override
  public CameraSetting.CameraFacingId getFacing() {
    return mFacing;
  }

  @Override
  public void updateCameraConfiguration(CameraConfiguration configuration) {
  }

  @Override
  public void setCameraCallback(CameraCallback callback) {
    mCameraCallback = callback;
  }
}
