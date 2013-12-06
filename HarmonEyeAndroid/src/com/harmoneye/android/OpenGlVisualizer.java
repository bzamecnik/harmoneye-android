package com.harmoneye.android;

import com.harmoneye.PitchClassProfile;
import com.harmoneye.Visualizer;
import com.harmoneye.android.opengl.MyGLSurfaceView;

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
