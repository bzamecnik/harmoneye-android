package com.harmoneye;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.harmoneye.android.R;
import com.harmoneye.audio.android.Capture;
import com.harmoneye.viz.OpenGlVisualizer;
import com.harmoneye.viz.gl.MyGLSurfaceView;

public class HarmonEyeActivity extends Activity {

	public static final String LOG_TAG = "HarmonEye";

	private Capture soundCapture;
	private TextView textView;
	private MyGLSurfaceView glView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);

		textView = new TextView(this);
		glView = new MyGLSurfaceView(this);

		setContentView(textView);

		toggle();
	}

	private void toggle() {
		boolean isRunning = soundCapture != null && soundCapture.isRunning();
		if (isRunning) {
			stop();
		} else {
			if (soundCapture == null) {
				printText("Initializing...");
				soundCapture = new Capture(new OpenGlVisualizer(glView));
				setContentView(glView);
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

	public void printText(String text) {
		if (textView != null) {
			textView.setText(text);
			textView.invalidate();
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
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.i(LOG_TAG, "touch down");
			toggle();
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}