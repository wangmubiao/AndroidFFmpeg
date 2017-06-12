package com.wlanjie.streaming;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.CameraView;
import com.wlanjie.streaming.camera.Constants;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.rtmp.Rtmp;
import com.wlanjie.streaming.video.EglCore;
import com.wlanjie.streaming.video.WindowSurface;
import com.wlanjie.streaming.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {

  private CameraView mCameraView;
  private EglCore mEglCore;
  private WindowSurface mWindowSurface;
  private LivingCamera mCamera;
  private int mSurfaceTextureId;
  private SurfaceTexture mSurfaceTexture;
  private CameraConfiguration configuration;
  private OpenGL mOpenGL = new OpenGL();
  private float[] mProjectionMatrix = new float[16];

  private float[] mSurfaceMatrix = new float[16];

  float[] mTransformMatrix = new float[16];
  private Rtmp mRtmp = new Rtmp();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }

    mCameraView = (CameraView) findViewById(R.id.surface_view);
    mCameraView.setVideoConfiguration(VideoConfiguration.createDefault());
    mCameraView.setCameraConfiguration(CameraConfiguration.createDefault());
    mCameraView.start("rtmp://192.168.1.102/live/livestream");

    mEglCore = new EglCore();

    new Thread(){
      @Override
      public void run() {
        super.run();
        mRtmp.connect("rtmp://www.ossrs.net:1935/live/test");
        mRtmp.startPublish();
      }
    }.start();
  }

  @Override
  protected void onResume() {
    super.onResume();
    configuration = CameraConfiguration.createDefault();
    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(configuration);
    } else if (Build.VERSION.SDK_INT < 23) {
      mCamera = new Camera21(this, configuration);
    } else {
      mCamera = new Camera21(this, configuration);
    }

    android.view.SurfaceView surfaceView = (android.view.SurfaceView) findViewById(R.id.surface);
    surfaceView.getHolder().addCallback(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCamera.stop();
    GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
    int[] texture = { mSurfaceTextureId };
    GLES20.glDeleteTextures(1, texture, 0);

    mSurfaceTexture.release();
    mSurfaceTexture = null;

    if (mWindowSurface != null) {
      mWindowSurface.release();
      mWindowSurface = null;
    }
    if (mEglCore != null) {
      mEglCore.makeNothingCurrent();
      mEglCore.release();
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
    mWindowSurface.makeCurrent();

    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    mSurfaceTextureId = textures[0];
    mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
    mSurfaceTexture.setOnFrameAvailableListener(this);

    CameraConfiguration.Builder builder = configuration.builder;
    if (configuration.width == 0 && configuration.height == 0) {
      builder.setPreview(768, 1280);
    }
    if (configuration.facing == -1) {
      builder.setFacing(Constants.FACING_BACK);
    }
    if (configuration.surfaceTexture == null) {
      builder.setSurfaceTexture(mSurfaceTexture);
    }

    mCamera.updateCameraConfiguration(builder.build());
    mCamera.start();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    mOpenGL.init(1280, 768);
    float outputAspectRatio = width > height ? (float) width / height : (float) height / width;
    float aspectRatio = outputAspectRatio / outputAspectRatio;
    if (width > height) {
      Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f);
    } else {
      Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {

  }

  int count = 0;

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    if (mSurfaceTexture == null) return;
    mSurfaceTexture.updateTexImage();
    mSurfaceTexture.getTransformMatrix(mSurfaceMatrix);
    Matrix.multiplyMM(mTransformMatrix, 0, mSurfaceMatrix, 0, mProjectionMatrix, 0);
    mOpenGL.setTextureTransformMatrix(mTransformMatrix);
    mOpenGL.draw(mSurfaceTextureId);
    ByteBuffer byteBuffer = mOpenGL.getOutputPixels();

//    if (count < 5) {
//      saveFrame(byteBuffer);
//    }
//    ++count;

    mWindowSurface.swapBuffers();
  }

  private void saveFrame(ByteBuffer buf) {
    buf.position(0);
    BufferedOutputStream bos = null;
    try {
      try {
        bos = new BufferedOutputStream(new FileOutputStream("/sdcard/temp" + System.currentTimeMillis() + ".png"));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      Bitmap bmp = Bitmap.createBitmap(1280, 768, Bitmap.Config.ARGB_8888);
      bmp.copyPixelsFromBuffer(buf);

      bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
      bmp.recycle();
    } finally {
      if (bos != null) try {
        bos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
