package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;

import com.wlanjie.streaming.OpenGL;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/6/4.
 */
public class SoftSurfaceRenderer extends SurfaceRenderer implements SurfaceTexture.OnFrameAvailableListener {

  private final OpenGL mOpenGL = new OpenGL();
//  private final WindowInputSurface mWindowSurface;

  public SoftSurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    super(context, texture, surfaceTextureId);
//    mWindowSurface = new WindowInputSurface(texture);
//    texture.setOnFrameAvailableListener(this);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    super.onSurfaceChanged(gl, width, height);
    mOpenGL.init(width, height);
//    mWindowSurface.makeCurrent();
  }

  @Override
  protected int draw() {
    mOpenGL.setTextureTransformMatrix(mTransformMatrix);
    int textureId = mOpenGL.draw(mSurfaceTextureId, 0);
//    mWindowSurface.swapBuffers();
    return textureId;
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//    surfaceTexture.updateTexImage();
    Log.d("SoftSurfaceRenderer", "onFrameAvailable");
  }
}
