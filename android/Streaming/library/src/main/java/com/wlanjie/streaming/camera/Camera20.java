package com.wlanjie.streaming.camera;

import java.util.Set;

/**
 * Created by wlanjie on 2017/5/21.
 */
public class Camera20 implements Camera {

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isCameraOpened() {
        return false;
    }

    @Override
    public void setFacing(int facing) {

    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        return null;
    }

    @Override
    public void setAspectRatio(AspectRatio ratio) {

    }

    @Override
    public AspectRatio getAspectRatio() {
        return null;
    }

    @Override
    public void setAutoFocus(boolean autoFocus) {

    }

    @Override
    public boolean getAutoFocus() {
        return false;
    }

    @Override
    public void setFlash(int flash) {

    }

    @Override
    public int getFlash() {
        return 0;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {

    }
}
