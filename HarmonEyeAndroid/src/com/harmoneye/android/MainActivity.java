package com.harmoneye.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class MainActivity extends Activity {

	static final String LOG_TAG = "HarmonEye";

	private Capture soundCapture;

	private TextView textView;

//	private GraphView graphView;
//	private GraphViewSeries graphViewSeries;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

				textView = new TextView(this);
				setContentView(textView);

//		graphViewSeries = new GraphViewSeries(new GraphViewData[] {
//			new GraphViewData(0, 0)
//		});
//
//		graphView = new LineGraphView(this, "GraphViewDemo");
//		graphView.addSeries(graphViewSeries);
//		graphView.setScrollable(true);
//		graphView.setViewPort(0, 120);
//		graphView.setManualYAxisBounds(1.0, 0.0);
//
//		setContentView(graphView);
		
		toggle();
	}

	private void toggle() {
		boolean isRunning = soundCapture != null && soundCapture.isRunning();
		if (isRunning) {
			stop();
		} else {
			soundCapture = new Capture(this);
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
		printText("Click to toggle recording.");
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

//	public GraphViewSeries getGraphViewSeries() {
//		return graphViewSeries;
//	}
}
