package com.jxhembulla.cameragl.ndk;

import java.io.IOException;

import com.jxhembulla.cameragl.colorpicker.ColorPickerDialog.OnColorChangedListener;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

public class CameraPreview implements SurfaceHolder.Callback,
		Camera.PreviewCallback {
	private Camera mCamera = null;
	private ImageView MyCameraPreview = null;
	private Bitmap bitmap = null;
	private int[] pixels = null;
	private byte[] FrameData = null;
	private byte[] tmpData = null;
	private int imageFormat;
	private int PreviewSizeWidth;
	private int PreviewSizeHeight;
	private boolean bProcessing = false;

	private int mR = 20, mG = 220, mB = 20;

	Handler mHandler = new Handler(Looper.getMainLooper());

	private class ColorChangeListener implements OnColorChangedListener {
		@Override
		public void colorChanged(int color) {
			// CameraPreview.this.takePicture();
			CameraPreview.this.filterColor(Color.red(color),
					Color.green(color), Color.blue(color));
		}
	}

	public ColorChangeListener colorListener;

	public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
			ImageView CameraPreview) {
		PreviewSizeWidth = PreviewlayoutWidth;
		PreviewSizeHeight = PreviewlayoutHeight;
		MyCameraPreview = CameraPreview;
		bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight,
				Bitmap.Config.ARGB_8888);
		pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
		colorListener = new ColorChangeListener();
	}

	public void filterColor(int red, int green, int blue) {
		mR = red;
		mB = blue;
		mG = green;
	}

	@Override
	public void onPreviewFrame(byte[] FrameData, Camera arg1) {
		// At preview mode, the frame data will push to here.
		if (imageFormat == ImageFormat.NV21) {
			// We only accept the NV21(YUV420) format.
			if (!bProcessing) {
				tmpData = FrameData;
				mHandler.post(DoImageProcessing);
			}
		}
		mCamera.addCallbackBuffer(FrameData);
		mCamera.setPreviewCallbackWithBuffer(this);
	}

	public void onPause() {
		mCamera.stopPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Parameters parameters;

		parameters = mCamera.getParameters();
		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

		imageFormat = parameters.getPreviewFormat();

		mCamera.setParameters(parameters);
		FrameData = new byte[PreviewSizeHeight * PreviewSizeWidth
				* ImageFormat.getBitsPerPixel(imageFormat)];

		mCamera.addCallbackBuffer(FrameData);
		mCamera.setPreviewCallbackWithBuffer(this);
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mCamera = Camera.open();
		try {
			// If did not set the SurfaceHolder, the preview area will be black.
			mCamera.setPreviewDisplay(arg0);
			mCamera.addCallbackBuffer(FrameData);
			mCamera.setPreviewCallbackWithBuffer(this);
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	//
	// Native JNI
	//
	public native boolean ImageProcessing(int width, int height,
			byte[] NV21FrameData, int[] pixels, int bitsPerPixel, float mR,
			float mG, float mB);

	static {
		System.loadLibrary("ImageProcessing");
	}

	private Runnable DoImageProcessing = new Runnable() {
		
		public void run() {
			Log.i("MyRealTimeImageProcessing", "DoImageProcessing():");
			bProcessing = true;
			long startTime = System.currentTimeMillis();

			ImageProcessing(PreviewSizeWidth, PreviewSizeHeight, tmpData,
					pixels, ImageFormat.getBitsPerPixel(imageFormat), mR,
					mG, mB);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(elapsedTime);

			bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0,
					PreviewSizeWidth, PreviewSizeHeight);
			MyCameraPreview.setImageBitmap(bitmap);

			bProcessing = false;
		}
	};

	public void takePicture() {
		// TODO Auto-generated method stub

	}
}