package com.harmoneye.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.harmoneye.android.opengl.MyGLSurfaceView;

public class MainActivity extends Activity {

	static final String LOG_TAG = "HarmonEye";

	private Capture soundCapture;
	private TextView textView;
	private MyGLSurfaceView glView;
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		textView = new TextView(this);
		glView = new MyGLSurfaceView(this);

		view = textView;

		setContentView(view);

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
				initialized();
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

	private void initialized() {
		view = glView;
		setContentView(view);
	}
}
