package com.harmoneye;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.harmoneye.android.R;
import com.harmoneye.audio.android.Capture;
import com.harmoneye.viz.OpenGlVisualizer;
import com.harmoneye.viz.gl.MyGLSurfaceView;

public class HarmonEyeActivity extends Activity {

	public static final String LOG_TAG = "HarmonEye";

	private Capture soundCapture;
	private MyGLSurfaceView glView;
	private OpenGlVisualizer visualizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);

		glView = new MyGLSurfaceView(this);
		visualizer = new OpenGlVisualizer(glView);

		setContentView(glView);

		toggle();
	}

	private void toggle() {
		boolean isRunning = soundCapture != null && soundCapture.isRunning();
		if (isRunning) {
			stop();
		} else {
			if (soundCapture == null) {
				soundCapture = new Capture(visualizer);
			}
			Thread thread = new Thread(soundCapture);
			thread.start();

		}
		Log.i(LOG_TAG, soundCapture.isRunning() ? "running" : "stopped");
	}

	private void stop() {
		if (soundCapture != null) {
			soundCapture.stop();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(LOG_TAG, "resume");
		glView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glView.onPause();
		Log.i(LOG_TAG, "pause");
		stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(LOG_TAG, "destroy");
		stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
