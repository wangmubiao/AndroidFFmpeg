package com.wlanjie.streaming.callback;

/**
 * Created by wlanjie on 2017/6/15.
 */

public interface SurfaceTextureCallback {

  void onSurfaceCreated();

  void onSurfaceChanged(int width, int height);

  void onSurfaceDestroyed();

  int onDrawFrame(int textureId, int textureWidth, int textureHeight, float[] transformMatrix);
}
