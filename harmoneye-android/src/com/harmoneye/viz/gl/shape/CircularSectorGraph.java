package com.harmoneye.viz.gl.shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.harmoneye.analysis.MusicAnalyzer.AnalyzedFrame;
import com.harmoneye.math.cqt.CqtContext;
import com.harmoneye.music.TonicDistance;
import com.harmoneye.viz.gl.MyGLRenderer;

public class CircularSectorGraph {

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
//		"uniform float value;" +
		"uniform float hue;" +
		"uniform float saturation;" +
		"uniform float brightness;" +
		// http://kristophercollins.blogspot.com/2010/08/glsl-hsb-to-rgb-function.html
		"vec3 hsbToRgb(vec3 colorIn) {" +
		"	float h = colorIn.x;" +
		"	float sl = colorIn.y;" +
		"	float l = colorIn.z;" +
		"" +
		"	float v;" +
		"	float r, g, b;" +
		"" +
		"	r = l; // default to gray\n" +
		"	g = l;" +
		"	b = l;" +
		"" +
		"	v = (l <= 0.5) ? (l * (1.0 + sl)) : (l + sl - l * sl);" +
		"" +
		"	if (v > 0.0) {" +
		"		float m;" +
		"		float sv;" +
		"		int sextant;" +
		"		float frac, vsf, mid1, mid2;" +
		"" +
		"		m = l + l - v;" +
		"		sv = (v - m ) / v;" +
		"		h *= 6.0;" +
		"		sextant = int(h);" +
		"		frac = h - float(sextant);" +
		"		vsf = v * sv * frac;" +
		"		mid1 = m + vsf;" +
		"		mid2 = v - vsf;" +
		"" +
		"		if (sextant==0) {" +
		"			r = v;" +
		"			g = mid1;" +
		"			b = m;" +
		"		} else if(sextant==1) {" +
		"			r = mid2;" +
		"			g = v;" +
		"			b = m;" +
		"		} else if(sextant==2) {" +
		"			r = m;" +
		"			g = v;" +
		"			b = mid1;" +
		"		} else if(sextant==3) {" +
		"			r = m;" +
		"			g = mid2;" +
		"			b = v;" +
		"		} else if(sextant==4) {" +
		"			r = mid1;" +
		"			g = m;" +
		"			b = v;" +
		"		} else if(sextant==5) {" +
		"			r = v;" +
		"			g = m;" +
		"			b = mid2;" +
		"		}" +
		"" +
		"	}" +
		"" +
		"	vec3 rgb;" +
		"" +
		"	rgb.r = r;" +
		"	rgb.g = g;" +
		"	rgb.b = b;" +
		"" +
		"	return rgb;" +
		"}" +
		"" +
		"vec3 temperatureColor(float value) {" +
		"	float hue = mod((1.8 - value), 1.0);" +
		"	return hsbToRgb(vec3(hue, value, 0.25 + 0.5 * value));" +
		"}" +
		"void main() {" +
		"  vec4 color = vec4(0.0, 0.0, 0.0, 1.0);" +
//		"  color.rgb = temperatureColor(value);" +
		"  color.rgb = hsbToRgb(vec3(hue, saturation, brightness));" +
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

	// overall scale of the graph
	private float scale;

	// number of circular sectors
	private int sectorCount;
	// 1 / sectorCount
	private float sectorCountInv;
	// tangent of the half-angle of the sector near the circle center
	private float tanAlpha;

	private AnalyzedFrame frame;

	private double values[];
	private Integer key;

	private int binsPerHalftone;
	private int halftoneCount;

	// eg. 1 for straight diagram, 7 for circle of fifths
	private int pitchStep = 1;

	private float[] model = new float[16];
	private float[] mvp = new float[16];

	private TonicDistance tonicDistance = new TonicDistance(12);

	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public CircularSectorGraph(float scale) {
		this.scale = scale;

		vertexBuffer = initVertexBuffer();
		drawListBuffer = initDrawListBuffer();
		program = initShaderProgram();

		setValue(null);
	}

	/** initialize vertex byte buffer for shape coordinates */
	private FloatBuffer initVertexBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(coords.length
			* BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = b.asFloatBuffer();
		vertexBuffer.put(coords);
		vertexBuffer.position(0);
		return vertexBuffer;
	}

