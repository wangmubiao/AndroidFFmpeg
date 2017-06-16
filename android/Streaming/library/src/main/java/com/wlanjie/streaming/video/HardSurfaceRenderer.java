package com.wlanjie.streaming.video;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.wlanjie.streaming.camera.Effect;
import com.wlanjie.streaming.camera.Size;
import com.wlanjie.streaming.utils.OpenGLUtils;
import com.wlanjie.streaming.utils.Rotation;
import com.wlanjie.streaming.utils.TextureRotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/6/4.
 */
public class HardSurfaceRenderer extends SurfaceRenderer {

  private final Effect mEffect;
  private final RendererScreen mRendererScreen;
  private Rotation mRotation = Rotation.ROTATION_90;

  public HardSurfaceRenderer(Context context, SurfaceTexture texture, int surfaceTextureId) {
    super(context, texture, surfaceTextureId);
    mEffect = new Effect(context);
    mRendererScreen = new RendererScreen(context);
  }

  @Override
  public void setPreviewSize(Size size) {
    super.setPreviewSize(size);
    if (size != null) {

    }
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
//    mEffect.onInputSizeChanged(1280, 720);
    mEffect.onDisplaySizeChange(width, height);
    mRendererScreen.setDisplaySize(width, height);

    mEffect.onInputSizeChanged(mPreviewSize.getWidth(), mPreviewSize.getHeight());
    mRendererScreen.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//    adjustImageScaling(width, height, 1024, 768);
  }

  private void adjustImageScaling(int displayWidth, int displayHeight, int imageWidth, int imageHeight) {
    float outputWidth = displayWidth;
    float outputHeight = displayHeight;
    if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
      outputWidth = displayHeight;
      outputHeight = displayWidth;
    }
    float ratioWidth = outputWidth / imageWidth;
    float ratioHeight = outputHeight / imageHeight;
    float ratioMax = Math.max(ratioWidth, ratioHeight);
    int imageWidthNew = Math.round(imageWidth * ratioMax);
    int imageHeightNew = Math.round(imageHeight * ratioMax);
    float scaleWidth = imageWidthNew / outputWidth;
    float scaleHeight = imageHeightNew / outputHeight;

    float distHorizontal = (1 - 1 / scaleWidth) / 2;
    float distVertical = (1 - 1 / scaleHeight) / 2;

    float[] texture = TextureRotationUtil.getRotation(mRotation, false, true);
    float[] textureCords = new float[] {
      addDistance(texture[0], distHorizontal), addDistance(texture[1], distVertical),
      addDistance(texture[2], distHorizontal), addDistance(texture[3], distVertical),
      addDistance(texture[4], distHorizontal), addDistance(texture[5], distVertical),
      addDistance(texture[6], distHorizontal), addDistance(texture[7], distVertical),
    };
    mEffect.updateTextureCoordinate(textureCords);
    mRendererScreen.updateTextureCoordinate(textureCords);
  }

  private float addDistance(float coordinate, float distance) {
    return coordinate == 0.0f ? distance : 1 - distance;
  }

  @Override
  protected int draw() {
    mEffect.setTextureTransformMatrix(mTransformMatrix);
    int textureId = mEffect.draw(mSurfaceTextureId);
    mRendererScreen.draw(textureId);
    return textureId;
  }
}
