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
import com.wlanjie.streaming.video.Encoder;
import com.wlanjie.streaming.video.HardEncoder;
import com.wlanjie.streaming.video.HardSurfaceRenderer;
import com.wlanjie.streaming.video.RendererEncoder;
import com.wlanjie.streaming.video.SoftEncoder;
import com.wlanjie.streaming.video.SoftSurfaceRenderer;
import com.wlanjie.streaming.video.SurfaceRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/5/22.
 */

public class RendererSurfaceView extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, SurfaceRenderer.OnSurfaceListener, SurfaceRenderer.OnRendererEncoderListener {

  private SurfaceTexture mSurfaceTexture;
  private LivingCamera mCamera;
  private Context mContext;
  private CameraConfiguration mCameraConfiguration;
  private RendererEncoder mRendererEncoder;
  private Encoder mEncoder;

  public RendererSurfaceView(Context context) {
    this(context, null);
  }

  public RendererSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init(null);
  }

  public void setVideoConfiguration(VideoConfiguration configuration) {
//    mEncoder.setVideoConfiguration(configuration);
  }

  public void setCameraConfiguration(CameraConfiguration configuration) {
    this.mCameraConfiguration = configuration;
    System.out.println("setCameraConfiguration");
  }

  public void init(CameraConfiguration configuration) {
    System.out.println("surface init");
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    int mSurfaceTextureId = textures[0];
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

    SurfaceRenderer mSurfaceRenderer = new SoftSurfaceRenderer(getContext(), mSurfaceTexture, mSurfaceTextureId);
    mSurfaceRenderer.setOnSurfaceListener(this);
    mSurfaceRenderer.setOnRendererEncoderListener(this);

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
    CameraConfiguration.Builder builder = mCameraConfiguration.builder;
    if (mCameraConfiguration.width == 0 && mCameraConfiguration.height == 0) {
      builder.setPreview(height, width);
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
