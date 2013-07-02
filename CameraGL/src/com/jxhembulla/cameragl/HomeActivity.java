package com.jxhembulla.cameragl;

import java.util.ArrayList;
import java.util.List;

import com.jxhembulla.cameragl.ndk.NdkActivity;
import com.jxhembulla.cameragl.opengl.OpenGLActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.app.Activity;
import android.content.Intent;

public class HomeActivity extends Activity {

	private Spinner mResolutionSpinner;
	private List<String> resolutions;
	private int width = 0, height = 0;

	private Button ndkButton, openGlButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		resolutions = new ArrayList<String>();
		resolutions.add("128x96");
		resolutions.add("320x240");
		resolutions.add("640x480");
		resolutions.add("1280x720");

		mResolutionSpinner = (Spinner) findViewById(R.id.resolution_spinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, android.R.id.text1);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mResolutionSpinner.setAdapter(spinnerAdapter);
		for (int i = 0; i < resolutions.size(); i++)
			spinnerAdapter.add(resolutions.get(i));
		spinnerAdapter.notifyDataSetChanged();
		mResolutionSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {

						switch (pos) {
						case 0:
							width = 128;
							height = 96;
							break;
						case 1:
							width = 320;
							height = 240;
							break;
						case 2:
							width = 640;
							height = 480;
							break;
						case 3:
							width = 1280;
							height = 720;
							break;

						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		ndkButton = (Button) findViewById(R.id.ndk);
		openGlButton = (Button) findViewById(R.id.openGl);

		ndkButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, NdkActivity.class);
				intent.putExtra("height", height);
				intent.putExtra("width", width);
				startActivity(intent);
			}
		});

		openGlButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this,
						OpenGLActivity.class);
				intent.putExtra("height", height);
				intent.putExtra("width", width);
				startActivity(intent);
			}
		});

	}
}
