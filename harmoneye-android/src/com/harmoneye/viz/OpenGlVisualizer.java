package com.harmoneye.viz;

import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.viz.gl.MyGLSurfaceView;

public class OpenGlVisualizer implements Visualizer<AnalyzedFrame> {

	private MyGLSurfaceView view;

	public OpenGlVisualizer(MyGLSurfaceView view) {
		this.view = view;
	}

	@Override
	public void update(AnalyzedFrame profile) {
		view.setValue(profile);
	}

}
