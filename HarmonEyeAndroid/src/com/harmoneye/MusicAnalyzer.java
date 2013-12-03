package com.harmoneye;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.cqt.CqtContext;
import com.harmoneye.cqt.FastCqt;
import com.harmoneye.cqt.HarmonicPatternPitchClassDetector;
import com.harmoneye.util.DoubleCircularBuffer;

public class MusicAnalyzer implements SoundConsumer {

	private CqtContext ctx = CqtContext.create()
			.samplingFreq(44100)
			.baseFreq(16 * 65.4063913251)
			.octaveCount(1)
			.binsPerHalftone(2)
			.build();
	private FastCqt cqt = new FastCqt(ctx);

	private final double DB_THRESHOLD = -(20 * FastMath.log10(2 << (16 - 1)));
	private final double DB_THRESHOLD_INV = 1.0 / DB_THRESHOLD; 
	private final int BINS_PER_HALFTONE = ctx.getBinsPerHalftone();
	private final int PITCH_BIN_COUNT = ctx.getBinsPerOctave();
	private final int HALFTONE_PER_OCTAVE_COUNT = ctx.getHalftonesPerOctave();

	// in samples
	private int signalBlockSize = ctx.getSignalBlockSize();

	private double[] amplitudes = new double[signalBlockSize];
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];

	private DoubleCircularBuffer amplitudeBuffer = new DoubleCircularBuffer(signalBlockSize);
	
	private HarmonicPatternPitchClassDetector pcDetector = new HarmonicPatternPitchClassDetector(ctx);
	
	private Visualizer<PitchClassProfile> visualizer;

	private boolean initialized;
	
	public MusicAnalyzer(Visualizer<PitchClassProfile> visualizer) {
		this.visualizer = visualizer;
		cqt.init();
		initialized = true;
	}

	@Override
	public void consume(double[] samples) {
		amplitudeBuffer.write(samples);
		updateSignal();
	}
	
	public void updateSignal() {
		if (!initialized) {
			return;
		}
		amplitudeBuffer.readLast(amplitudes, amplitudes.length);
		computeAmplitudeSpectrum(amplitudes);
		double[] pitchClassProfileDb = computePitchClassProfile();
		PitchClassProfile pcProfile = new PitchClassProfile(pitchClassProfileDb, HALFTONE_PER_OCTAVE_COUNT,
			BINS_PER_HALFTONE);
		visualizer.update(pcProfile);
	}

	private void computeAmplitudeSpectrum(double[] signal) {
		Complex[] cqSpectrum = cqt.transform(signal);
		if (amplitudeSpectrumDb == null) {
			amplitudeSpectrumDb = new double[cqSpectrum.length];
		}
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			double referenceAmplitude = 1;
			double amplitudeDb = 20 * FastMath.log10(amplitude / referenceAmplitude);
			if (amplitudeDb < DB_THRESHOLD) {
				amplitudeDb = DB_THRESHOLD;
			}
			double scaledAmplitudeDb = 1 - amplitudeDb * DB_THRESHOLD_INV;
			amplitudeSpectrumDb[i] = scaledAmplitudeDb;
		}
	}

	private double[] computePitchClassProfile() {
		for (int i = 0; i < PITCH_BIN_COUNT; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
				value = FastMath.max(value, amplitudeSpectrumDb[j]);
			}
			octaveBinsDb[i] = value;
		}

		double[] pitchClassBinsDb = pcDetector.detectPitchClasses(amplitudeSpectrumDb);
		double max = 0;
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			max = FastMath.max(max, amplitudeSpectrumDb[i]);
		}
		// just an ad hoc reduction of noise and equalization
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] = FastMath.pow(pitchClassBinsDb[i], 3);
		}
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] *= max;
		}
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] *= pitchClassBinsDb[i];
		}
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] = FastMath.pow(octaveBinsDb[i], 1 / 3.0);
		}

		double[] pitchClassProfileDb = null;
//		if (accumulatorEnabled) {
//			accumulator.add(octaveBinsDb);
//			pitchClassProfileDb = accumulator.getAverage();
//		} else {
//			binSmoother.smooth(octaveBinsDb);
//			pitchClassProfileDb = binSmoother.smooth(octaveBinsDb);
//		}
		pitchClassProfileDb = octaveBinsDb;
		return pitchClassProfileDb;
	}
}
