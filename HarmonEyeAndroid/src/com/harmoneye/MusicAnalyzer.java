package com.harmoneye;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.cqt.CqtContext;
import com.harmoneye.cqt.FastCqt;
import com.harmoneye.cqt.HarmonicPatternPitchClassDetector;
import com.harmoneye.util.DoubleCircularBuffer;

public class MusicAnalyzer implements SoundConsumer {

	/** [0.0; 1.0] 1.0 = no smoothing */
	private static final double SMOOTHING_FACTOR = 0.25;

	private CqtContext ctx;

	private FastCqt cqt;
	private DoubleCircularBuffer amplitudeBuffer;
	private HarmonicPatternPitchClassDetector pcDetector;
	private Visualizer<PitchClassProfile> visualizer;
	private ExpSmoother binSmoother;

	private double[] amplitudes;
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBinsDb;

	private double dbThreshold;
	private double dbThresholdInv;

	private AtomicBoolean initialized = new AtomicBoolean();

	public MusicAnalyzer(Visualizer<PitchClassProfile> visualizer,
		int sampleRate, int bitsPerSample) {
		this.visualizer = visualizer;

		dbThreshold = -(20 * FastMath.log10(2 << (bitsPerSample - 1)));
		dbThresholdInv = 1.0 / dbThreshold;

		//@formatter:off
		ctx = CqtContext.create()
			.samplingFreq(sampleRate)
			.baseFreq((2 << 2) * 65.4063913251)
			.octaveCount(2)
			.binsPerHalftone(5)
			.build();
		//@formatter:on

		amplitudes = new double[ctx.getSignalBlockSize()];
		octaveBinsDb = new double[ctx.getBinsPerOctave()];
		amplitudeBuffer = new DoubleCircularBuffer(ctx.getSignalBlockSize());
		pcDetector = new HarmonicPatternPitchClassDetector(ctx);
		binSmoother = new ExpSmoother(ctx.getBinsPerOctave(), SMOOTHING_FACTOR);

		cqt = new FastCqt(ctx);
		cqt.init();
		initialized.set(true);
	}

	@Override
	public void consume(double[] samples) {
		amplitudeBuffer.write(samples);
		updateSignal();
	}

	public void updateSignal() {
		if (!initialized.get()) {
			return;
		}
		amplitudeBuffer.readLast(amplitudes, amplitudes.length);
		computeAmplitudeSpectrum(amplitudes);
		double[] pitchClassProfileDb = computePitchClassProfile();
		PitchClassProfile pcProfile = new PitchClassProfile(pitchClassProfileDb,
			ctx.getHalftonesPerOctave(), ctx.getBinsPerHalftone());
		visualizer.update(pcProfile);
	}

	private void computeAmplitudeSpectrum(double[] signal) {
		Complex[] cqSpectrum = cqt.transform(signal);
		if (amplitudeSpectrumDb == null) {
			amplitudeSpectrumDb = new double[cqSpectrum.length];
		}
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			// Since reference amplitude is 1, this code is implied:
			// double referenceAmplitude = 1;
			// amplitude /= referenceAmplitude;
			double amplitudeDb = 20 * FastMath.log10(amplitude);
			if (amplitudeDb < dbThreshold) {
				amplitudeDb = dbThreshold;
			}
			// rescale: [DB_THRESHOLD; 0] -> [-1; 0] -> [0; 1]
			double scaledAmplitudeDb = amplitudeDb * dbThresholdInv;
			amplitudeSpectrumDb[i] = 1 - scaledAmplitudeDb;
		}
	}

	private double[] computePitchClassProfile() {
		int binsPerOctave = ctx.getBinsPerOctave();
		for (int i = 0; i < binsPerOctave; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += binsPerOctave) {
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
		// if (accumulatorEnabled) {
		// accumulator.add(octaveBinsDb);
		// pitchClassProfileDb = accumulator.getAverage();
		// } else {
		binSmoother.smooth(octaveBinsDb);
		pitchClassProfileDb = binSmoother.smooth(octaveBinsDb);
		// }
		// pitchClassProfileDb = octaveBinsDb;
		return pitchClassProfileDb;
	}
}
