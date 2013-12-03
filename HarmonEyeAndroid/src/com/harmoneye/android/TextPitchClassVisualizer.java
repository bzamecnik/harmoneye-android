package com.harmoneye.android;

import com.harmoneye.PitchClassProfile;
import com.harmoneye.Visualizer;

public class TextPitchClassVisualizer implements Visualizer<PitchClassProfile> {

	private MainActivity activity;

	public TextPitchClassVisualizer(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void update(PitchClassProfile value) {
		double[] bins = value.getPitchClassBins();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bins.length; i++) {
			sb.append("[").append(i).append("] ");
			doubleToStars(bins[i], sb);
			sb.append("\n");
		}
		printText(sb.toString());
	}

	private static StringBuilder doubleToStars(double amplitude, StringBuilder sb) {
		int starCount = (int) Math.round(amplitude * 10);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb;
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
