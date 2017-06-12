package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.wlanjie.streaming.OpenGL;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/6/4.
 */
public class SoftSurfaceRenderer extends SurfaceRenderer {

  private final OpenGL mOpenGL = new OpenGL();

  public SoftSurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    super(context, texture, surfaceTextureId);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    super.onSurfaceChanged(gl, width, height);
    mOpenGL.init(width, height);
  }

  @Override
  protected int draw() {
    mOpenGL.setTextureTransformMatrix(mTransformMatrix);
    int textureId = mOpenGL.draw(mSurfaceTextureId, 0);
    return textureId;
  }
}
