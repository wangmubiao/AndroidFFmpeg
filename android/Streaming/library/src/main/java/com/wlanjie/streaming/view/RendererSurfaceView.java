package com.wlanjie.streaming.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.camera.AspectRatio;
import com.wlanjie.streaming.camera.Camera1;
import com.wlanjie.streaming.camera.Camera2;
import com.wlanjie.streaming.camera.Camera2Api23;
import com.wlanjie.streaming.camera.CameraCallback;
import com.wlanjie.streaming.camera.CameraView;
import com.wlanjie.streaming.camera.CameraViewImpl;
import com.wlanjie.streaming.camera.EglCore;
import com.wlanjie.streaming.video.SurfaceRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by caowu15 on 2017/5/22.
 */

public class RendererSurfaceView extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, SurfaceRenderer.OnSurfaceListener {

  private SurfaceRenderer mSurfaceRenderer;
  private SurfaceTexture mSurfaceTexture;
  private int mSurfaceTextureId;

  private CameraViewImpl mImpl;
  private CallbackBridge mCallbacks;

  public RendererSurfaceView(Context context, CameraViewImpl impl) {
    this(context, null, impl);
  }

  public RendererSurfaceView(Context context, AttributeSet attrs, CameraViewImpl impl) {
    super(context, attrs);
    init(impl);
  }

  private void init(CameraViewImpl impl) {
    mImpl = impl;
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    mSurfaceTextureId = textures[0];
    mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
    mSurfaceTexture.setOnFrameAvailableListener(this);

    impl.setPreviewSurface(mSurfaceTexture);

    mSurfaceRenderer = new SurfaceRenderer(new EglCore(getResources()), mSurfaceTexture, mSurfaceTextureId);
    mSurfaceRenderer.setOnSurfaceListener(this);
    setEGLContextClientVersion(2);
    setRenderer(mSurfaceRenderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        mImpl.stop();
      }
    });
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    requestRender();
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {

  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    mImpl.setSize(width, height);
    mImpl.start();
    mImpl.startPreview();
  }

  @Override
  public void onDrawFrame(GL10 gl) {

  }

  class CallbackBridge implements CameraCallback {

    private final List<CameraView.Callback> mCallbacks = new ArrayList<>();

    private boolean mRequestLayoutOnOpen;

    @Override
    public void onCameraOpened(int previewWidth, int previewHeight) {

    }

    @Override
    public void onCameraClosed() {

    }

    @Override
    public void onPreviewFrame(byte[] data) {

    }

    @Override
    public void onPreview(int previewWidth, int previewHeight) {

    }
  }

  public void setDisplayOrientation(int displayOrientation) {
    mImpl.setDisplayOrientation(displayOrientation);
  }

  public boolean isCameraOpened() {
    return mImpl.isCameraOpened();
  }

  public Set<AspectRatio> getSupportAspectRatios() {
    return mImpl.getSupportedAspectRatios();
  }

  public AspectRatio getAspectRatio() {
    return mImpl.getAspectRatio();
  }

  public boolean getAutoFocus() {
    return mImpl.getAutoFocus();
  }

  public int getFlash() {
    return mImpl.getFlash();
  }

  public int getFacing() {
    return mImpl.getFacing();
  }
}
