package com.wlanjie.streaming.video;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.wlanjie.streaming.R;
import com.wlanjie.streaming.camera.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL11;

/**
 * Created by wlanjie on 2017/5/27.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class RendererEncoder {

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

  private final FloatBuffer mCubeBuffer;
  private final FloatBuffer mTextureBuffer;

  // screen
  private int mScreenProgramId;
  private int mScreenPosition;
  private int mScreenUniformTexture;
  private int mScreenTextureCoordinate;
  private Resources mResources;

  private Encoder mEncoder;

  private EGLDisplay mSavedEglDisplay     = null;
  private EGLSurface mSavedEglDrawSurface = null;
  private EGLSurface mSavedEglReadSurface = null;
  private EGLContext mSavedEglContext     = null;

  public RendererEncoder(Context context, Encoder encoder) {
    mEncoder = encoder;
    mResources = context.getResources();
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
    mScreenProgramId = OpenGLUtils.loadProgram(OpenGLUtils.readSharedFromRawResource(mResources, R.raw.vertex_default), OpenGLUtils.readSharedFromRawResource(mResources, R.raw.fragment_default));
    mScreenPosition = GLES20.glGetAttribLocation(mScreenProgramId, "position");
    mScreenUniformTexture = GLES20.glGetUniformLocation(mScreenProgramId, "inputImageTexture");
    mScreenTextureCoordinate = GLES20.glGetAttribLocation(mScreenProgramId, "inputTextureCoordinate");

  }

  public void draw(int textureId) {
    saveRenderState();
    if (mEncoder.firstTimeSetup()) {
      mEncoder.startEncoder();
      mEncoder.makeCurrent();
      init();
    } else {
      mEncoder.makeCurrent();
    }
    GLES20.glUseProgram(mScreenProgramId);

    GLES20.glEnableVertexAttribArray(mScreenPosition);
    GLES20.glVertexAttribPointer(mScreenPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, mCubeBuffer);

    GLES20.glEnableVertexAttribArray(mScreenTextureCoordinate);
    GLES20.glVertexAttribPointer(mScreenTextureCoordinate, 2, GLES20.GL_FLOAT, false, 4 * 2, mTextureBuffer);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    GLES20.glUniform1i(mScreenUniformTexture, 0);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    GLES20.glDisableVertexAttribArray(mScreenPosition);
    GLES20.glDisableVertexAttribArray(mScreenTextureCoordinate);

    mEncoder.swapBuffers();
    restoreRenderState();
  }

  private void saveRenderState() {
    mSavedEglDisplay     = EGL14.eglGetCurrentDisplay();
    mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
    mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
    mSavedEglContext     = EGL14.eglGetCurrentContext();
  }

  private void restoreRenderState() {
    if (!EGL14.eglMakeCurrent(
      mSavedEglDisplay,
      mSavedEglDrawSurface,
      mSavedEglReadSurface,
      mSavedEglContext)) {
      throw new RuntimeException("eglMakeCurrent failed");
    }
  }
}