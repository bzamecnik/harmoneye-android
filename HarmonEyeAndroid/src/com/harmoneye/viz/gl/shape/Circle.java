package com.harmoneye.viz.gl.shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.harmoneye.viz.gl.MyGLRenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Circle {

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
	/** number of coordinates per vertex in this array */
	private static final int COORDS_PER_VERTEX = 3;
	/** bytes per vertex */
	private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	/** handle of the shader program */
	private final int program;

	/** number of outer vertices */
	private int vertexCount;
	/** 1 / {@link #vertexCount} */
	private float vertexCountInv;
	private float scale;
	private float[] fillColor;
	private float[] borderColor;

	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public Circle(int vertexCount, float scale, float[] fillColor,
		float[] borderColor) {
		this.vertexCount = vertexCount;
		vertexCountInv = 1.0f / vertexCount;
		this.scale = scale;
		this.fillColor = fillColor;
		this.borderColor = borderColor;

		vertexBuffer = initVertexBuffer(generateCircleCoords());
		drawListBuffer = initDrawListBuffer(generateDrawList());
		program = initShaderProgram();
	}

	/** initialize vertex byte buffer for shape coordinates */
	private FloatBuffer initVertexBuffer(float coords[]) {
		ByteBuffer b = ByteBuffer.allocateDirect(coords.length * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = b.asFloatBuffer();
		vertexBuffer.put(coords);
		vertexBuffer.position(0);
		return vertexBuffer;
	}

	private float[] generateCircleCoords() {
		int coordsCount = vertexCount * COORDS_PER_VERTEX;
		float[] coords = new float[coordsCount];
		double angleStep = 2 * Math.PI * vertexCountInv;
		double angle = 0;
		for (int i = 0; i < coordsCount; i += COORDS_PER_VERTEX, angle += angleStep) {
			coords[i] = (float) Math.sin(angle);
			coords[i + 1] = (float) Math.cos(angle);
			coords[i + 2] = 0;
		}
		return coords;
	}

	/** initialize byte buffer for the draw list */
	private ShortBuffer initDrawListBuffer(short[] drawOrder) {
		ByteBuffer b = ByteBuffer.allocateDirect(drawOrder.length * BYTES_PER_SHORT);
		b.order(ByteOrder.nativeOrder());
		ShortBuffer drawListBuffer = b.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		return drawListBuffer;
	}

	private short[] generateDrawList() {
		short drawOrder[] = new short[vertexCount];
		for (int i = 0; i < drawOrder.length; i++) {
			drawOrder[i] = (short) i;
		}
		return drawOrder;
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
			VERTEX_STRIDE,
			vertexBuffer);

		int colorHandle = GLES20.glGetUniformLocation(program, "color");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		// modified MVP matrix
		float[] mvp = new float[16];

		Matrix.scaleM(mvp, 0, mvpMatrix, 0, scale, scale, 1);

		GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvp, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		if (fillColor != null) {
			GLES20.glUniform4fv(colorHandle, 1, fillColor, 0);
			MyGLRenderer.checkGlError("glUniform4fv");

			GLES20.glDrawElements(
				GLES20.GL_TRIANGLE_FAN,
				vertexCount,
				GLES20.GL_UNSIGNED_SHORT,
				drawListBuffer);
		}

		if (borderColor != null) {
			GLES20.glUniform4fv(colorHandle, 1, borderColor, 0);
			MyGLRenderer.checkGlError("glUniform4fv");
			
			GLES20.glDrawElements(
				GLES20.GL_LINE_LOOP,
				vertexCount,
				GLES20.GL_UNSIGNED_SHORT,
				drawListBuffer);
		}

		GLES20.glDisableVertexAttribArray(positionHandle);
	}

}
