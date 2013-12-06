package com.harmoneye.android.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class CircularSectorGraph {

	//@formatter:off
	private static final String vertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 mvpMatrix;" +
	"attribute vec4 position;" +
	"uniform vec4 translation;" +
	"void main() {" +
	// The matrix must be included as a modifier of gl_Position.
	// Note that the mvpMatrix factor *must be first* in order
	// for the matrix multiplication product to be correct.
	"  gl_Position = mvpMatrix * (position - translation);" +
	"}";

	private static final String fragmentShaderCode =
		"precision mediump float;" +
		"uniform float value;" +
		"void main() {" +
		"  gl_FragColor = vec4(value, value, value, 1.0);" +
		"}";
	//@formatter:on

	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_SHORT = 2;

	// overall scale of the graph
	final static float TOTAL_SCALE = 0.9f;

	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private final int program;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	// single triangle
	static float coords[] = {
		// top left
		-1.0f, 1.0f, 0.0f,
		// bottom
		0.0f, 0.0f, 0.0f,
		// top right
		1.0f, 1.0f, 0.0f };

	// order to draw vertices
	private final short drawOrder[] = { 0, 1, 2 };

	// 4 bytes per vertex
	private final int vertexStride = COORDS_PER_VERTEX * 4;

	// number of circular sectors
	private int sectorCount;
	// 1 / sectorCount
	private float sectorCountInv;
	// tangent of the half-angle of the sector near the circle center
	private float tanAlpha;

	private double values[];

	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public CircularSectorGraph() {
		vertexBuffer = initVertexBuffer();
		drawListBuffer = initDrawListBuffer();
		program = initShaderProgram();

		setValues(new double[1]);
	}

	/** initialize vertex byte buffer for shape coordinates */
	private FloatBuffer initVertexBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(coords.length * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = b.asFloatBuffer();
		vertexBuffer.put(coords);
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

		// value of the sector
		int valueHandle = GLES20.glGetUniformLocation(program, "value");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		float[] model = new float[16];
		float[] mvp = new float[16];

		float sectorCountInvDegrees = 360 * sectorCountInv;

		for (int i = 0; i < sectorCount; i++) {
			float value = (float)values[i];

			Matrix.setIdentityM(model, 0);

			float angle = i * sectorCountInvDegrees;
			Matrix.setRotateM(model, 0, angle, 0, 0, 1);

			float sectorLength = TOTAL_SCALE * value;
			float xScale = sectorLength * tanAlpha;
			float yScale = sectorLength;
			Matrix.scaleM(model, 0, xScale, yScale, 1);
			Matrix.multiplyMM(mvp, 0, mvpMatrix, 0, model, 0);

			GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvp, 0);
			MyGLRenderer.checkGlError("glUniformMatrix4fv");

			GLES20.glUniform1f(valueHandle, value);
			MyGLRenderer.checkGlError("glUniform1f");

			GLES20.glDrawElements(
				GLES20.GL_TRIANGLES,
				drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT,
				drawListBuffer);
		}

		GLES20.glDisableVertexAttribArray(positionHandle);
	}

	public void setValues(double values[]) {
		this.values = values;
		this.sectorCount = values.length;
		this.sectorCountInv = 1.0f / sectorCount;
		this.tanAlpha = (float) Math.tan(Math.PI * sectorCountInv);
	}
}
