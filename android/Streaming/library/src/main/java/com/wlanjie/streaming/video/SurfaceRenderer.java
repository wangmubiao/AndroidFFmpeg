package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.wlanjie.streaming.callback.SurfaceTextureCallback;
import com.wlanjie.streaming.camera.Size;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/5/22.
 */

public abstract class SurfaceRenderer implements GLSurfaceView.Renderer {

  private SurfaceTexture mSurfaceTexture;
  int mSurfaceTextureId;
  private OnRendererEncoderListener mOnRendererEncoderListener;
  private float[] mProjectionMatrix = new float[16];
  private float[] mSurfaceMatrix = new float[16];
  float[] mTransformMatrix = new float[16];
  private List<SurfaceTextureCallback> mSurfaceTextureCallbacks = new ArrayList<>();
  private Context mContext;
  Size mPreviewSize;

  SurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    mContext = context;
    mSurfaceTexture = texture;
    mSurfaceTextureId = surfaceTextureId;
  }

  public void addSurfaceTextureCallback(SurfaceTextureCallback callback) {
    mSurfaceTextureCallbacks.add(callback);
  }

  public void setPreviewSize(Size size) {
    mPreviewSize = size;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    for (SurfaceTextureCallback callback : mSurfaceTextureCallbacks) {
      callback.onSurfaceCreated();
    }
    GLES20.glDisable(GLES20.GL_STENCIL_TEST);
    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    for (SurfaceTextureCallback callback : mSurfaceTextureCallbacks) {
      callback.onSurfaceChanged(width, height);
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
    for (SurfaceTextureCallback callback : mSurfaceTextureCallbacks) {
//      callback.onDrawFrame()
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

  public void setOnRendererEncoderListener(OnRendererEncoderListener l) {
    mOnRendererEncoderListener = l;
  }

  public interface OnRendererEncoderListener {
    void onRenderEncoder(int textureId);
  }
}
