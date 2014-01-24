package com.harmoneye;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.harmoneye.analysis.MusicAnalyzer;
import com.harmoneye.android.R;
import com.harmoneye.audio.android.Capture;
import com.harmoneye.viz.OpenGlVisualizer;
import com.harmoneye.viz.gl.MyGLSurfaceView;

public class HarmonEyeActivity extends Activity {

	public static final String LOG_TAG = "HarmonEye";

	private static final int TIME_PERIOD_MILLIS = 25;

	private Capture soundCapture;
	private MyGLSurfaceView glView;
	private OpenGlVisualizer visualizer;

	private Timer updateTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);

		glView = new MyGLSurfaceView(this);
		visualizer = new OpenGlVisualizer(glView);

		setContentView(glView);
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

			startUpdateTimer();
		}
		Log.i(LOG_TAG, soundCapture.isRunning() ? "running" : "stopped");
	}

	private void startUpdateTimer() {
		updateTimer = new Timer("update timer");
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				MusicAnalyzer musicAnalyzer = soundCapture.getMusicAnalyzer();
				if (musicAnalyzer != null) {
					musicAnalyzer.updateSignal();
				}
			}
		};
		updateTimer.scheduleAtFixedRate(updateTask, 200, TIME_PERIOD_MILLIS);
	}

	private void stop() {
		if (soundCapture != null) {
			soundCapture.stop();
		}
		if (updateTimer != null) {
			updateTimer.cancel();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(LOG_TAG, "resume");
		glView.onResume();
		toggle();
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
