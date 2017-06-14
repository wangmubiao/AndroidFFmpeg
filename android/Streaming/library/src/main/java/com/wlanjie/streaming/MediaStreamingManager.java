package com.wlanjie.streaming;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;

import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.setting.AudioSetting;
import com.wlanjie.streaming.setting.CameraSetting;
import com.wlanjie.streaming.setting.StreamingSetting;
import com.wlanjie.streaming.video.Encoder;

/**
 * Created by wlanjie on 2017/6/14.
 */
public class MediaStreamingManager {

  private final GLSurfaceView mGLSurfaceView;
  private final Context mContext;
  private CameraSetting mCameraSetting;
  private AudioSetting mAudioSetting;
  private StreamingSetting mStreamingSetting;
  private LivingCamera mCamera;
  private Encoder mEncoder;

  public MediaStreamingManager(GLSurfaceView surfaceView) {
    mGLSurfaceView = surfaceView;
    mContext = surfaceView.getContext();
  }

  /**
   * prepare setting
   * @param cameraSetting
   * @param audioSetting
   * @param streamingSetting
   */
  public void prepare(CameraSetting cameraSetting, AudioSetting audioSetting, StreamingSetting streamingSetting) {
    mCameraSetting = cameraSetting;
    mAudioSetting = audioSetting;
    mStreamingSetting = streamingSetting;

    if (Build.VERSION.SDK_INT < 21) {
      mCamera = new Camera9(configuration);
    } else if (Build.VERSION.SDK_INT < 23) {
      mCamera = new Camera21(mContext, configuration);
    } else {
      mCamera = new Camera21(mContext, configuration);
    }
  }

  /**
   * open camera
   */
  public void resume() {

  }

  /**
   * stop camera
   */
  public void pause() {

  }

  /**
   * release resource
   */
  public void destroy() {

  }

  /**
   * switch camera.
   * @param facingId camera id
   */
  public void switchCamera(int facingId) {

  }

  /**
   * start publish rtmp stream.
   */
  public void startStreaming() {

  }

  /**
   * stop publish rtmp stream.
   */
  public void stopStreaming() {

  }
}
