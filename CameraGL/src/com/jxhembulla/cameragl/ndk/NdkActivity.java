package com.jxhembulla.cameragl.ndk;

import com.jxhembulla.cameragl.R;
import com.jxhembulla.cameragl.colorpicker.ColorPickerDialog;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class NdkActivity extends Activity {

	private CameraPreview camPreview;
	private ImageView MyCameraPreview = null;
	private RelativeLayout mainLayout;
	private int PreviewSizeWidth = 0;
	private int PreviewSizeHeight = 0;
	private Button colorButton;
	private View shutterButton;

	private ColorPickerDialog mColorDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set this APK Full screen

		if (getIntent().getExtras().getInt("height") != 0) {
			PreviewSizeHeight = getIntent().getExtras().getInt("height");
			PreviewSizeWidth = getIntent().getExtras().getInt("width");
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Set this APK no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		//
		// Create my camera preview
		//
		MyCameraPreview = new ImageView(this);

		SurfaceView camView = new SurfaceView(this);
		SurfaceHolder camHolder = camView.getHolder();
		camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight,
				MyCameraPreview);

		camHolder.addCallback(camPreview);
		camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mainLayout = (RelativeLayout) findViewById(R.id.surfaceview_holder);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				PreviewSizeWidth, PreviewSizeHeight);

		lp.addRule(RelativeLayout.CENTER_VERTICAL);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mainLayout.addView(camView, lp);
		mainLayout.addView(MyCameraPreview, lp);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp2.addRule(RelativeLayout.CENTER_VERTICAL);
		lp2.alignWithParent = true;
		lp2.leftMargin = 25;

		colorButton = new Button(this);
		colorButton.setLayoutParams(lp2);
		colorButton.setBackgroundResource(R.drawable.color_button);

		((RelativeLayout) findViewById(R.id.main_layout)).addView(colorButton,
				lp2);

		shutterButton = new Button(this);
		RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lps.addRule(RelativeLayout.CENTER_VERTICAL);
		lps.alignWithParent = true;
		lps.rightMargin = 25;
		shutterButton.setLayoutParams(lps);
		shutterButton.setBackgroundResource(R.drawable.shutter_button);

		((RelativeLayout) findViewById(R.id.main_layout)).addView(
				shutterButton, lps);
		colorButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MyCameraPreview != null)
					mColorDialog.show();
			}
		});

		shutterButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (camPreview != null)
					camPreview.takePicture();
			}
		});
		//
		mColorDialog = new ColorPickerDialog(this, camPreview.colorListener,
				Color.RED, Color.BLUE);

	}

	protected void onPause() {
		if (camPreview != null)
			camPreview.onPause();
		super.onPause();
	}

}
