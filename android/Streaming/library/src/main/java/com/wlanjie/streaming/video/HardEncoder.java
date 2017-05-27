package com.wlanjie.streaming.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;

import com.wlanjie.streaming.configuration.VideoConfiguration;
import com.wlanjie.streaming.rtmp.Rtmp;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wlanjie on 2017/5/25.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HardEncoder implements Encoder {

  private VideoConfiguration mVideoConfiguration;
  private MediaCodec mMediaCodec;
  private InputSurface mInputSurface;
  private HandlerThread mHandlerThread;
  private Handler mEncoderHandler;
  private MediaCodec.BufferInfo mBufferInfo;
  private boolean mIsStarted;
  private ReentrantLock mEncoderLock = new ReentrantLock();
  private Rtmp mRtmp = new Rtmp();

  public HardEncoder(VideoConfiguration configuration) {
    mVideoConfiguration = configuration;
  }

  @Override
  public void prepareEncoder() {
    if (mMediaCodec != null || mInputSurface != null) {
      throw new IllegalStateException("prepareEncoder already called.");
    }
    mMediaCodec = getVideoMediaCodec(mVideoConfiguration);
    mHandlerThread = new HandlerThread("HardEncoder");
    mHandlerThread.start();
    mEncoderHandler = new Handler(mHandlerThread.getLooper());
    mBufferInfo = new MediaCodec.BufferInfo();

    int result = mRtmp.connect("rtmp://www.ossrs.net:1935/live/demo");
    System.out.println("result = " + result);
    new Thread(){
      @Override
      public void run() {
        super.run();
        mRtmp.startPublish();
      }
    }.start();
  }

  @Override
  public boolean firstTimeSetup() {
    if (mMediaCodec == null || mInputSurface != null) {
      return false;
    }
    mInputSurface = new InputSurface(mMediaCodec.createInputSurface());
    mMediaCodec.start();
    return true;
  }

  @Override
  public void makeCurrent() {
    mInputSurface.makeCurrent();
  }

  @Override
  public void swapBuffers() {
    mInputSurface.swapBuffers();
    mInputSurface.setPresentationTime(System.nanoTime());
  }

  private MediaCodec getVideoMediaCodec(VideoConfiguration videoConfiguration) {
    int videoWidth = getVideoSize(videoConfiguration.width);
    int videoHeight = getVideoSize(videoConfiguration.height);
    MediaFormat format = MediaFormat.createVideoFormat(videoConfiguration.mime, videoWidth, videoHeight);
    format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
      MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    format.setInteger(MediaFormat.KEY_BIT_RATE, videoConfiguration.maxBps * 1024);
    int fps = videoConfiguration.fps;
    //设置摄像头预览帧率
//    if(BlackListHelper.deviceInFpsBlacklisted()) {
//      SopCastLog.d(SopCastConstant.TAG, "Device in fps setting black list, so set mediacodec fps 15");
//      fps = 15;
//    }
    format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfiguration.ifi);
    format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
    format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
    MediaCodec mediaCodec = null;

    try {
      mediaCodec = MediaCodec.createEncoderByType(videoConfiguration.mime);
      mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }catch (Exception e) {
      e.printStackTrace();
      if (mediaCodec != null) {
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
      }
    }
    return mediaCodec;
  }

  // We avoid the device-specific limitations on width and height by using values that
  // are multiples of 16, which all tested devices seem to be able to handle.
  private int getVideoSize(int size) {
    int multiple = (int)Math.ceil(size/16.0);
    return multiple*16;
  }

  @Override
  public void startEncoder() {
    mIsStarted = true;
    mEncoderHandler.post(mEncoderRunnable);
  }

  private Runnable mEncoderRunnable = new Runnable() {
    @Override
    public void run() {
      drainEncoder();
    }
  };

  private void drainEncoder() {
    ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();
    while (mIsStarted) {
      mEncoderLock.lock();

      int outBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
      if (outBufferIndex > 0) {
        ByteBuffer bb = outBuffers[outBufferIndex];
        bb.position(mBufferInfo.offset);
        bb.limit(mBufferInfo.offset + mBufferInfo.size);
        byte[] h264 = new byte[mBufferInfo.size];
        bb.get(h264, 0, mBufferInfo.size);
//        mRtmp.muxerH264(h264, mBufferInfo.size, (int) (mBufferInfo.presentationTimeUs / 1000));
        mRtmp.writeVideo(mBufferInfo.presentationTimeUs / 1000, h264);
        mMediaCodec.releaseOutputBuffer(outBufferIndex, false);
      } else {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      mEncoderLock.unlock();
    }
  }

  @Override
  public void releaseEncoder() {
    if (!mIsStarted) {
      return;
    }
    mIsStarted = false;
    mEncoderHandler.removeCallbacks(mEncoderRunnable);
    mHandlerThread.quit();
    mEncoderLock.lock();
    if (mMediaCodec != null) {
      mMediaCodec.signalEndOfInputStream();
      mMediaCodec.stop();
      mMediaCodec.release();
      mMediaCodec = null;
    }
    if (mInputSurface != null) {
      mInputSurface.release();
      mInputSurface = null;
    }
    mEncoderLock.unlock();
  }
}
