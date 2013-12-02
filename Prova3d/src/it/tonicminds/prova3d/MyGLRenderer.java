package it.tonicminds.prova3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.util.Log;

public class MyGLRenderer implements Renderer {

    private static final String TAG = "MyGLRenderer";
	private int mProgram;
	
	final String vertexShader =
		    "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
		 
		  + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.
		 
		  + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.
		 
		  + "void main()                    \n"     // The entry point for our vertex shader.
		  + "{                              \n"
		  + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
		                                            // It will be interpolated across the triangle.
		  + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
		  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
		  + "}                              \n";    // normalized screen coordinates.
	
	final String fragmentShader =
		    "precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
		                                            // precision in the fragment shader.
		  + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
		                                            // triangle per fragment.
		  + "void main()                    \n"     // The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
		  + "}                              \n";
	
	private FloatBuffer mVertexBuffer;
	private float [] mVertices;
	private float color[] = { 0.8f, 0.1f, 0.1f, 1.0f };
    static final int COORDS_PER_VERTEX = 7;
    private final int vertexCount ;
    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * 4;
     
    /** Offset of the position data. */
    private final int mPositionOffset = 0;
     
    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;
     
    /** Offset of the color data. */
    private final int mColorOffset = 3;
     
    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;
    
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    
    
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    
    private float mAngle = 180.0f;
    
	public MyGLRenderer(ArrayList<Object3D> scene, ArrayMap<String, Material> arrayMap) {

		int vertices = 0;
		int index = 0;
		for(Object3D obj3d: scene){
			vertices+= obj3d.getmFaces().size()*3*(COORDS_PER_VERTEX); //3coords per vertex 4 floats for the color
		}
		
		mVertices = new float[vertices];
		
		for(Object3D obj3d: scene){
			for (Face face : obj3d.getmFaces()) {
				mVertices[index++] = face.getP1().getX();
				mVertices[index++] = face.getP1().getY();
				mVertices[index++] = face.getP1().getZ();
				
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getX();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getY();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getZ();
				mVertices[index++] = color[3];
				
				mVertices[index++] = face.getP2().getX();
				mVertices[index++] = face.getP2().getY();
				mVertices[index++] = face.getP2().getZ();
				
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getX();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getY();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getZ();
				mVertices[index++] = color[3];
				
				mVertices[index++] = face.getP3().getX();
				mVertices[index++] = face.getP3().getY();
				mVertices[index++] = face.getP3().getZ();
				
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getX();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getY();
				mVertices[index++] = arrayMap.get(obj3d.getMaterial()).getmKd().getZ();
				mVertices[index++] = color[3];
			}
//			color[0] *= (float)index/mVertices.length;
//			color[1] *= (float)index/mVertices.length;
//			color[2] *= (float)index/mVertices.length;
		}
		
		vertexCount = vertices / COORDS_PER_VERTEX;
		ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
				mVertices.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(mVertices);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);

	}
	
	public void rotate(float angle){
		mAngle+=angle;
	}
	
	
	float xtrans = 0, ytrans=0, ztrans=0;
	public void move(boolean forward){
		if(forward){
			ztrans+=10;
		}else{
			ztrans-=10;
		}
	}
	
	
	@Override
	public void onDrawFrame(GL10 gl) {
		
        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        
        
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 3f, 2.0f, 4f, 0f, 2f, -15f, 0.0f, 1.0f, 0.0f);
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation for the triangle

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // Do a complete rotation every 10 seconds.
//        long time = SystemClock.uptimeMillis() % 10000L;
//        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        long time = SystemClock.uptimeMillis() % 10000L;
        float distance = (20.0f / 10000.0f) * ((int) time);
		Matrix.translateM(mMVPMatrix, 0, xtrans, ytrans, ztrans);
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0f, 1f, 0f);
        
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
      
        
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        MyGLRenderer.checkGlError("glGetAttribLocation");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        MyGLRenderer.checkGlError("glGetAttribLocation");
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        //mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        mVertexBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mVertexBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray");

        // 	Pass in the color information
        mVertexBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, mVertexBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray");
        
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	    // Set the OpenGL viewport to the same size as the surface.
	    GLES20.glViewport(0, 0, width, height);
	 
	    // Create a new perspective projection matrix. The height will stay the same
	    // while the width will vary as per aspect ratio.
	    final float ratio = (float) width / height;
	    final float left = -ratio;
	    final float right = ratio;
	    final float bottom = -1.0f;
	    final float top = 1.0f;
	    final float near = 1f;
	    final float far = 500.0f;
	 
	    
	    //Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	    Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		int vertexShaderP = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
	    int fragmentShaderP = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

	    mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(mProgram, vertexShaderP);   // add the vertex shader to program
	    GLES20.glAttachShader(mProgram, fragmentShaderP); // add the fragment shader to program
	    GLES20.glLinkProgram(mProgram);
	    
	    // Bind attributes
	    GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
	    GLES20.glBindAttribLocation(mProgram, 1, "a_Color");
	    
	    // Get the link status.
	    final int[] linkStatus = new int[1];
	    GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
	 
	    // If the link failed, delete the program.
	    if (linkStatus[0] == 0)
	    {
	        GLES20.glDeleteProgram(mProgram);
	        mProgram = 0;
	    }
	    if (mProgram == 0)
	    {
	        throw new RuntimeException("Error creating program.");
	    }
	}

	
    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
	
    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

     // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
     
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0)
        {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        
        if (shader == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }
        
        return shader;
    }
}
