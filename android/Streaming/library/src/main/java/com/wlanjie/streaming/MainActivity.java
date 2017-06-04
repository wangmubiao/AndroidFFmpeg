package com.wlanjie.streaming;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.wlanjie.streaming.camera.CameraView;
import com.wlanjie.streaming.configuration.CameraConfiguration;
import com.wlanjie.streaming.configuration.VideoConfiguration;

public class MainActivity extends AppCompatActivity {

  private CameraView mCameraView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }

    mCameraView = (CameraView) findViewById(R.id.surface_view);
    mCameraView.setVideoConfiguration(VideoConfiguration.createDefault());
    mCameraView.setCameraConfiguration(CameraConfiguration.createDefault());
    mCameraView.start("rtmp://192.168.1.102/live/livestream");
  }
}
