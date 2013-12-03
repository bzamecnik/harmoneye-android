package com.harmoneye.cqt;

import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.cqt.window.WindowFunction;

public class CqtCalculator {

	private CqtContext ctx;

	private FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	public CqtCalculator(CqtContext ctx) {
		this.ctx = ctx;
	}

	public double centerFreq(int k) {
		// (k - binsPerHalftoneHalf) instead of k
		// in order to put the center frequency in the center bin for that halftone
		return (ctx.getBaseFreq() * FastMath.pow(2, (k - ctx.getBinsPerHalftone() / 2) * ctx.getBinsPerOctaveInv()));
	}

	public int bandWidth(int k) {
		return (int) FastMath.ceil(ctx.getQ() * ctx.getSamplingFreq() / centerFreq(k));
	}

	protected Complex[] temporalKernel(int k) {
		int size = bandWidth(k);
		Complex[] coeffs = new Complex[size];
		double sizeInv = 1.0 / size;
		double factor = 2 * FastMath.PI * ctx.getQ() * sizeInv;
		for (int i = 0; i < size; i++) {
			Complex value = ComplexUtils.polar2Complex(ctx.getWindow().value(i * sizeInv) * sizeInv, i * factor);
			coeffs[i] = value;
		}
		return coeffs;
	}

	public Complex[] spectralKernel(int k) {
		Complex[] temporalKernel = padLeft(temporalKernel(k), ctx.getSignalBlockSize());
		Complex[] spectrum = fft.transform(temporalKernel, TransformType.FORWARD);

		chop(spectrum);
		return spectrum;
	}

	public double windowIntegral(WindowFunction window) {
		UnivariateIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(100, window, 0, 1);
	}

	// http://acius2.blogspot.cz/2007/11/calculating-next-power-of-2.html
	public int nextPowerOf2(int value) {
		value--;
		value |= (value >> 1);
		value |= (value >> 2);
		value |= (value >> 4);
		value |= (value >> 8);
		value |= (value >> 16);
		value++;
		return value;
	}

	public void chop(Complex[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].abs() < ctx.getChopThreshold()) {
				values[i] = Complex.ZERO;
			}
		}
	}

	public Complex[] conjugate(Complex[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i].conjugate();
		}
		return values;
	}

	public Complex[] padLeft(Complex[] values, int totalSize) {
		Complex[] padded = new Complex[totalSize];
		int dataSize = FastMath.min(values.length, totalSize);
		int paddingSize = totalSize - dataSize;

		for (int i = 0; i < paddingSize; i++) {
			padded[i] = Complex.ZERO;
		}

		System.arraycopy(values, 0, padded, paddingSize, dataSize);

		return padded;
	}

	public void padRight(double[] in, double[] padded) {
		int dataSize = FastMath.min(in.length, padded.length);

		System.arraycopy(in, 0, padded, 0, dataSize);

		for (int i = dataSize; i < padded.length; i++) {
			padded[i] = 0;
		}
	}

	@Deprecated
	public Complex[] padRight(Complex[] values, int totalSize) {
		Complex[] padded = new Complex[totalSize];
		int size = FastMath.min(values.length, totalSize);
		for (int i = 0; i < size; i++) {
			padded[i] = values[i];
		}
		for (int i = values.length; i < totalSize; i++) {
			padded[i] = Complex.ZERO;
		}
		return padded;
	}

	@Deprecated
	public double[] padRight(double[] values, int totalSize) {
		double[] padded = new double[totalSize];
		int size = FastMath.min(values.length, totalSize);
		for (int i = 0; i < size; i++) {
			padded[i] = values[i];
		}
		for (int i = values.length; i < totalSize; i++) {
			padded[i] = 0;
		}
		return padded;
	}

	public double sum(double[] values) {
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum;
	}

}
