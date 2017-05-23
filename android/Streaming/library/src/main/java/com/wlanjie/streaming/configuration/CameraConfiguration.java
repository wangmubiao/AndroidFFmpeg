package com.wlanjie.streaming.configuration;

import com.wlanjie.streaming.camera.CameraCallback;

/**
 * Created by wlanjie on 2017/5/23.
 */
public class CameraConfiguration {

  private static final int DEFAULT_HEIGHT = 1280;
  private static final int DEFAULT_WIDTH = 720;
  private static final int DEFAULT_FPS = 15;
  private static final Facing DEFAULT_FACING = Facing.FRONT;
  private static final Orientation DEFAULT_ORIENTATION = Orientation.PORTRAIT;
  private static final FocusMode DEFAULT_FOCUSMODE = FocusMode.AUTO;

  public enum  Facing {
    FRONT,
    BACK
  }

  public enum  Orientation {
    LANDSCAPE,
    PORTRAIT
  }

  public enum  FocusMode {
    AUTO,
    TOUCH
  }

  public final int height;
  public final int width;
  public final int fps;
  public final Facing facing;
  public final Orientation orientation;
  public final FocusMode focusMode;
  public final int screenRotation;
  public final CameraCallback cameraCallback;

  private CameraConfiguration(final Builder builder) {
    height = builder.height;
    width = builder.width;
    facing = builder.facing;
    fps = builder.fps;
    orientation = builder.orientation;
    focusMode = builder.focusMode;
    screenRotation = builder.screenRotation;
    cameraCallback = builder.cameraCallback;
  }

  public static CameraConfiguration createDefault() {
    return new Builder().build();
  }

  public static class Builder {
    private int height = DEFAULT_HEIGHT;
    private int width = DEFAULT_WIDTH;
    private int fps = DEFAULT_FPS;
    private int screenRotation = 90;
    private Facing facing = DEFAULT_FACING;
    private Orientation orientation = DEFAULT_ORIENTATION;
    private FocusMode focusMode = DEFAULT_FOCUSMODE;
    private CameraCallback cameraCallback;

    public Builder setPreview(int height, int width) {
      this.height = height;
      this.width = width;
      return this;
    }

    public Builder setFacing(Facing facing) {
      this.facing = facing;
      return this;
    }

    public Builder setOrientation(Orientation orientation) {
      this.orientation = orientation;
      return this;
    }

    public Builder setFps(int fps) {
      this.fps = fps;
      return this;
    }

    public Builder setFocusMode(FocusMode focusMode) {
      this.focusMode = focusMode;
      return this;
    }

    public Builder setScreenRotation(int screenRotation) {
      this.screenRotation = screenRotation;
      return this;
    }

    public Builder setCameraCallback(CameraCallback callback) {
      this.cameraCallback = callback;
      return this;
    }

    public CameraConfiguration build() {
      return new CameraConfiguration(this);
    }
  }
}
