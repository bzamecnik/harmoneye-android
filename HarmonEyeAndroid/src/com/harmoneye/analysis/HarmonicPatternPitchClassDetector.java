package com.harmoneye.analysis;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.cqt.CqtCalculator;
import com.harmoneye.math.cqt.CqtContext;

public class HarmonicPatternPitchClassDetector {

	private static final int DEFAULT_HARMONIC_COUNT = 7;

	private CqtCalculator calc;

	private int harmonicCount;
	private int binsPerOctave;
	private int binsPerHalftoneHalf;
	private double baseFreq;

	public HarmonicPatternPitchClassDetector(CqtContext ctx) {
		this(ctx, DEFAULT_HARMONIC_COUNT);
	}

	public HarmonicPatternPitchClassDetector(CqtContext ctx, int harmonicCount) {
		this.calc = new CqtCalculator(ctx);
		this.harmonicCount = harmonicCount;
		binsPerOctave = ctx.getBinsPerOctave();
		binsPerHalftoneHalf = ctx.getBinsPerHalftone() / 2;
		baseFreq = ctx.getBaseFreq();
	}

	/**
	 * detect fundamental frequencies for bins within pitch classes via pattern
	 * matching of the harmonics
	 * 
	 * @param cqBins
	 * @return
	 */
	public double[] detectPitchClasses(double[] cqBins) {
		double[] octaveBins = new double[binsPerOctave];
		for (int i = 0; i < cqBins.length; i++) {
			octaveBins[i % binsPerOctave] += calc.sum(extractHarmonics(cqBins, i, harmonicCount));
		}

		//			double[] octaveBins = new double[cqBins.length];
		//			for (int i = 0; i < cqBins.length; i++) {
		//				octaveBins[i] = sum(extractHarmonics(cqBins, i, HARMONIC_COUNT));
		//			}

		// normalize
		double max = 0;
		for (int i = 0; i < octaveBins.length; i++) {
			max = FastMath.max(max, octaveBins[i]);
		}
		if (max > 0) {
			for (int i = 0; i < octaveBins.length; i++) {
				octaveBins[i] /= max;
			}
		}

		return octaveBins;
	}

	private double[] extractHarmonics(double[] cqBins, int baseFreqBin, int harmonicCount) {
		double[] harmonics = new double[harmonicCount];
		for (int i = 1; i <= harmonicCount; i++) {
			double frequency = calc.centerFreq(baseFreqBin);
			int harmonicBin = harmonicFreqBin(frequency, i);
			if (harmonicBin < cqBins.length) {
				double weight = 1 - 0.4 * i / (double) (harmonicCount - 1);
				harmonics[i - 1] = weight * cqBins[harmonicBin];
			}
		}
		return harmonics;
	}

	/**
	 * @param frequency of the first harmonic
	 * @param harmonicIndex one-based index of the desired k-th harmonic
	 * @return zero-based cq bin index of the k-th harmonic frequency
	 */
	private int harmonicFreqBin(double frequency, int harmonicIndex) {
		return (int) FastMath.round(binsPerOctave * FastMath.log(2, harmonicIndex * frequency / baseFreq))
			+ binsPerHalftoneHalf;
	}
}
