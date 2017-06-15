package com.wlanjie.streaming.video;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

/**
 * 窗口输出表面
 */
public class WindowInputSurface {
  private static final int EGL_RECORDABLE_ANDROID = 0x3142;

  //gl设备
  private EGLDisplay mEGLDisplay            = EGL14.EGL_NO_DISPLAY;
  //gl环境
  private EGLContext mEGLContext            = EGL14.EGL_NO_CONTEXT;
  //gl表面
  private EGLSurface mEGLSurface            = EGL14.EGL_NO_SURFACE;

  //gl属性
  private static final int[] mAttribList    = {
    EGL14.EGL_RED_SIZE, 8,
    EGL14.EGL_GREEN_SIZE, 8,
    EGL14.EGL_BLUE_SIZE, 8,
    EGL14.EGL_ALPHA_SIZE, 8,
    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
    EGL_RECORDABLE_ANDROID, 1,
    EGL14.EGL_NONE
  };

  //配置context属性
  private static final int[] mContextAttlist = {
    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
    EGL14.EGL_NONE
  };

  //surface属性
  private static final int[] mSurfaceAttribs = {
    EGL14.EGL_NONE
  };

  private Surface    mSurface;
  private SurfaceTexture mSurfaceTexture;

  public WindowInputSurface(Surface mSurface) {
    this.mSurface = mSurface;
    init();
  }

  public WindowInputSurface(SurfaceTexture surfaceTexture) {
    this.mSurfaceTexture = surfaceTexture;
    init();
  }

  //初始化配置
  private final void init(){
    //创建设备
    mEGLDisplay         = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
      throw new RuntimeException("unable to get EGL14 display");
    }

    //初始化设备
    int[] version       = new int[2];
    if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
      throw new RuntimeException("unable to initialize EGL14");
    }

    //根据属性选择最优的配置
    EGLConfig[] configs = new EGLConfig[1];
    int[] numConfigs    = new int[1];
    EGL14.eglChooseConfig(mEGLDisplay, mAttribList, 0, configs, 0, configs.length,
      numConfigs, 0);

    checkEglError("eglCreateContext RGB888+recordable ES2");

    mEGLContext         = EGL14.eglCreateContext(
      mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
      mContextAttlist, 0);

    checkEglError("eglCreateContext");

    mEGLSurface         = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface == null ? mSurfaceTexture : mSurface,
      mSurfaceAttribs, 0);
    checkEglError("eglCreateWindowSurface");

  }

  /**
   * 准备当前渲染环境
   */
  public final void makeCurrent() {
    EGL14.eglMakeCurrent(mEGLDisplay,
      mEGLSurface, mEGLSurface, mEGLContext);
    checkEglError("eglMakeCurrent");
  }

  /**
   * 生产数据
   * @return
   */
  public final boolean swapBuffers() {
    boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    checkEglError("eglSwapBuffers");
    return result;
  }

  /**
   * 设置下一帧数据时间戳
   * @param nsecs
   */
  public final void setPresentationTime(long nsecs) {
    EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
    checkEglError("eglPresentationTimeANDROID");
  }

  /**
   * 释放资源
   */
  public final void release() {
    if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
      EGL14.eglMakeCurrent(mEGLDisplay,
        EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
        EGL14.EGL_NO_CONTEXT);
      EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
      EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
      EGL14.eglReleaseThread();
      EGL14.eglTerminate(mEGLDisplay);
    }
    mSurface.release();
    mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    mEGLContext = EGL14.EGL_NO_CONTEXT;
    mEGLSurface = EGL14.EGL_NO_SURFACE;
    mSurface = null;
  }

  //检查错误
  private final void checkEglError(String msg) {
    int error;
    if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
      throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
    }
  }
}
