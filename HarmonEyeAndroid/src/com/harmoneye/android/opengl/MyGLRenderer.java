/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harmoneye.android.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.harmoneye.PitchClassProfile;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class must
 * override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyGLRenderer";

	private CircularSectorGraph circularSectorGraph;

	private final float[] modelViewProjection = new float[16];
	private final float[] projection = new float[16];
	private final float[] view = new float[16];

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.05f, 0.25f, 0.05f, 1.0f);

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		circularSectorGraph = new CircularSectorGraph();
//		int sectorCount = 10 * 12;
//		double values[] = new double[sectorCount];
//		for (int i = 0; i < sectorCount; i++) {
//			// values[i] = (i + 1) / (float) (sectorCount + 1);
//			values[i] = Math.abs(Math.cos(4 * 2 * Math.PI * i / (double) sectorCount));
//		}
//		circularSectorGraph.setValues(values);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		circularSectorGraph.draw(modelViewProjection);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		if (width > height) {
			float ratio = (float) width / height;
			Matrix.orthoM(projection, 0, -ratio, ratio, -1, 1, 3, 7);
		} else {
			float ratio = (float) height / width;
			Matrix.orthoM(projection, 0, -1, 1, -ratio, ratio, 3, 7);
		}

		// Set the camera position (View matrix)
		Matrix.setLookAtM(view, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(modelViewProjection, 0, projection, 0, view, 0);

	}

	/**
	 * Utility method for compiling a OpenGL shader.
	 * 
	 * <p>
	 * <strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.
	 * </p>
	 * 
	 * @param type
	 *          - Vertex or fragment shader type.
	 * @param shaderCode
	 *          - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode) {
		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 * 
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 * 
	 * If the operation is not successful, the check throws an error.
	 * 
	 * @param glOperation
	 *          - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	public void setValue(PitchClassProfile profile) {
		double[] values = profile.getPitchClassBins();
		circularSectorGraph.setValues(values);
	}
}