package it.tonicminds.prova3d;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Activity;

public class MainActivity extends Activity {
	private ObjLoader loader;

	private GLSurfaceView mGlView;
	private RelativeLayout rl;
	private RelativeLayout console;
	private Button rotateL, rotateR, forward, backward, zoomIn,zoomOut;
	private Console mConsole;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rl = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.activity_main, null);

		loader = new ObjLoader("scenablend", this);
		mGlView = new MyGLSurfaceView(this, loader.getObjects3d(),
				loader.getMaterials());
		rl.addView(mGlView);

		console = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.console, rl);
		mConsole = (Console) mGlView;
		rotateL = (Button) console.findViewById(R.id.left);
		rotateL.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.rotateLeft(10);
			}
		});
		
		rotateR = (Button) console.findViewById(R.id.right);
		rotateR.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.rotateRight(10);
			}
		});
		
		forward = (Button) console.findViewById(R.id.up);
		forward.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.moveForward();
			}
		});
		
		backward = (Button) console.findViewById(R.id.down);
		backward.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.moveBackward();
			}
		});
		
		zoomIn = (Button) console.findViewById(R.id.zoomin);
		zoomIn.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.zoom(0.25f);
			}
		});
		
		zoomOut = (Button) console.findViewById(R.id.zoomout);
		zoomOut.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mConsole.zoom(-0.25f);
			}
		});
		
		
		setContentView(rl);
	}

}
