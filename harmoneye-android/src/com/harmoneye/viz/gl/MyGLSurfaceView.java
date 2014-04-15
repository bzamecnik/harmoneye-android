package com.harmoneye.viz.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.viz.Visualizer;

/**
 * A view container where OpenGL ES graphics can be drawn on screen. This view
 * can also be used to capture touch events, such as a user interacting with
 * drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView implements
	Visualizer<AnalyzedFrame> {

	private final MyGLRenderer renderer;
	private final MultisampleConfigChooser msaaConfigChooser;

	public MyGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		msaaConfigChooser = new MultisampleConfigChooser();
		setEGLConfigChooser(msaaConfigChooser);

		// Set the Renderer for drawing on the GLSurfaceView
		renderer = new MyGLRenderer(context);
		renderer.setMsaaEnabled(msaaConfigChooser.isMsaaEnabled());
		setRenderer(renderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void update(AnalyzedFrame frame) {
		renderer.setValue(frame);
		requestRender();
	}
}
