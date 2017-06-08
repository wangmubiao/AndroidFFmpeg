package com.wlanjie.streaming.video;

import android.media.MediaCodec;

import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.rtmp.Rtmp;

import java.nio.ByteBuffer;

/**
 * Created by wlanjie on 2017/5/28.
 */
public class VideoController implements OnVideoEncoderListener {

  private final Rtmp mRtmp;
  private OnVideoEncoderListener mOnVideoEncoderListener;
  private Encoder mEncoder;

  public VideoController() {
    mRtmp = new Rtmp();
  }

  public void setOnVideoEncoderListener(OnVideoEncoderListener l) {
    mOnVideoEncoderListener = l;
  }

  public void setVideoConfiguration(VideoConfiguration configuration) {
    mEncoder = configuration.encoder;
    mEncoder.setVideoConfiguration(configuration);
    mEncoder.prepareEncoder();
    mEncoder.startEncoder();
    mEncoder.setOnVideoEncoderListener(this);
  }

  public void start(final String rtmpUrl) {
    mRtmp.connect(rtmpUrl);
    new Thread(){
      @Override
      public void run() {
        super.run();
        mRtmp.startPublish();
      }
    }.start();
//    mEncoder.prepareEncoder();
//    mEncoder.startEncoder();
  }

  public void stop() {

  }

  @Override
  public void onVideoEncode(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
    if (mOnVideoEncoderListener != null) {
      mOnVideoEncoderListener.onVideoEncode(buffer, bufferInfo);
    }
    buffer.position(bufferInfo.offset);
    buffer.limit(bufferInfo.offset + bufferInfo.size);
    byte[] h264 = new byte[bufferInfo.size];
    buffer.get(h264, 0, bufferInfo.size);
    mRtmp.writeVideo(bufferInfo.presentationTimeUs / 1000, h264);
  }
}
