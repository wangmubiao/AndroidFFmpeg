package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/5/22.
 */

public abstract class SurfaceRenderer implements GLSurfaceView.Renderer {

  private SurfaceTexture mSurfaceTexture;
  int mSurfaceTextureId;
  private OnSurfaceListener mOnSurfaceListener;
  private OnRendererEncoderListener mOnRendererEncoderListener;

  private float[] mProjectionMatrix = new float[16];

  private float[] mSurfaceMatrix = new float[16];

  float[] mTransformMatrix = new float[16];


  public SurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
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
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    if (mOnSurfaceListener != null) {
      mOnSurfaceListener.onSurfaceChanged(gl, width, height);
    }

    GLES20.glViewport(0, 0, width, height);

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

    int textureId = draw();

    if (mOnRendererEncoderListener != null) {
      mOnRendererEncoderListener.onRenderEncoder(textureId);
    }
  }

  protected abstract int draw();

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
