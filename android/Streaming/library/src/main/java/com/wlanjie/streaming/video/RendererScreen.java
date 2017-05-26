package com.wlanjie.streaming.video;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import com.wlanjie.streaming.R;
import com.wlanjie.streaming.camera.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wlanjie on 2017/5/25.
 */

public class RendererScreen {

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

  private int[] mCubeId;
  private final FloatBuffer mCubeBuffer;
  private int[] mTextureCoordinatedId;
  private final FloatBuffer mTextureBuffer;

  // screen
  private int mScreenProgramId;
  private int mScreenPosition;
  private int mScreenUniformTexture;
  private int mScreenTextureCoordinate;
  private Resources mResources;

  public RendererScreen(Context context) {
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

    initVbo();
  }

  private void initVbo() {
    mCubeId = new int[1];
    mTextureCoordinatedId = new int[1];

    GLES20.glGenBuffers(1, mCubeId, 0);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeId[0]);
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mCubeBuffer.capacity() * 4, mCubeBuffer, GLES20.GL_STATIC_DRAW);

    GLES20.glGenBuffers(1, mTextureCoordinatedId, 0);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinatedId[0]);
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextureBuffer.capacity() * 4, mTextureBuffer, GLES20.GL_STATIC_DRAW);;
  }

  public void draw(int textureId) {
    GLES20.glUseProgram(mScreenProgramId);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeId[0]);
    GLES20.glEnableVertexAttribArray(mScreenPosition);
    GLES20.glVertexAttribPointer(mScreenPosition, 2, GLES20.GL_FLOAT, false, 4 * 2, 0);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextureCoordinatedId[0]);
    GLES20.glEnableVertexAttribArray(mScreenTextureCoordinate);
    GLES20.glVertexAttribPointer(mScreenTextureCoordinate, 2, GLES20.GL_FLOAT, false, 4 * 2, 0);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    GLES20.glUniform1i(mScreenUniformTexture, 0);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    GLES20.glDisableVertexAttribArray(mScreenPosition);
    GLES20.glDisableVertexAttribArray(mScreenTextureCoordinate);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
  }
}
