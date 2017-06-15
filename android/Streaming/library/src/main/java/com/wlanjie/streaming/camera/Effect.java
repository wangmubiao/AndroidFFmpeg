package com.wlanjie.streaming.camera;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wlanjie.streaming.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wlanjie on 2016/12/12.
 */

public final class Effect {

  private static final float TEXTURE_NO_ROTATION[] = {
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
  };

  private static final float TEXTURE_ROTATED_90[] = {
    1.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    0.0f, 0.0f
  };

  private static final float TEXTURE_ROTATED_180[] = {
    1.0f, 0.0f,
    0.0f, 0.0f,
    1.0f, 1.0f,
    0.0f, 1.0f
  };

  private static final float TEXTURE_ROTATED_270[] = {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 0.0f,
    1.0f, 1.0f
  };

  private static final float CUBE[] = {
    -1.0f, -1.0f,
    1.0f, -1.0f,
    -1.0f, 1.0f,
    1.0f, 1.0f
  };

  private int mInputWidth;
  private int mInputHeight;

  private int mDisplayWidth;
  private int mDisplayHeight;

  private int[] mFboId;
  private int[] mFboTextureId;

  private int[] mCubeId;
  private final FloatBuffer mCubeBuffer;
  private final FloatBuffer mTextureBuffer;

  private int mProgramId;
  private int mPosition;
  private int mUniformTexture;
  private int mTextureCoordinate;
  private int mTextureTransform;
  private float[] mTextureTransformMatrix;

  private final Resources mResources;

  public Effect(Context context) {
    this.mResources = context.getResources();
    mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer();
    mCubeBuffer.put(CUBE).position(0);

    mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer();
    mTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
  }

  public void init() {
    onInit();
  }

  public void onInputSizeChanged(int width, int height) {
    this.mInputWidth = width;
    this.mInputHeight = height;
    initFboTexture(width, height);
  }

  public void onDisplaySizeChange(int width, int height) {
    mDisplayWidth = width;
    mDisplayHeight = height;
  }

  private void onInit() {
    mProgramId = OpenGLUtils.loadProgram(OpenGLUtils.readSharedFromRawResource(mResources, R.raw.vertex_oes), OpenGLUtils.readSharedFromRawResource(mResources, R.raw.fragment_oes));
    mPosition = GLES20.glGetAttribLocation(mProgramId, "position");
    mUniformTexture = GLES20.glGetUniformLocation(mProgramId, "inputImageTexture");
    mTextureCoordinate = GLES20.glGetAttribLocation(mProgramId, "inputTextureCoordinate");
    mTextureTransform = GLES20.glGetUniformLocation(mProgramId, "textureTransform");
  }

  private void destroyVbo() {
    if (mCubeId != null) {
      GLES20.glDeleteBuffers(1, mCubeId, 0);
      mCubeId = null;
    }
  }

  private void initFboTexture(int width, int height) {
    if (mFboId != null && width != mInputWidth && height != mInputHeight) {
      destroyFboTexture();
    }
    mFboId = new int[1];
    mFboTextureId = new int[1];

    GLES20.glGenFramebuffers(1, mFboId, 0);
    GLES20.glGenTextures(1, mFboTextureId, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboTextureId[0]);
    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFboTextureId[0], 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
  }

  private void destroyFboTexture() {
    if (mFboId != null) {
      GLES20.glDeleteFramebuffers(1, mFboId, 0);
    }
    if (mFboTextureId != null) {
      GLES20.glDeleteTextures(1, mFboTextureId, 0);
    }
  }

  public int draw(int textureId) {
    GLES20.glUseProgram(mProgramId);

    GLES20.glEnableVertexAttribArray(mPosition);
    GLES20.glVertexAttribPointer(mPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, mCubeBuffer);

    GLES20.glEnableVertexAttribArray(mTextureCoordinate);
    GLES20.glVertexAttribPointer(mTextureCoordinate, 2, GLES20.GL_FLOAT, false, 4 * 2, mTextureBuffer);

    GLES20.glUniformMatrix4fv(mTextureTransform, 1, false, mTextureTransformMatrix, 0);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
    GLES20.glUniform1i(mUniformTexture, 0);

    GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    GLES20.glDisableVertexAttribArray(mPosition);
    GLES20.glDisableVertexAttribArray(mTextureCoordinate);

    return mFboTextureId[0];
  }

  public void setTextureTransformMatrix(float[] matrix) {
    mTextureTransformMatrix = matrix;
  }

  final void destroy() {
    destroyFboTexture();
    destroyVbo();
    GLES20.glDeleteProgram(mProgramId);
  }

  public void updateTextureCoordinate(float[] textureCords) {
    mTextureBuffer.clear();
    mTextureBuffer.put(textureCords).position(0);
  }
}
