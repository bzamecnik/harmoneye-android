package com.harmoneye.analysis;

import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.viz.Visualizer;

public class RmsAnalyzer implements SoundConsumer {
	
	private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));
	private final double DB_THRESHOLD_INV = 1.0 / DB_THRESHOLD;
	
	private Visualizer<Double> visualizer;

//	private ExpSmoother accumulator = new ExpSmoother(1, 0.25);
//	private double[] frame = new double[1];

	public RmsAnalyzer(Visualizer<Double> visualizer) {
		this.visualizer = visualizer;
	}

	@Override
	public void consume(double[] samples) {
		double rms = computeRms(samples);
//		frame[0] = rms;
//		double smoothed = accumulator.smooth(frame)[0];
		visualizer.update(toDecibel(rms));
	}

	private double computeRms(double[] samples) {
		double sum = 0;
		for (double amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return Math.sqrt(sum / (double) samples.length);
	}
	

	private double toDecibel(double amplitude) {
		// double referenceAmplitude = 1;
		amplitude = Math.abs(amplitude); // / referenceAmplitude
		double amplitudeDb = 20 * Math.log10(amplitude);
		if (amplitudeDb < DB_THRESHOLD) {
			amplitudeDb = DB_THRESHOLD;
		}
		// rescale: [DB_THRESHOLD; 0] -> [-1; 0] -> [0; 1]
		double scaledAmplitudeDb = amplitudeDb * DB_THRESHOLD_INV;
		return 1 - scaledAmplitudeDb;
	}
}
