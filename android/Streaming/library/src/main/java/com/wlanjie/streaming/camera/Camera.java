package com.wlanjie.streaming.camera;

import android.graphics.SurfaceTexture;

import java.util.Set;

/**
 * Created by wlanjie on 2017/5/21.
 */
public interface Camera {

    void start(SurfaceTexture texture);

    void stop();

    boolean isCameraOpened();

    void setFacing(int facing);

    int getFacing();

    Set<AspectRatio> getSupportedAspectRatios();

    void setAspectRatio(AspectRatio ratio);

    AspectRatio getAspectRatio();

    void setAutoFocus(boolean autoFocus);

    boolean getAutoFocus();

    void setFlash(int flash);

    int getFlash();

    void setDisplayOrientation(int displayOrientation);
}
