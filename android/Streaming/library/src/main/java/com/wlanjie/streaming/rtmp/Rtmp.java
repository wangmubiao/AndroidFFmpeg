package com.wlanjie.streaming.rtmp;

/**
 * Created by caowu15 on 2017/5/27.
 */

public class Rtmp {

  public native void startPublish();

  /**
   * connect rtmp server
   * @param url rtmp url
   * @return 0 is success, other failed.
   */
  public native int connect(String url);

  /**
   * publish audio to rtmp server
   * @param timestamp pts
   * @param data aac data
   * @param sampleRate aac sample rate
   * @param channel aac channel
   * @return 0 is success, other failed.
   */
  public native int writeAudio(long timestamp, byte[] data, int sampleRate, int channel);

  /**
   * publish video to rtmp server
   * @param timestamp pts
   * @param data h264 data
   * @return 0 is success, other failed.
   */
  public native int writeVideo(long timestamp, byte[] data);

  /**
   * destroy rtmp resources {@link #connect(String url)}
   */
  public native void destroy();
}
