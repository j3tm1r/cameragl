
package com.jxhembulla.cameragl;

import com.jxhembulla.cameragl.R;
import com.jxhembulla.cameragl.colorpicker.ColorPickerDialog;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
    private MainView mView;

    private ColorPickerDialog mColorDialog;

    private Button colorButton, shutterButton;

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen & full brightness
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mView = new MainView(this);

        ((RelativeLayout)findViewById(R.id.surfaceview_holder)).addView(mView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        lp.alignWithParent = true;
        lp.leftMargin = 25;

        colorButton = new Button(this);
        colorButton.setLayoutParams(lp);
        colorButton.setBackgroundResource(R.drawable.color_button);

        ((RelativeLayout)findViewById(R.id.surfaceview_holder)).addView(colorButton, lp);

        shutterButton = new Button(this);
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lps.addRule(RelativeLayout.CENTER_VERTICAL);
        lps.alignWithParent = true;
        lps.rightMargin = 25;
        shutterButton.setLayoutParams(lps);
        shutterButton.setBackgroundResource(R.drawable.shutter_button);

        ((RelativeLayout)findViewById(R.id.surfaceview_holder)).addView(shutterButton, lps);
        colorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mView != null)
                    mColorDialog.show();
            }
        });

        shutterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mView != null)
                    mView.takePicture();
            }
        });

        mColorDialog = new ColorPickerDialog(this, mView.colorListener, Color.RED, Color.BLUE);

    }

    @Override
    protected void onPause() {
        mView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

}
