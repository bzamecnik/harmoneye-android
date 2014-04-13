package com.harmoneye.viz.gl.shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.harmoneye.viz.gl.MyGLRenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class CircularGrid {

	//@formatter:off
	private static final String vertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 mvpMatrix;" +
	"attribute vec4 position;" +
	"void main() {" +
	// The matrix must be included as a modifier of gl_Position.
	// Note that the mvpMatrix factor *must be first* in order
	// for the matrix multiplication product to be correct.
	"  gl_Position = mvpMatrix * position;" +
	"}";

	private static final String fragmentShaderCode =
		"precision mediump float;" +
		"uniform vec4 color;" +
		"void main() {" +
		"  gl_FragColor = color;" +
		"}";
	//@formatter:on

	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_SHORT = 2;

	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private final int program;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	// single line
	static float lineCoords[] = {
		// up
		0.0f, 1.0f, 0.0f,
		// center
		0.0f, 0.0f, 0.0f };

	// order to draw vertices
	private final short drawOrder[] = { 0, 1 };

	// 4 bytes per vertex
	private final int vertexStride = COORDS_PER_VERTEX * 4;

	// number of circular sectors
	private int sectorCount;
	// 1 / sectorCount
	private float sectorCountInv;

	private float scale;
	private float[] color;

	private float[] model;

	private float[] mvp;

	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public CircularGrid(int sectorCount, float scale, float[] color) {
		this.sectorCount = sectorCount;
		this.sectorCountInv = 1.0f / sectorCount;
		this.scale = scale;
		this.color = color;

		vertexBuffer = initVertexBuffer();
		drawListBuffer = initDrawListBuffer();
		program = initShaderProgram();
		
		model = new float[16];
		mvp = new float[16];
	}

	/** initialize vertex byte buffer for shape coordinates */
	private FloatBuffer initVertexBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(lineCoords.length
			* BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = b.asFloatBuffer();
		vertexBuffer.put(lineCoords);
		vertexBuffer.position(0);
		return vertexBuffer;
	}

	/** initialize byte buffer for the draw list */
	private ShortBuffer initDrawListBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(drawOrder.length * BYTES_PER_SHORT);
		b.order(ByteOrder.nativeOrder());
		ShortBuffer drawListBuffer = b.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		return drawListBuffer;
	}

	/** prepare shaders and OpenGL program */
	private int initShaderProgram() {
		int vertexShader = MyGLRenderer.loadShader(
			GLES20.GL_VERTEX_SHADER,
			vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(
			GLES20.GL_FRAGMENT_SHADER,
			fragmentShaderCode);

		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		return program;
	}

	/**
	 * Encapsulates the OpenGL ES instructions for drawing this shape.
	 * 
	 * @param mvpMatrix
	 *          - The Model View Project matrix in which to draw this shape.
	 */
	public void draw(float[] mvpMatrix) {
		GLES20.glUseProgram(program);

		// vertex position
		int positionHandle = GLES20.glGetAttribLocation(program, "position");
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(
			positionHandle,
			COORDS_PER_VERTEX,
			GLES20.GL_FLOAT,
			false,
			vertexStride,
			vertexBuffer);

		// model-view-position matrix for the sector
		int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		int colorHandle = GLES20.glGetUniformLocation(program, "color");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		GLES20.glUniform4fv(colorHandle, 1, color, 0);
		MyGLRenderer.checkGlError("glUniform4fv");

		

		float sectorCountInvDegrees = 360 * sectorCountInv;

		GLES20.glLineWidth(2f);
		
		// rays
		for (int i = 0; i < sectorCount; i++) {
			Matrix.setIdentityM(model, 0);
			float angle = (i - 0.5f) * sectorCountInvDegrees;
			Matrix.setRotateM(model, 0, angle, 0, 0, 1);
			Matrix.scaleM(model, 0, 1, scale, 1);
			Matrix.multiplyMM(mvp, 0, mvpMatrix, 0, model, 0);

			GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvp, 0);
			MyGLRenderer.checkGlError("glUniformMatrix4fv");

			GLES20.glDrawElements(
				GLES20.GL_LINES,
				drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT,
				drawListBuffer);
		}

		GLES20.glDisableVertexAttribArray(positionHandle);
	}

}
