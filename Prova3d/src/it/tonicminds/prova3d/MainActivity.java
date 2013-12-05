package it.tonicminds.prova3d;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Activity;

public class MainActivity extends Activity {
	private ObjLoader loader;

	private final float ROTATION_ANGLE = 5f;

	private GLSurfaceView mGlViewL, mGlViewR;
	private RelativeLayout rl;
	private RelativeLayout console;
	private Button rotateL, rotateR, forward, backward, zoomIn, zoomOut;
	private Console mConsoleR, mConsoleL;
	private MyGLRenderer mRendererR, mRendererL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		float cameraX = 0, cameraY = 4, cameraZ = 0, hX = 0, hY = 2, hZ = -50, mAngle = 180;
		rl = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.activity_main, null);

		loader = new ObjLoader("scenablend", this);
		mRendererR = new MyGLRenderer(loader.getObjects3d(),
				loader.getMaterials());
		mRendererL = new MyGLRenderer(loader.getObjects3d(),
				loader.getMaterials());

		/*
		 * eyeX = 3f; eyeY = 2.0f; eyeZ = 4f; centerX = 3f; centerY = 0f;
		 * centerZ = -15f;
		 */
		mRendererL.setCameraPos(cameraX - 0.10f, cameraY, cameraZ, hX, hY, hZ, mAngle);
		mRendererR.setCameraPos(cameraX + 0.10f, cameraY, cameraZ, hX, hY, hZ, mAngle);

		mGlViewL = new MyGLSurfaceView(this);
		mGlViewR = new MyGLSurfaceView(this);

		((MyGLSurfaceView) mGlViewL).setRendererScene(mRendererL);
		((MyGLSurfaceView) mGlViewR).setRendererScene(mRendererR);

		RelativeLayout.LayoutParams glviewL, glviewR;

		glviewL = new RelativeLayout.LayoutParams(480, 540);
		glviewR = new RelativeLayout.LayoutParams(480, 540);

		glviewL.setMargins(0, 0, 480, 0);
		glviewL.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		glviewR.setMargins(480, 0, 0, 0);
		glviewR.addRule(RelativeLayout.RIGHT_OF, mGlViewR.getId());
		glviewR.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

		rl.addView(mGlViewL, glviewL);
		rl.addView(mGlViewR, glviewR);

		console = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.console, rl);
		mConsoleR = (Console) mGlViewR;
		mConsoleL = (Console) mGlViewL;
		rotateL = (Button) console.findViewById(R.id.left);
		rotateL.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.rotateLeft(ROTATION_ANGLE);
				mConsoleL.rotateLeft(ROTATION_ANGLE);
			}
		});

		rotateR = (Button) console.findViewById(R.id.right);
		rotateR.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.rotateRight(ROTATION_ANGLE);
				mConsoleL.rotateRight(ROTATION_ANGLE);
			}
		});

		forward = (Button) console.findViewById(R.id.up);
		forward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.moveForward();
				mConsoleL.moveForward();
			}
		});

		backward = (Button) console.findViewById(R.id.down);
		backward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.moveBackward();
				mConsoleL.moveBackward();
			}
		});

		zoomIn = (Button) console.findViewById(R.id.zoomin);
		zoomIn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.zoom(0.25f);
				mConsoleL.zoom(0.25f);
			}
		});

		zoomOut = (Button) console.findViewById(R.id.zoomout);
		zoomOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mConsoleR.zoom(-0.25f);
				mConsoleL.zoom(-0.25f);
			}
		});

		setContentView(rl);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d("Prova", "Key input" + keyCode + " " + event.getKeyCode());
		return true;
	}

}
