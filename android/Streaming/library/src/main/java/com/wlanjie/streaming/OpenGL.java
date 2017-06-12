package com.wlanjie.streaming;

import java.nio.ByteBuffer;

/**
 * Created by wlanjie on 2017/6/1.
 */

public class OpenGL {

  public native void init(int width, int height);

  public native int draw(int inputTextureId, int time);

  public native void setInputTexture(int textureId);

  public native void setInputPixels(byte[] pixels);

  public native ByteBuffer getOutputPixels();

  public native void setTextureTransformMatrix(float[] matrix);

  public native void release();
}
