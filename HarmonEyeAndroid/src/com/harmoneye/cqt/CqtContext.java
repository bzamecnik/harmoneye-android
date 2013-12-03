package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.cqt.window.HammingWindow;
import com.harmoneye.cqt.window.WindowFunction;

public final class CqtContext {
	private int octaveCount = 6;
	private double baseFreq = 65.4063913251;
	private double samplingFreq = 22050;
	private int halftonesPerOctave = 12;
	private int binsPerHalftone = 5;
	private WindowFunction window = new HammingWindow();
	private double chopThreshold = 0.005;

	private double maxFreq;
	private int binsPerOctave;
	private double binsPerOctaveInv;
	private int totalBins;
	private double q;
	private double windowIntegral;
	private int signalBlockSize;
	private Complex normalizationFactor;

	private CqtContext() {
	}

	public static Builder create() {
		return new Builder();
	}

	private void update() {
		CqtCalculator calc = new CqtCalculator(this);
		maxFreq = FastMath.pow(2, octaveCount) * baseFreq;
		binsPerOctave = halftonesPerOctave * binsPerHalftone;
		binsPerOctaveInv = 1.0 / binsPerOctave;
		totalBins = (int) FastMath.ceil(binsPerOctave * FastMath.log(2, maxFreq / baseFreq));
		q = 1 / (FastMath.pow(2, binsPerOctaveInv) - 1);
		windowIntegral = calc.windowIntegral(window);

		signalBlockSize = calc.nextPowerOf2(calc.bandWidth(0));
		normalizationFactor = new Complex(2 / (signalBlockSize * windowIntegral));
	}

	public int getOctaveCount() {
		return octaveCount;
	}

	public void setOctaveCount(int octaveCount) {
		this.octaveCount = octaveCount;
	}

	public double getBaseFreq() {
		return baseFreq;
	}

	public void setBaseFreq(double baseFreq) {
		this.baseFreq = baseFreq;
	}

	public double getSamplingFreq() {
		return samplingFreq;
	}

	public void setSamplingFreq(double samplingFreq) {
		this.samplingFreq = samplingFreq;
	}

	public int getHalftonesPerOctave() {
		return halftonesPerOctave;
	}

	public void setHalftonesPerOctave(int halftonesPerOctave) {
		this.halftonesPerOctave = halftonesPerOctave;
	}

	public int getBinsPerHalftone() {
		return binsPerHalftone;
	}

	public void setBinsPerHalftone(int binsPerHalftone) {
		this.binsPerHalftone = binsPerHalftone;
	}

	public WindowFunction getWindow() {
		return window;
	}

	public void setWindow(WindowFunction window) {
		this.window = window;
	}

	public double getChopThreshold() {
		return chopThreshold;
	}

	public void setChopThreshold(double chopThreshold) {
		this.chopThreshold = chopThreshold;
	}

	public double getMaxFreq() {
		return maxFreq;
	}

	public int getBinsPerOctave() {
		return binsPerOctave;
	}

	public double getBinsPerOctaveInv() {
		return binsPerOctaveInv;
	}

	public int getTotalBins() {
		return totalBins;
	}

	public double getQ() {
		return q;
	}

	public double getWindowIntegral() {
		return windowIntegral;
	}

	public int getSignalBlockSize() {
		return signalBlockSize;
	}

	public Complex getNormalizationFactor() {
		return normalizationFactor;
	}

	public static class Builder {

		private CqtContext ctx = new CqtContext();

		public CqtContext build() {
			ctx.update();
			return ctx;
		}

		public Builder octaveCount(int octaveCount) {
			ctx.setOctaveCount(octaveCount);
			return this;
		}

		public Builder baseFreq(double baseFreq) {
			ctx.setBaseFreq(baseFreq);
			return this;
		}

		public Builder samplingFreq(double samplingFreq) {
			ctx.setSamplingFreq(samplingFreq);
			return this;
		}

		public Builder halftonesPerOctave(int halftonesPerOctave) {
			ctx.setHalftonesPerOctave(halftonesPerOctave);
			return this;
		}

		public Builder binsPerHalftone(int binsPerHalftone) {
			ctx.setBinsPerHalftone(binsPerHalftone);
			return this;
		}

		public Builder window(WindowFunction window) {
			ctx.setWindow(window);
			return this;
		}

		public Builder chopThreshold(int chopThreshold) {
			ctx.setChopThreshold(chopThreshold);
			return this;
		}
	}
}
