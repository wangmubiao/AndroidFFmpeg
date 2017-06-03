package com.wlanjie.streaming;

import java.nio.ByteBuffer;

/**
 * Created by wlanjie on 2017/6/1.
 */

public class OpenGL {

  public native void init(int width, int height);

  public native void draw(int inputTextureId);

  public native void setInputTexture(int textureId);

  public native void setInputPixels(byte[] pixels);

  public native ByteBuffer getOutputPixels();

  public native void release();
}
