package com.jxhembulla.cameragl.ndk;

import java.io.IOException;
import com.jxhembulla.cameragl.colorpicker.ColorPickerDialog.OnColorChangedListener;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

public class CameraPreview implements SurfaceHolder.Callback,
		Camera.PreviewCallback {
	private Camera mCamera = null;
	private ImageView MyCameraPreview = null;
	private int[] pixels = null;
	private byte[] FrameData = null;
	private byte[] tmpData = null;
	private int imageFormat;
	private int PreviewSizeWidth;
	private int PreviewSizeHeight;
	private boolean bProcessing = false;

	private byte[] pixels3, pixels2;

	private int mR = 20, mG = 220, mB = 20;

	private Handler mHandler;

	private static Handler h;

	private int newFrame = 10;
	private Message processingM;
	private MHandler mHandlerProcesing;


	//
	// Native JNI
	//
	public native String ImageProcessing(int width, int height,
			byte[] NV21FrameData, int[] pixels, int bitsPerPixel, float mR,
			float mG, float mB);
	
	static{
		System.loadLibrary("ImageProcessing");
	}
	
	HandlerThread ht = new HandlerThread("Processing");
	
	
	private class MHandler extends HandlerThread {

		private Handler myHandler;
		private Handler camHandler;
		private Message msgCam;
		

		public MHandler(String name, Handler h) {
			super(name);
			camHandler = h;
		}

		@Override
		protected void onLooperPrepared() {
			super.onLooperPrepared();

			synchronized (this) {
				this.myHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == newFrame) {
							Bitmap bitmap = null;
							bitmap = Bitmap.createBitmap(PreviewSizeWidth,
									PreviewSizeHeight, Bitmap.Config.ARGB_8888);

							Log.i("MyRealTimeImageProcessing",
									"DoImageProcessing():");
							
							long startTime = System.currentTimeMillis();
							String time = new String();

							time = ImageProcessing(PreviewSizeWidth,
									PreviewSizeHeight, (byte[])msg.obj, pixels,
									ImageFormat.getBitsPerPixel(imageFormat),
									mR, mG, mB);
							long stopTime = System.currentTimeMillis();
							long elapsedTime = stopTime - startTime;
							System.out.println(elapsedTime);

							bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0,
									PreviewSizeWidth, PreviewSizeHeight);

							msgCam = camHandler.obtainMessage();
							msgCam.obj = bitmap;
							camHandler.sendMessage(msgCam);
						}
					};
				};
			}
		}

		public synchronized Handler getHandler() {
			while (myHandler == null)
				try {
					wait();
				} catch (InterruptedException e) {
					// ignore
				}

			return myHandler;
		}
	}

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
		pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
		colorListener = new ColorChangeListener();
		h = new Handler() {
			public void dispatchMessage(Message msg) {
				Bitmap bitmap = (Bitmap) msg.obj;
				MyCameraPreview.setImageBitmap(bitmap);
			}
		};
		mHandlerProcesing = new MHandler("ProcessingThread", h);
		mHandlerProcesing.start();
	}

	public void filterColor(int red, int green, int blue) {
		mR = red;
		mB = blue;
		mG = green;
	}

	@Override
	public void onPreviewFrame(byte[] frameData, Camera arg1) {
		// At preview mode, the frame data will push to here.

		processingM = mHandler.obtainMessage(newFrame);
		processingM.obj = frameData;
		mHandler.sendMessage(processingM);

		mCamera.addCallbackBuffer(pixels3);
		mCamera.addCallbackBuffer(pixels2);
		mCamera.setPreviewCallbackWithBuffer(this);
	}

	public void onPause() {
		mCamera.stopPreview();
		mHandlerProcesing.quit();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Parameters parameters;

		parameters = mCamera.getParameters();
		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

		imageFormat = parameters.getPreviewFormat();

		mCamera.setParameters(parameters);
		pixels2 = new byte[PreviewSizeHeight * PreviewSizeWidth
				* ImageFormat.getBitsPerPixel(imageFormat)];

		pixels3 = new byte[PreviewSizeHeight * PreviewSizeWidth
				* ImageFormat.getBitsPerPixel(imageFormat)];

		mCamera.addCallbackBuffer(pixels3);
		mCamera.addCallbackBuffer(pixels2);
		mCamera.setPreviewCallbackWithBuffer(this);
		mCamera.startPreview();
		mHandler = mHandlerProcesing.getHandler();
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


	public void takePicture() {

	}
}