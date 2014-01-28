package com.jxhembulla.cameragl.opengl;

import com.jxhembulla.cameragl.colorpicker.ColorPickerDialog.OnColorChangedListener;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class MainView extends GLSurfaceView {
	private MainRenderer mRenderer;

	private int surfacewidth;

	private int surfaceheight;

	private class ColorChangeListener implements OnColorChangedListener {
		@Override
		public void colorChanged(int color) {
			// mRenderer.takePicture();
			mRenderer.filterColor(Color.red(color), Color.green(color),
					Color.blue(color));
		}
	}

	public ColorChangeListener colorListener;

	public MainView(Context context) {
		super(context);
		mRenderer = new MainRenderer(this);
		setEGLContextClientVersion(2);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		colorListener = new ColorChangeListener();
	}

	public MainView(Context context, AttributeSet attrs) {
		super(context);
		mRenderer = new MainRenderer(this);
		setEGLContextClientVersion(2);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		colorListener = new ColorChangeListener();
	}

	public void takePicture() {
		mRenderer.takePicture();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mRenderer.close();
		super.surfaceDestroyed(holder);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		surfacewidth = w;
		surfaceheight = h;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mRenderer.touchEvent(event.getX() / surfacewidth, event.getY()
				/ surfaceheight);
		return super.onTouchEvent(event);
	}

	public void setHandW(int previewSizeWidth, int previewSizeHeight) {
		mRenderer.setWH(previewSizeWidth, previewSizeHeight);
	}
}
