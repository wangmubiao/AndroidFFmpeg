package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.wlanjie.streaming.OpenGL;
import com.wlanjie.streaming.camera.Effect;
import com.wlanjie.streaming.configuration.VideoConfiguration;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by caowu15 on 2017/5/22.
 */

public class SurfaceRenderer implements GLSurfaceView.Renderer {

  private SurfaceTexture mSurfaceTexture;
  private int mSurfaceTextureId;
  private final Effect mEffect;
  private OnSurfaceListener mOnSurfaceListener;
  private OnRendererEncoderListener mOnRendererEncoderListener;

  private float[] mProjectionMatrix = new float[16];

  private float[] mSurfaceMatrix = new float[16];

  private float[] mTransformMatrix = new float[16];

  private final RendererScreen mRendererScreen;

  private OpenGL mOpenGL = new OpenGL();

  private EglCore mEglCore = new EglCore();

  private WindowSurface mWindowSurface;

  public SurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    mRendererScreen = new RendererScreen(context);
    mEffect = new Effect(context);
    mSurfaceTexture = texture;
    mSurfaceTextureId = surfaceTextureId;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    if (mOnSurfaceListener != null) {
      mOnSurfaceListener.onSurfaceCreated(gl, config);
    }
    GLES20.glDisable(GLES20.GL_STENCIL_TEST);
    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    mEffect.init();
    mRendererScreen.init();
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    if (mOnSurfaceListener != null) {
      mOnSurfaceListener.onSurfaceChanged(gl, width, height);
    }

//    mWindowSurface = new WindowSurface(mEglCore, mSurfaceTexture);
//    mWindowSurface.makeCurrent();

    mOpenGL.init(width, height);
    mEffect.onInputSizeChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
    mEffect.onDisplaySizeChange(width, height);

    float outputAspectRatio = width > height ? (float) width / height : (float) height / width;
    float aspectRatio = outputAspectRatio / outputAspectRatio;
    if (width > height) {
      Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f);
    } else {
      Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
    }
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    Log.d("Renderer", "onDrawFrame");
    if (mOnSurfaceListener != null) {
      mOnSurfaceListener.onDrawFrame(gl);
    }
    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    mSurfaceTexture.updateTexImage();

    mSurfaceTexture.getTransformMatrix(mSurfaceMatrix);
    Matrix.multiplyMM(mTransformMatrix, 0, mSurfaceMatrix, 0, mProjectionMatrix, 0);
    mEffect.setTextureTransformMatrix(mTransformMatrix);
//    int textureId = mEffect.draw(mSurfaceTextureId);
//    mRendererScreen.draw(textureId);

//    mOpenGL.setInputTexture(mSurfaceTextureId);
    mOpenGL.draw(mSurfaceTextureId);
//    mWindowSurface.swapBuffers();

//    if (mOnRendererEncoderListener != null) {
//      mOnRendererEncoderListener.onRenderEncoder(textureId);
//    }
  }

  public void setOnSurfaceListener(OnSurfaceListener l) {
    mOnSurfaceListener = l;
  }

  public void setOnRendererEncoderListener(OnRendererEncoderListener l) {
    mOnRendererEncoderListener = l;
  }

  public interface OnSurfaceListener {
    void onSurfaceCreated(GL10 gl, EGLConfig config);

    void onSurfaceChanged(GL10 gl, int width, int height);

    void onDrawFrame(GL10 gl);
  }

  public interface OnRendererEncoderListener {
    void onRenderEncoder(int textureId);
  }
}
