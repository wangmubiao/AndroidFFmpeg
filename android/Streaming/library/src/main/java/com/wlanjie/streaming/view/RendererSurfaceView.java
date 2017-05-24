package com.wlanjie.streaming.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.camera.AspectRatio;
import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.CameraViewImpl;
import com.wlanjie.streaming.camera.EglCore;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.video.SurfaceRenderer;

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
  private LivingCamera mCamera;
  private Context mContext;

  public RendererSurfaceView(Context context) {
    this(context, null);
  }

  public RendererSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public void init(CameraConfiguration configuration) {
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    mSurfaceTextureId = textures[0];
    mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
    mSurfaceTexture.setOnFrameAvailableListener(this);

    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(configuration);
    } else if (Build.VERSION.SDK_INT < 23) {
      mCamera = new Camera21(mContext, configuration);
    } else {
      mCamera = new Camera21(mContext, configuration);
    }

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
        mCamera.stop();
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
//    mImpl.setSize(width, height);
//    mImpl.start();
//    mImpl.startPreview();
    mCamera.start();
  }

  @Override
  public void onDrawFrame(GL10 gl) {

  }

  public void setDisplayOrientation(int displayOrientation) {
//    mImpl.setDisplayOrientation(displayOrientation);
  }

//  public boolean isCameraOpened() {
//    return mImpl.isCameraOpened();
//  }
//
//  public Set<AspectRatio> getSupportAspectRatios() {
//    return mImpl.getSupportedAspectRatios();
//  }
//
//  public AspectRatio getAspectRatio() {
//    return mImpl.getAspectRatio();
//  }
//
//  public boolean getAutoFocus() {
//    return mImpl.getAutoFocus();
//  }
//
//  public int getFlash() {
//    return mImpl.getFlash();
//  }
//
//  public int getFacing() {
//    return mImpl.getFacing();
//  }
}
