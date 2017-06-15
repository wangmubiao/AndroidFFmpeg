package com.wlanjie.streaming.utils;

import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by wlanjie on 2017/6/15.
 */

public class CameraUtils {


  public static int getOrientation(int currentFacingId) {
    Camera.CameraInfo camInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(currentFacingId, camInfo);
    return camInfo.orientation;
  }

  public static int getOrientation(Context context, int currentFacingId) {
    int degree = getDeviceRotationDegree(context);
    Camera.CameraInfo camInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(currentFacingId, camInfo);
    int orientation;
    if(currentFacingId == 1) {
      orientation = (camInfo.orientation + degree) % 360;
    } else {
      orientation = (camInfo.orientation - degree + 360) % 360;
    }
    return orientation;
  }

  private static int getDisplayDefaultRotation(Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    return windowManager.getDefaultDisplay().getRotation();
  }

  private static int getDeviceRotationDegree(Context context) {
    switch(getDisplayDefaultRotation(context)) {
      case Surface.ROTATION_0:
        return 0;
      case Surface.ROTATION_90:
        return 90;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_270:
        return 270;
      default:
        return 0;
    }
  }
}
