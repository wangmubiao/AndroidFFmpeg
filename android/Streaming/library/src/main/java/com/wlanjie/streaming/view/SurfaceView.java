package com.wlanjie.streaming.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.OpenGL;
import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.Constants;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.video.EglCore;
import com.wlanjie.streaming.video.Encoder;
import com.wlanjie.streaming.video.RendererEncoder;
import com.wlanjie.streaming.video.SoftEncoder;
import com.wlanjie.streaming.video.SoftSurfaceRenderer;
import com.wlanjie.streaming.video.SurfaceRenderer;
import com.wlanjie.streaming.video.WindowInputSurface;
import com.wlanjie.streaming.video.WindowSurface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/5/22.
 */

public class SurfaceView extends android.view.SurfaceView implements SurfaceTexture.OnFrameAvailableListener, SurfaceRenderer.OnSurfaceListener, SurfaceRenderer.OnRendererEncoderListener {

  private SurfaceTexture mSurfaceTexture;
  private LivingCamera mCamera;
  private Context mContext;
  private CameraConfiguration mCameraConfiguration;
  private RendererEncoder mRendererEncoder;
  private Encoder mEncoder;
  private int mSurfaceTextureId;
  EglCore mEglCore;
  WindowSurface mWindowSurface;
  WindowInputSurface mWindowInputSurface;
  OpenGL mOpenGL = new OpenGL();

  private float[] mProjectionMatrix = new float[16];

  private float[] mSurfaceMatrix = new float[16];

  float[] mTransformMatrix = new float[16];

  public SurfaceView(Context context) {
    this(context, null);
  }

  public SurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init(null);
  }

  public void setVideoConfiguration(VideoConfiguration configuration) {
//    mEncoder.setVideoConfiguration(configuration);
  }

  public void setCameraConfiguration(CameraConfiguration configuration) {
    this.mCameraConfiguration = configuration;
  }

  public void init(CameraConfiguration configuration) {
    mEglCore = new EglCore();
    if (configuration == null) {
      configuration = CameraConfiguration.createDefault();
    }
    mCameraConfiguration = configuration;
    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(configuration);
    } else if (Build.VERSION.SDK_INT < 23) {
      mCamera = new Camera21(mContext, configuration);
    } else {
      mCamera = new Camera21(mContext, configuration);
    }



//    SurfaceRenderer mSurfaceRenderer = new SoftSurfaceRenderer(getContext(), mSurfaceTexture, mSurfaceTextureId);
//    mSurfaceRenderer.setOnSurfaceListener(this);
//    mSurfaceRenderer.setOnRendererEncoderListener(this);

//      mWindowSurface = new WindowSurface(mEglCore, mSurfaceTexture);
//      mWindowSurface.makeCurrent();

    getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(SurfaceView.this);

        CameraConfiguration.Builder builder = mCameraConfiguration.builder;
        if (mCameraConfiguration.width == 0 && mCameraConfiguration.height == 0) {
          builder.setPreview(720, 1280);
        }
        if (mCameraConfiguration.facing == -1) {
          builder.setFacing(Constants.FACING_BACK);
        }
        if (mCameraConfiguration.surfaceTexture == null) {
          builder.setSurfaceTexture(mSurfaceTexture);
        }

        mCamera.updateCameraConfiguration(builder.build());
        mCamera.start();
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        float outputAspectRatio = width > height ? (float) width / height : (float) height / width;
        float aspectRatio = outputAspectRatio / outputAspectRatio;
        if (width > height) {
          Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f);
        } else {
          Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
        }

        mOpenGL.init(720, 1280);
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stop();
        mOpenGL.release();
      }
    });
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    mSurfaceTexture.updateTexImage();
    mSurfaceTexture.getTransformMatrix(mSurfaceMatrix);
    Matrix.multiplyMM(mTransformMatrix, 0, mSurfaceMatrix, 0, mProjectionMatrix, 0);
    mOpenGL.setTextureTransformMatrix(mTransformMatrix);
    mOpenGL.draw(mSurfaceTextureId);
    mWindowSurface.swapBuffers();
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {

  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {

  }

  @Override
  public void onDrawFrame(GL10 gl) {

  }

  @Override
  public void onRenderEncoder(int textureId) {
//    if (mRendererEncoder == null) {
//      Encoder encoder = new HardEncoder();
//      encoder.prepareEncoder();
//      mRendererEncoder = new RendererEncoder(getContext(), encoder);
//    }
//    mRendererEncoder.draw(textureId);

    if (mEncoder == null) {
      mEncoder = new SoftEncoder();
      mEncoder.prepareEncoder();
      mEncoder.startEncoder();
    }
  }
}
