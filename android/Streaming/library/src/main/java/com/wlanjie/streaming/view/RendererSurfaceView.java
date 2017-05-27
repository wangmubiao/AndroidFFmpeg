package com.wlanjie.streaming.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.Constants;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.video.HardEncoder;
import com.wlanjie.streaming.video.SurfaceRenderer;

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
  private CameraConfiguration mCameraConfiguration;

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

    if (configuration == null) {
      configuration = CameraConfiguration.createDefault();
    }
    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(configuration);
    } else if (Build.VERSION.SDK_INT < 23) {
      mCamera = new Camera21(mContext, configuration);
    } else {
      mCamera = new Camera21(mContext, configuration);
    }

    mSurfaceRenderer = new SurfaceRenderer(getContext(), mSurfaceTexture, mSurfaceTextureId);
    mSurfaceRenderer.setOnSurfaceListener(this);

    mSurfaceRenderer.setEncoder(new HardEncoder(VideoConfiguration.createDefault()));
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
    mCameraConfiguration = new CameraConfiguration.Builder()
      .setPreview(height, width)
      .setFacing(Constants.FACING_FRONT)
      .setSurfaceTexture(mSurfaceTexture)
      .build();
    mCamera.updateCameraConfiguration(mCameraConfiguration);
    mCamera.start();
  }

  @Override
  public void onDrawFrame(GL10 gl) {

  }
}
