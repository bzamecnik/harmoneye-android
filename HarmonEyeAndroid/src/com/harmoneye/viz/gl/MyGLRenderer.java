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
package com.harmoneye.viz.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.android.R;
import com.harmoneye.viz.gl.shape.Circle;
import com.harmoneye.viz.gl.shape.CircularGrid;
import com.harmoneye.viz.gl.shape.CircularSectorGraph;
import com.harmoneye.viz.gl.shape.TexturedQuad;

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

	private static final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;

	private static final float OUTER_CIRCLE_SCALE = 0.9f;
	private static final float INNER_CIRCLE_SCALE = 0.2f;

	private final float[] modelViewProjection = new float[16];
	private final float[] projection = new float[16];
	private final float[] view = new float[16];

	private CircularSectorGraph circularSectorGraph;
	private Circle outerCircle;
	private Circle innerCircle;
	private CircularGrid circularGrid;
	private TexturedQuad introLogo;
	private TexturedQuad toneNames;

	/** indicates whether multi-sample anti-aliasing is enabled */
	private boolean msaaEnabled;

	private boolean initialized;

	private boolean hasValue;

	private Context activityContext;

	public MyGLRenderer(Context context) {
		this.activityContext = context;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_DITHER);

		circularSectorGraph = new CircularSectorGraph(OUTER_CIRCLE_SCALE);
		float[] midGrey = new float[] { 0.35f, 0.35f, 0.35f, 1.0f };
		float[] darkGrey = new float[] { 0.25f, 0.25f, 0.25f, 1.0f };
		outerCircle = new Circle(100, OUTER_CIRCLE_SCALE, null, midGrey);
		innerCircle = new Circle(30, INNER_CIRCLE_SCALE, darkGrey, midGrey);
		circularGrid = new CircularGrid(12, OUTER_CIRCLE_SCALE, midGrey);
		introLogo = new TexturedQuad(activityContext, R.drawable.intro, 0.6f);
		toneNames = new TexturedQuad(activityContext, R.drawable.tone_name_circle, OUTER_CIRCLE_SCALE);
		initialized = true;
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		boolean introEnabled = !hasValue;
		if (introEnabled) {
			GLES20.glClearColor(0.8f, 0.8f, 0.8f, 0.0f);
		} else {
			GLES20.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
		}
		
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT;
		if (msaaEnabled) {
			clearMask |= GL_COVERAGE_BUFFER_BIT_NV;
		}
		GLES20.glClear(clearMask);

		if (initialized) {
			if (introEnabled) {
				introLogo.draw(modelViewProjection);
			} else {
				toneNames.draw(modelViewProjection);
				outerCircle.draw(modelViewProjection);
				circularGrid.draw(modelViewProjection);
				circularSectorGraph.draw(modelViewProjection);
				innerCircle.draw(modelViewProjection);
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		if (width > height) {
			float ratio = (float) width / height;
			Matrix.orthoM(projection, 0, -ratio, ratio, -1, 1, -1, 1);
		} else {
			float ratio = (float) height / width;
			Matrix.orthoM(projection, 0, -1, 1, -ratio, ratio, -1, 1);
		}
		
//		if (width > height) {
//			float ratio = (float) width / height;
//			Matrix.orthoM(projection, 0, -ratio, ratio, -1, 1, -1, 1);
//		} else {
//			float ratio = (float) height / width;
//			Matrix.orthoM(projection, 0, -1, 1, -ratio, ratio, -1, 1);
//		}

		// Set the camera position (View matrix)
		Matrix.setLookAtM(view, 0, 0, 0, 1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//		Matrix.setLookAtM(view, 0, 0, 0, 0, 0, 0, -1, 0, 1, 0);

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

	public void setValue(AnalyzedFrame frame) {
		if (initialized) {
			circularSectorGraph.setValue(frame);
		}
		hasValue = true;
	}

	public boolean isMsaaEnabled() {
		return msaaEnabled;
	}

	public void setMsaaEnabled(boolean msaaEnabled) {
		this.msaaEnabled = msaaEnabled;
	}
}
