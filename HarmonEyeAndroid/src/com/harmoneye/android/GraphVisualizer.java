package com.harmoneye.android;

import com.harmoneye.Visualizer;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class GraphVisualizer implements Visualizer<Double> {

	private MainActivity activity;
	private GraphViewSeries timeSeries;
	private int index = 0;

	public GraphVisualizer(MainActivity activity, GraphViewSeries timeSeries) {
		this.activity = activity;
		this.timeSeries = timeSeries;
	}

	@Override
	public void update(final Double value) {
		index++;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				timeSeries.appendData(new GraphViewData(index, value), true);
			}
		});
	}

}
