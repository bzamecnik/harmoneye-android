package com.harmoneye.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final String LOG_TAG = "HarmonEye";

	private TextView textView;

	private SoundConsumer soundConsumer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		textView = new TextView(this);
		setContentView(textView);
	}

	private void toggle() {
		boolean isRunning = soundConsumer != null && soundConsumer.isRunning();
		if (isRunning) {
			stop();
		} else {
			soundConsumer = new SoundConsumer(this);
			Thread thread = new Thread(soundConsumer);
			thread.start();
		}
		Log.i(LOG_TAG, soundConsumer.isRunning() ? "running" : "stopped");
	}

	private void stop() {
		if (soundConsumer != null) {
			soundConsumer.stop();
		}
	}

	void printText(String text) {
		textView.setText(text);
		textView.invalidate();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
