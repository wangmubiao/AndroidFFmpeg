package com.wlanjie.streaming.camera;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.callback.SurfaceTextureCallback;
import com.wlanjie.streaming.setting.CameraSetting;
import com.wlanjie.streaming.video.HardSurfaceRenderer;
import com.wlanjie.streaming.video.SoftSurfaceRenderer;
import com.wlanjie.streaming.video.SurfaceRenderer;

/**
 * Created by wlanjie on 2017/6/15.
 */

public class CameraRender implements SurfaceTexture.OnFrameAvailableListener, SurfaceHolder.Callback {

  private final GLSurfaceView mSurfaceView;
  private LivingCamera mCamera;
  private int mSurfaceTextureId;
  private SurfaceTexture mSurfaceTexture;
  private SurfaceRenderer mRenderer;

  public CameraRender(GLSurfaceView surfaceView) {
    mSurfaceView = surfaceView;
  }

  public void prepare(CameraSetting cameraSetting) {
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    mSurfaceTextureId = textures[0];
    mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
    mSurfaceView.setEGLContextClientVersion(2);
    mRenderer = new HardSurfaceRenderer(mSurfaceView.getContext(), mSurfaceTexture, mSurfaceTextureId);
//    mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    mSurfaceView.setRenderer(mRenderer);
    cameraSetting.setSurfaceTexture(mSurfaceTexture);
    mSurfaceTexture.setOnFrameAvailableListener(this);

//    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(cameraSetting);
//    } else if (Build.VERSION.SDK_INT < 23) {
//      mCamera = new Camera21(mSurfaceView.getContext(), cameraSetting);
//    } else {
//      mCamera = new Camera21(mSurfaceView.getContext(), cameraSetting);
//    }

    mSurfaceView.getHolder().addCallback(this);
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    mSurfaceView.requestRender();
  }

  public void resume() {
    mCamera.start();
  }

  public void pause() {
    mCamera.stop();
  }

  public void addSurfaceTextureCallback(SurfaceTextureCallback callback) {
    mRenderer.addSurfaceTextureCallback(callback);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    mSurfaceTexture.release();
  }
}
