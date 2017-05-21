package com.wlanjie.streaming.video;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.wlanjie.streaming.camera.CameraCallback;
import com.wlanjie.streaming.camera.CameraViewImpl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wlanjie on 2017/5/20.
 */
public class SurfaceRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private CameraViewImpl mCameraView;
    private SurfaceTexture mSurfaceTexture;
    private int mSurfaceTextureId;
    private CameraCallback mCameraCallback;

    public SurfaceRenderer(CameraCallback callback) {
        mCameraCallback = callback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);

        initSurfaceTexture();
        mCameraView.setPreviewSurface(mSurfaceTexture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        mCameraView.start();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    private void initSurfaceTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}
