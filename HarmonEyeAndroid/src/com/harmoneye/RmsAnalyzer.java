package com.harmoneye;

public class RmsAnalyzer implements SoundConsumer {
	private Visualizer<Double> visualizer;

	public RmsAnalyzer(Visualizer<Double> visualizer) {
		this.visualizer = visualizer;
	}

	@Override
	public void consume(double[] samples) {
		double rms = computeRms(samples);
		visualizer.update(rms);
	}

	private double computeRms(double[] samples) {
		double sum = 0;
		for (double amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return Math.sqrt(sum / (double) samples.length);
	}

}
