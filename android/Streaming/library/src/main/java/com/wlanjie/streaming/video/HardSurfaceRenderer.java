package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.wlanjie.streaming.camera.Effect;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/6/4.
 */
public class HardSurfaceRenderer extends SurfaceRenderer {

  private final Effect mEffect;
  private final RendererScreen mRendererScreen;

  public HardSurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    super(context, texture, surfaceTextureId);
    mEffect = new Effect(context);
    mRendererScreen = new RendererScreen(context);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    super.onSurfaceCreated(gl, config);
    mEffect.init();
    mRendererScreen.init();
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    super.onSurfaceChanged(gl, width, height);
    mEffect.onInputSizeChanged(width, height);
    mEffect.onDisplaySizeChange(width, height);
  }

  @Override
  protected int draw() {
    mEffect.setTextureTransformMatrix(mTransformMatrix);
    int textureId = mEffect.draw(mSurfaceTextureId);
    mRendererScreen.draw(textureId);
    return textureId;
  }
}
