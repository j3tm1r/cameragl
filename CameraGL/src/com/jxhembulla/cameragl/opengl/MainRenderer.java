package com.jxhembulla.cameragl.opengl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class MainRenderer implements GLSurfaceView.Renderer,
		SurfaceTexture.OnFrameAvailableListener {
	private final String vss = "attribute vec2 vPosition;\n"
			+ "attribute vec2 vTexCoord;\n" + "varying vec2 texCoord;\n"
			+ "void main() {\n" + "  texCoord = vTexCoord;\n"
			+ "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 0.9 );\n"
			+ "}";

	private final String fss = "#extension GL_OES_EGL_image_external : require\r\n"
			+ "precision mediump float;\r\n"
			+ "uniform samplerExternalOES sTexture;\r\n"
			+ "uniform float rUniform;\r\n"
			+ "uniform float gUniform;\r\n"
			+ "uniform float bUniform;\r\n"
			+ "varying vec2 texCoord;\r\n"
			+ "void main(void)\r\n"
			+ "{      lowp vec4 textureColor = texture2D(sTexture,texCoord);\r\n"
			+ "    lowp float gray = dot(textureColor, vec4(0.299, 0.587, 0.114, 0.0));\r\n"
			+ "    \r\n"
			+ "    mat3 rgb2yuv = mat3(\r\n"
			+ "        0.2126, -0.09991, 0.615,    // first column (not row!)\r\n"
			+ "        0.7152, -0.33609, -0.55861,     // second column\r\n"
			+ "        0.0722, 0.436, -0.05639     // third column\r\n"
			+ "    );\r\n"
			+ "    \r\n"
			+ "    mat3 yuv2rgb = mat3(\r\n"
			+ "        1.0, 1.0, 1.0,              // first column (not row!)\r\n"
			+ "        0.0, -0.21482, 2.12798,     // second column\r\n"
			+ "        1.28033, -0.38059, 0.0      // third column\r\n"
			+ "    );  \r\n"
			+ "    \r\n"
			+ "    lowp vec3 rgbColor = vec3(textureColor.r,textureColor.g,textureColor.b);\r\n"
			+ "    lowp vec3 yuvColor = rgb2yuv * rgbColor;\r\n"
			+ "    lowp vec3 filterColor = rgb2yuv * vec3(rUniform,gUniform,bUniform)/255.0;\r\n"
			+ "    \r\n"
			+ "    highp float vp = yuvColor[2];\r\n"
			+ "    highp float up = yuvColor[1];\r\n"
			+ "                highp float uf = filterColor[1];\r\n"
			+ "                highp float vf = filterColor[2];\r\n"
			+ "                highp float angleP = atan(vp,up);\r\n"
			+ "            highp float angleF = atan(vf,uf);\r\n"
			+ "    //if(textureColor.r > 0.6 && textureColor.g < 0.2 && textureColor.g < 0.2)\r\n"
			+ "    \r\n"
			+ "    highp float delta = abs(angleF-angleP);\r\n"
			+ "    \r\n"
			+ "    highp float a=-0.2-0.05;\r\n"
			+ "    highp float b=-0.2;\r\n"
			+ "    highp float c=0.2;\r\n"
			+ "    highp float d=0.2+0.05;\r\n"
			+ "    \r\n"
			+ "    highp float alpha;\r\n"
			+ "    \r\n"
			+ "    if (delta < a || delta > d) alpha=0.0;\r\n"
			+ "    else if (a <= delta && delta <= b) alpha=(delta-a)/(b-a);\r\n"
			+ "    else if (b <= delta && delta <= c) alpha=1.0;\r\n"
			+ "    else if (c <= delta && delta <= d) alpha=(d-delta)/(d-c);\r\n"
			+ "    \r\n"
			+ "    gl_FragColor = textureColor * alpha + vec4(gray,gray,gray,1)*(1.0-alpha);"
			+ "}";

	private int mR = 20, mG = 220, mB = 20;

	private int[] hTex;

	private FloatBuffer pVertex;

	private FloatBuffer pTexCoord;

	private int hProgram;

	private static Camera mCamera;

	private SurfaceTexture mSTexture;

	private boolean mUpdateST = false;

	private MainView mView;

	private byte[] callbackBuffer;

	private int previewSizeHeight, previewSizeWidth;

	private FPSCounter mFpsCounter;

	public MainRenderer(MainView view) {
		mView = view;
		float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
		float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
		pVertex = ByteBuffer.allocateDirect(8 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		pVertex.put(vtmp);
		pVertex.position(0);
		pTexCoord = ByteBuffer.allocateDirect(8 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		pTexCoord.put(ttmp);
		pTexCoord.position(0);
		mFpsCounter = new FPSCounter();
	}

	public void close() {
		mCamera.setPreviewCallbackWithBuffer(null);
		releaseCamera();
		mUpdateST = false;
		mSTexture.release();
		callbackBuffer = null;
		deleteTex();
	}

	private static synchronized boolean openCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		try {
			mCamera = Camera.open();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static synchronized void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private static synchronized Camera.Parameters getCameraParameters() {
		if (mCamera != null) {
			return mCamera.getParameters();
		}
		return null;
	}

	private static synchronized void setCameraParameters(
			Camera.Parameters params) {
		if (mCamera != null) {
			mCamera.setParameters(params);
		}
	}

	public static synchronized void addCameraCallbackBuffer(byte[] data) {
		if (mCamera != null) {
			mCamera.addCallbackBuffer(data);
		}
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		// Log.i("mr", "Gl extensions: " + extensions);
		// Assert.assertTrue(extensions.contains("OES_EGL_image_external"));

		initTex();
		mSTexture = new SurfaceTexture(hTex[0]);
		mSTexture.setOnFrameAvailableListener(this);

		openCamera();

		Camera.Parameters param = mCamera.getParameters();
		List<Size> psize = param.getSupportedPreviewSizes();
		if (psize.size() > 0) {
			int i;
			for (i = 0; i < psize.size(); i++) {
				if (psize.get(i).width < mView.getWidth()
						|| psize.get(i).height < mView.getHeight())
					break;
			}
			if (i > 0)
				i--;
			param.setPreviewSize(psize.get(i).width, psize.get(i).height);
			int bitsPerPixel = ImageFormat.getBitsPerPixel(mCamera
					.getParameters().getPreviewFormat());

			int bufferSize = psize.get(i).height * psize.get(i).width
					* bitsPerPixel / 8;
			callbackBuffer = null;

			callbackBuffer = new byte[bufferSize];
		}

		try {

			addCameraCallbackBuffer(callbackBuffer);
			mCamera.setPreviewCallbackWithBuffer(pb);
			mCamera.setPreviewTexture(mSTexture);
		} catch (IOException ioe) {
		}

		GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

		hProgram = loadShader(vss, fss);
	}

	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		mFpsCounter.logFrame();

		synchronized (this) {
			if (mUpdateST) {
				mSTexture.updateTexImage();
				mUpdateST = false;
			}
		}

		GLES20.glUseProgram(hProgram);

		int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
		int tch = GLES20.glGetAttribLocation(hProgram, "vTexCoord");
		int th = GLES20.glGetUniformLocation(hProgram, "sTexture");

		int rLocation = GLES20.glGetUniformLocation(hProgram, "rUniform");
		int gLocation = GLES20.glGetUniformLocation(hProgram, "gUniform");
		int bLocation = GLES20.glGetUniformLocation(hProgram, "bUniform");

		GLES20.glUniform1f(rLocation, (float) mR);
		GLES20.glUniform1f(gLocation, (float) mG);
		GLES20.glUniform1f(bLocation, (float) mB);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		GLES20.glUniform1i(th, 0);

		GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2,
				pVertex);
		GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2,
				pTexCoord);
		GLES20.glEnableVertexAttribArray(ph);
		GLES20.glEnableVertexAttribArray(tch);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glFlush();

	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		Camera.Parameters param = getCameraParameters();
		List<Size> psize = param.getSupportedPreviewSizes();
		if (psize.size() > 0) {
			int i;
			for (i = 0; i < psize.size(); i++) {
				if (psize.get(i).width < width || psize.get(i).height < height)
					break;
			}
			if (i > 0)
				i--;
			param.setPreviewSize(previewSizeWidth, previewSizeHeight);

			int bitsPerPixel = ImageFormat.getBitsPerPixel(mCamera
					.getParameters().getPreviewFormat());

			int bufferSize = psize.get(i).height * psize.get(i).width
					* bitsPerPixel / 8;
			callbackBuffer = new byte[bufferSize];
		}

		addCameraCallbackBuffer(callbackBuffer);
		mCamera.setPreviewCallbackWithBuffer(pb);
		param.setPreviewFormat(ImageFormat.NV21);
		
		param.set("orientation", "landscape");
		setCameraParameters(param);
		mCamera.startPreview();
	}

	PreviewCallback pb = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Log.d("CameraRenderer", "OnPreviewFrame");
			addCameraCallbackBuffer(callbackBuffer);
			mCamera.setPreviewCallbackWithBuffer(pb);
		}
	};

	public void takePicture() {
		mCamera.takePicture(null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				// Log.d("CameraRenderer", "Raw picture taken" +
				// callbackBuffer[0]);
				camera.startPreview();
			}
		}, null, null);
	}

	public void touchEvent(float x, float y) {

		int offset = previewSizeWidth * previewSizeHeight;

		int divideby = 0;
		int y1, y2, y3, y4;
		int r, g, b;

		int pixelh = (int) (((float) previewSizeHeight) * y);
		int pixelw = (int) (((float) previewSizeWidth) * x);

		int v = ((callbackBuffer[offset + pixelw * pixelh])) & 0xff;
		int u = ((callbackBuffer[offset + pixelw * pixelh + 1])) & 0xff;

		y1 = 0xff & callbackBuffer[pixelw * pixelh];

		divideby++;
		//
		// // elements of he row before
		// if (y > 1) {
		// u += (0xff & callbackBuffer[offset + (pixelw>>1) * (pixelh - 1)] -
		// 128);
		// v += (0xff & callbackBuffer[offset + (pixelw>>1) * (pixelh - 1) + 1]
		// - 128);
		// divideby++;
		// }

		// // elements of he row after
		// if (y < previewSize.height) {
		// u += (0xff & callbackBuffer[offset + (x >> 1) * (y + 1)] - 128);
		// v += (0xff & callbackBuffer[offset + (x >> 1) * (y + 1) + 1] - 128);
		// divideby++;
		// }
		//
		// // elements on the left
		// if (x > 0) {
		// u += (0xff & callbackBuffer[offset + ((x - 1) >> 1) * (y)] - 128);
		// v += (0xff & callbackBuffer[offset + ((x - 1) >> 1) * (y) + 1] -
		// 128);
		// divideby++;
		// }
		//
		// // elements on the right
		// if (x < previewSize.width) {
		// u += (0xff & callbackBuffer[offset + ((x + 1) >> 1) * y] - 128);
		// v += (0xff & callbackBuffer[offset + ((x + 1) >> 1) * y + 1] - 128);
		// divideby++;
		// }

		// u /= 128;
		// v /= 128;
		u -= 128;
		v -= 128;

		r = y1 + (int) 1.402f * v;
		g = y1 - (int) (0.344f * u + 0.714f * v);
		b = y1 + (int) 1.772f * u;
		r = r > 255 ? 255 : r < 0 ? 0 : r;
		g = g > 255 ? 255 : g < 0 ? 0 : g;
		b = b > 255 ? 255 : b < 0 ? 0 : b;

		this.mR = r;
		this.mG = g;
		this.mB = b;

		Log.d("CameraRenderer", "Touch even @ U=" + u + " V=" + v);
		Log.d("CameraRenderer", "R for pixel " + r + "G for pixel " + g
				+ " B for pixel " + b);

	}

	private void initTex() {
		hTex = new int[1];
		GLES20.glGenTextures(1, hTex, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	}

	private void deleteTex() {
		GLES20.glDeleteTextures(1, hTex, 0);
	}

	public synchronized void onFrameAvailable(SurfaceTexture st) {
		mUpdateST = true;
		mView.requestRender();
	}

	public void filterColor(int r, int g, int b) {
		mR = r;
		mB = b;
		mG = g;
	}

	private static int loadShader(String vss, String fss) {
		int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vshader, vss);
		GLES20.glCompileShader(vshader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile vshader");
			Log.v("Shader",
					"Could not compile vshader:"
							+ GLES20.glGetShaderInfoLog(vshader));
			GLES20.glDeleteShader(vshader);
			vshader = 0;
		}

		int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fshader, fss);
		GLES20.glCompileShader(fshader);
		GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile fshader");
			Log.v("Shader",
					"Could not compile fshader:"
							+ GLES20.glGetShaderInfoLog(fshader));
			GLES20.glDeleteShader(fshader);
			fshader = 0;
		}

		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vshader);
		GLES20.glAttachShader(program, fshader);
		GLES20.glLinkProgram(program);

		return program;
	}

	public void setWH(int previewSizeWidth, int previewSizeHeight) {
		this.previewSizeHeight = previewSizeHeight;
		this.previewSizeWidth = previewSizeWidth;
	}

}
