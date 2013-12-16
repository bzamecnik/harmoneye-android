package com.harmoneye.analysis;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import android.util.Log;

import com.harmoneye.HarmonEyeActivity;
import com.harmoneye.audio.DecibelCalculator;
import com.harmoneye.audio.MultiRateRingBufferBank;
import com.harmoneye.math.cqt.CqtContext;
import com.harmoneye.math.cqt.FastCqt;
import com.harmoneye.viz.Visualizer;

public class MusicAnalyzer implements SoundConsumer {

	/** [0.0; 1.0] 1.0 = no smoothing */
	private static final double SMOOTHING_FACTOR = 0.25;

	private CqtContext ctx;

	private FastCqt cqt;
	private MultiRateRingBufferBank ringBufferBank;
	private DecibelCalculator dbCalculator;
	private HarmonicPatternPitchClassDetector pcDetector;
	private Visualizer<AnalyzedFrame> visualizer;
	private ExpSmoother binSmoother;

	private double[] samples;
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBins;

	private AtomicBoolean initialized = new AtomicBoolean();
	private static final boolean BIN_SMOOTHER_ENABLED = true;
	private AtomicBoolean updating = new AtomicBoolean();
	
//	ScalarExpSmoother acc = new ScalarExpSmoother(0.01);

	public MusicAnalyzer(Visualizer<AnalyzedFrame> visualizer,
		int sampleRate, int bitsPerSample) {
		this.visualizer = visualizer;

		//@formatter:off
		ctx = CqtContext.create()
			.samplingFreq(sampleRate)
			//.maxFreq((2 << 6) * 65.4063913251)
			.octaves(4)
			.kernelOctaves(1)
			.binsPerHalftone(5)
			.build();
		//@formatter:on

		samples = new double[ctx.getSignalBlockSize()];
		amplitudeSpectrumDb = new double[ctx.getTotalBins()];
		octaveBins = new double[ctx.getBinsPerOctave()];
		
		ringBufferBank = new MultiRateRingBufferBank(ctx.getSignalBlockSize(), ctx.getOctaves());
		dbCalculator = new DecibelCalculator(bitsPerSample);
		pcDetector = new HarmonicPatternPitchClassDetector(ctx);
		binSmoother = new ExpSmoother(ctx.getBinsPerOctave(), SMOOTHING_FACTOR);

		cqt = new FastCqt(ctx);
		cqt.init();
		initialized.set(true);
	}

	@Override
	public void consume(double[] samples) {
		ringBufferBank.write(samples);
		updateSignal();
	}

	public void updateSignal() {
		if (!initialized.get() || updating.get()) {
			return;
		}
		updating.set(true);
		
//		StopWatch sw = new StopWatch();
//		sw.start();
		
		computeCqtSpectrum();
		
		AnalyzedFrame frame = analyzeFrame(amplitudeSpectrumDb);
		visualizer.update(frame);

//		sw.stop();

//		Log.d(HarmonEyeActivity.LOG_TAG, "updateSingal() in " + acc.smooth(sw.getTime()) + " ms");
		updating.set(false);
	}

	private void computeCqtSpectrum() {
		int startIndex = (ctx.getOctaves() - 1) * ctx.getBinsPerOctave();
		for (int octave = 0; octave < ctx.getOctaves(); octave++, startIndex -= ctx.getBinsPerOctave()) {
			ringBufferBank.readLast(octave, samples.length, samples);
			Complex[] cqtSpectrum = cqt.transform(samples);
			toAmplitudeDbSpectrum(cqtSpectrum, amplitudeSpectrumDb, startIndex);
		}
	}

	private void toAmplitudeDbSpectrum(Complex[] cqtSpectrum, double[] amplitudeSpectrum, int startIndex) {
		for (int i = 0; i < cqtSpectrum.length; i++) {
			double amplitude = cqtSpectrum[i].abs();
			double amplitudeDb = dbCalculator.amplitudeToDb(amplitude);
			double value = dbCalculator.rescale(amplitudeDb);
			amplitudeSpectrumDb[startIndex + i] = value;
		}
	}

	private AnalyzedFrame analyzeFrame(double[] allBins) {
		aggregateIntoOctaves(allBins);

		// disabled until it's more optimized 
		
//		double[] pitchClassBins = pcDetector.detectPitchClasses(allBins);
//		
//		octaveBins = enhance(allBins, pitchClassBins, octaveBins);

//		double[] smoothedOctaveBins = smooth(octaveBins);
		double[] smoothedOctaveBins = octaveBins;
		
		AnalyzedFrame pcProfile = new AnalyzedFrame(ctx, allBins, smoothedOctaveBins);
		return pcProfile;
	}

	private void aggregateIntoOctaves(double[] amplitudeSpectrumDb) {
		int binsPerOctave = ctx.getBinsPerOctave();
		for (int i = 0; i < binsPerOctave; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += binsPerOctave) {
				value = FastMath.max(value, amplitudeSpectrumDb[j]);
			}
			octaveBins[i] = value;
		}
	}

	// just an ad hoc reduction of noise and equalization
	private double[] enhance(double[] allBins, double[] pitchClassBinsDb, double[] octaveBins) {
		double max = 0;
		for (int i = 0; i < allBins.length; i++) {
			max = FastMath.max(max, allBins[i]);
		}
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] = FastMath.pow(pitchClassBinsDb[i], 3);
		}
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] *= max;
		}
		for (int i = 0; i < octaveBins.length; i++) {
			octaveBins[i] *= pitchClassBinsDb[i];
		}
		for (int i = 0; i < octaveBins.length; i++) {
			octaveBins[i] = FastMath.pow(octaveBins[i], 1 / 3.0);
		}
		return octaveBins;
	}

	private double[] smooth(double[] octaveBins) {
		double[] smoothedOctaveBins = null;
		if (BIN_SMOOTHER_ENABLED) {
			binSmoother.smooth(octaveBins);
			smoothedOctaveBins = binSmoother.smooth(octaveBins);
		} else {
			smoothedOctaveBins = octaveBins;
		}
		return smoothedOctaveBins;
	}
	
}
