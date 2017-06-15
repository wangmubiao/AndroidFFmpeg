package com.wlanjie.streaming.callback;

import com.wlanjie.streaming.camera.Size;

import java.util.List;

/**
 * Created by caowu15 on 2017/6/15.
 */

public interface CameraCallback {

  void onOpenCamera();

  void onCloseCamera();

  Size onPreviewSizeSelected(List<Size> sizes);
}
