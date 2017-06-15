package com.wlanjie.streaming.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.M)
class Camera23 extends Camera2 {

    Camera23(CameraCallback callback, Context context) {
        super(callback, context);
    }

    @Override
    protected void collectPictureSizes(SizeMap sizes, StreamConfigurationMap map) {
        // Try to get hi-res output sizes
        android.util.Size[] outputSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG);
        if (outputSizes != null) {
            for (android.util.Size size : map.getHighResolutionOutputSizes(ImageFormat.JPEG)) {
                sizes.add(new Size(size.getWidth(), size.getHeight()));
            }
        }
        if (sizes.isEmpty()) {
            super.collectPictureSizes(sizes, map);
        }
    }

}
