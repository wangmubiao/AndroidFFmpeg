package com.wlanjie.streaming.video;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * Created by wlanjie on 2017/5/28.
 */
public interface OnVideoEncoderListener {
  void onVideoEncode(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);
}
