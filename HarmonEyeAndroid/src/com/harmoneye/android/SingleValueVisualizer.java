package com.harmoneye.android;

import com.harmoneye.Visualizer;

public class SingleValueVisualizer implements Visualizer<Double> {

	private MainActivity activity;

	public SingleValueVisualizer(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void update(Double value) {
		printText("> " + doubleToStars(value));
	}

	private static String doubleToStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int starCount = (int) Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}

	private void printText(final String text) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.printText(text);
			}
		});
	}

}
