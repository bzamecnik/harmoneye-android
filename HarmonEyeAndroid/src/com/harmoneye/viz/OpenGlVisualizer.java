package com.harmoneye.viz;

import com.harmoneye.analysis.PitchClassProfile;
import com.harmoneye.viz.gl.MyGLSurfaceView;

public class OpenGlVisualizer implements Visualizer<PitchClassProfile> {

	private MyGLSurfaceView view;

	public OpenGlVisualizer(MyGLSurfaceView view) {
		this.view = view;
	}

	@Override
	public void update(PitchClassProfile profile) {
		view.setValue(profile);
	}

}
