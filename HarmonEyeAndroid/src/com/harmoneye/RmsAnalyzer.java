package com.harmoneye;

public class RmsAnalyzer implements SoundConsumer {
	private Visualizer<Double> visualizer;

	private ExpSmoother accumulator = new ExpSmoother(1, 0.25);
	private double[] frame = new double[1];

	public RmsAnalyzer(Visualizer<Double> visualizer) {
		this.visualizer = visualizer;
	}

	@Override
	public void consume(double[] samples) {
		double rms = computeRms(samples);
		frame[0] = rms;
		visualizer.update(accumulator.smooth(frame)[0]);
	}

	private double computeRms(double[] samples) {
		double sum = 0;
		for (double amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return Math.sqrt(sum / (double) samples.length);
	}
}