	/** initialize byte buffer for the draw list */
	private ShortBuffer initDrawListBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(drawOrder.length
			* BYTES_PER_SHORT);
		b.order(ByteOrder.nativeOrder());
		ShortBuffer drawListBuffer = b.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		return drawListBuffer;
	}

	/** prepare shaders and OpenGL program */
	private int initShaderProgram() {
		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
			vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
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
	 * @param mvpMatrix - The Model View Project matrix in which to draw this
	 * shape.
	 */
	public void draw(float[] mvpMatrix) {
		if (frame == null) {
			return;
		}

		GLES20.glUseProgram(program);

		// vertex position
		int positionHandle = GLES20.glGetAttribLocation(program, "position");
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle,
			COORDS_PER_VERTEX,
			GLES20.GL_FLOAT,
			false,
			vertexStride,
			vertexBuffer);

		// model-view-position matrix for the sector
		int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		// value of the sector
//		 int valueHandle = GLES20.glGetUniformLocation(program, "value");
//		 MyGLRenderer.checkGlError("glGetUniformLocation");

		int hueHandle = GLES20.glGetUniformLocation(program, "hue");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		int saturationHandle = GLES20.glGetUniformLocation(program, "saturation");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		int brightnessHandle = GLES20.glGetUniformLocation(program, "brightness");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		float sectorCountInvDegrees = 360 * sectorCountInv;

		// hack to prevent holes between adjacent triangles
		float epsilon = 1.01f;
		int halfBinsPerHalftone = binsPerHalftone / 2;
		for (int i = 0; i < sectorCount; i++) {
			int pitchClass = i / binsPerHalftone;
			int binInPitchClass = i % binsPerHalftone;
			int movedPitchClass = (pitchClass * pitchStep) % halftoneCount;
			int index = movedPitchClass * binsPerHalftone + binInPitchClass;
			float value = (float) values[index];
			value = Math.max(Math.min(value, 1.0f), 0.0f);

			Matrix.setIdentityM(model, 0);

			float angle = -(i - halfBinsPerHalftone) * sectorCountInvDegrees;
			Matrix.setRotateM(model, 0, angle, 0, 0, 1);

			float sectorLength = 0.65f * scale * value;
			float xScale = sectorLength * tanAlpha * epsilon;
			float yScale = sectorLength;
			Matrix.scaleM(model, 0, xScale, yScale, 1);
			Matrix.multiplyMM(mvp, 0, mvpMatrix, 0, model, 0);

			GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvp, 0);
			MyGLRenderer.checkGlError("glUniformMatrix4fv");

//			 GLES20.glUniform1f(valueHandle, value);
//			 MyGLRenderer.checkGlError("glUniform1f");

			float hue = key != null ? tonicDistance.distanceToHue(tonicDistance
				.distance(movedPitchClass, key)) : 0;
			float saturation = key != null ? 0.1f + 0.75f * value : 0;
			float brightness = 0.25f + 0.55f * value;

			GLES20.glUniform1f(hueHandle, hue);
			MyGLRenderer.checkGlError("glUniform1f");
			GLES20.glUniform1f(saturationHandle, saturation);
			MyGLRenderer.checkGlError("glUniform1f");
			GLES20.glUniform1f(brightnessHandle, brightness);
			MyGLRenderer.checkGlError("glUniform1f");

			GLES20.glDrawElements(GLES20.GL_TRIANGLES,
				drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT,
				drawListBuffer);
		}

		GLES20.glDisableVertexAttribArray(positionHandle);
	}

	public void setValue(AnalyzedFrame frame) {
		this.frame = frame;

		if (frame != null) {
			CqtContext ctx = frame.getCqtContext();
			binsPerHalftone = ctx.getBinsPerHalftone();
			halftoneCount = ctx.getHalftonesPerOctave();

			values = frame.getOctaveBins();
			key = frame.getKey();
			sectorCount = values.length;
			sectorCountInv = 1.0f / sectorCount;
			tanAlpha = (float) Math.tan(Math.PI * sectorCountInv);
		}
	}

	public void setPitchStep(int pitchStep) {
		this.pitchStep = pitchStep;
	}
}
