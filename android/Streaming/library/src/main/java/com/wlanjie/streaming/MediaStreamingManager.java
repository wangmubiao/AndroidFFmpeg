package com.wlanjie.streaming;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.SurfaceHolder;

import com.wlanjie.streaming.callback.CameraCallback;
import com.wlanjie.streaming.callback.SurfaceTextureCallback;
import com.wlanjie.streaming.camera.Camera21;
import com.wlanjie.streaming.camera.Camera9;
import com.wlanjie.streaming.camera.CameraRender;
import com.wlanjie.streaming.camera.LivingCamera;
import com.wlanjie.streaming.setting.AudioSetting;
import com.wlanjie.streaming.setting.CameraSetting;
import com.wlanjie.streaming.setting.StreamingSetting;
import com.wlanjie.streaming.video.Encoder;
import com.wlanjie.streaming.video.HardSurfaceRenderer;
import com.wlanjie.streaming.video.SoftSurfaceRenderer;
import com.wlanjie.streaming.video.SurfaceRenderer;

/**
 * Created by wlanjie on 2017/6/14.
 */
public class MediaStreamingManager {

  private CameraCallback mCameraCallback;
  private SurfaceTextureCallback mSurfaceTextureCallback;
  private final GLSurfaceView mGLSurfaceView;
  private final Context mContext;
  private CameraSetting mCameraSetting;
  private AudioSetting mAudioSetting;
  private StreamingSetting mStreamingSetting;
  private LivingCamera mCamera;
  private Encoder mEncoder;
  private int mSurfaceTextureId;
  private SurfaceTexture mSurfaceTexture;
  private SurfaceRenderer mRenderer;
  private CameraRender mCameraRenderer;

  public MediaStreamingManager(GLSurfaceView surfaceView) {
    mGLSurfaceView = surfaceView;
    mContext = surfaceView.getContext();
    mCameraRenderer = new CameraRender(surfaceView);
  }

  /**
   * prepare setting
   * @param cameraSetting camera setting parameters
   * @param audioSetting audio setting parameters
   * @param streamingSetting stream setting parameters
   */
  public void prepare(CameraSetting cameraSetting, AudioSetting audioSetting, StreamingSetting streamingSetting) {
    mCameraSetting = cameraSetting;
    mAudioSetting = audioSetting;
    mStreamingSetting = streamingSetting;

    mCameraRenderer.prepare(cameraSetting);
  }

  /**
   * open camera
   */
  public void resume() {
    mCameraRenderer.resume();
  }

  /**
   * stop camera
   */
  public void pause() {
    mCameraRenderer.pause();
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
  public void switchCamera(CameraSetting.CameraFacingId facingId) {
    mCamera.setFacing(facingId);
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

  public void setCameraCallback(CameraCallback cameraCallback) {
     mCameraCallback = cameraCallback;
  }

  public void setSurfaceTextureCallback(SurfaceTextureCallback callback) {
    mSurfaceTextureCallback = callback;
  }

  static {
    System.loadLibrary("wlanjie");
  }
}
