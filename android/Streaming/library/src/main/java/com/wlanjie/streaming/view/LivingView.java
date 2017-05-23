package com.wlanjie.streaming.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by wlanjie on 2017/5/23.
 */
public class LivingView extends FrameLayout {

  private RendererSurfaceView mRendererSurfaceView;

  public LivingView(@NonNull Context context) {
    this(context, null);
  }

  public LivingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LivingView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    mRendererSurfaceView = new RendererSurfaceView(context);
    addView(mRendererSurfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }
}
