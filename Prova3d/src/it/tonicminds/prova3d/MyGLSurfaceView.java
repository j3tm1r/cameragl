package it.tonicminds.prova3d;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.util.ArrayMap;

public class MyGLSurfaceView extends GLSurfaceView implements Console {

	private MyGLRenderer renderer;

	public MyGLSurfaceView(Context context, ArrayList<Object3D> objs3d,
			ArrayMap<String, Material> arrayMap) {
		super(context);

		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
		// Set the Renderer for drawing on the GLSurfaceView
		renderer = new MyGLRenderer(objs3d, arrayMap);
		setRenderer(renderer);
		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void rotateLeft(float degrees) {
		renderer.rotate(-5);
		requestRender();
	}

	@Override
	public void rotateRight(float degrees) {
		renderer.rotate(5);
		requestRender();
	}

	@Override
	public void moveForward() {
		renderer.move(true);
		requestRender();
	}

	@Override
	public void moveBackward() {
		renderer.move(false);
		requestRender();
	}

	@Override
	public void zoom(float value) {
		renderer.zoom(value);
		requestRender();
	}
}