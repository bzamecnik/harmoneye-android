package com.harmoneye.math.cqt;

import java.util.Arrays;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.harmoneye.math.matrix.InPlaceFieldMatrix;
import com.harmoneye.math.matrix.LinkedListNonZeroSparseFieldMatrix;
import com.harmoneye.math.matrix.NonZeroSparseFieldMatrix;

public class FastCqt implements Cqt {

	protected CqtContext ctx;
	protected CqtCalculator calc;

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	private InPlaceFieldMatrix<Complex> spectralKernels;
	private Complex[] cqtSpectrum;
	private double[][] dataRI;

	// allocated result
	private Complex[] dftSpectrum;

	public FastCqt(CqtContext ctx) {
		this.ctx = ctx;
		this.calc = new CqtCalculator(ctx);
	}

	public void init() {
		if (spectralKernels == null) {
			computeSpectralKernels();
		}

		int signalBlockSize = ctx.getSignalBlockSize();
		dataRI = new double[][] { new double[signalBlockSize], new double[signalBlockSize] };
		cqtSpectrum = new Complex[spectralKernels.getRowDimension()];
	}

	@Override
	public Complex[] transform(double[] signal) {
		// StopWatch sw = new StopWatch();
		// sw.start();

		// Use only the real part of the signal.
		// Right padding for real-time usage - minimal latency.
		calc.padRight(signal, dataRI[0]);
		// The imaginary part is zero.
		Arrays.fill(dataRI[1], 0);

		FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);

		dftSpectrum = toComplexArray(dataRI, dftSpectrum);

		// Transform the DFT spectrum bins into CQT spectrum bins using the
		// precomputed matrix of kernels (in frequency domain).
		spectralKernels.operate(dftSpectrum, cqtSpectrum);

		double normalizationFactor = ctx.getNormalizationFactor();
		for (int i = 0; i < cqtSpectrum.length; i++) {
			cqtSpectrum[i] = cqtSpectrum[i].multiply(normalizationFactor);
		}

		// sw.stop();
		// System.out.println("Computed transformed signal in " +sw.getNanoTime() * 0.001 + " us");

		return cqtSpectrum;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
		int rows = ctx.getKernelBins();
		int columns = ctx.getSignalBlockSize();
		System.out.println("rows x columns: " + rows + "x" + columns
				+ ", total: " + rows * columns);
		StopWatch sw = new StopWatch();
		sw.start();
		NonZeroSparseFieldMatrix<Complex> kernels = new NonZeroSparseFieldMatrix<Complex>(
				field, rows, columns);
		for (int k = 0; k < rows; k++) {
			kernels.setRow(k, calc.conjugate(calc.spectralKernel(k)));
		}
		kernels.transpose();
		spectralKernels = new LinkedListNonZeroSparseFieldMatrix<Complex>(kernels);
		sw.stop();
		System.out.println("Computed kernels in " + sw.getTime() + " ms");
		System.out.println(spectralKernels);
	}

	public CqtContext getContext() {
		return ctx;
	}

	private Complex[] toComplexArray(double[][] RI, Complex[] result) {
		// in-place variant of TransformUtils.createComplexArray(dataRI);

		if (dataRI.length != 2) {
			throw new DimensionMismatchException(dataRI.length, 2);
		}
		final double[] dataR = dataRI[0];
		final double[] dataI = dataRI[1];
		if (dataR.length != dataI.length) {
			throw new DimensionMismatchException(dataI.length, dataR.length);
		}

		final int n = dataR.length;
		if (result == null || result.length != n) {
			result = new Complex[n];
		}
		for (int i = 0; i < n; i++) {
			result[i] = new Complex(dataR[i], dataI[i]);
		}
		return result;

	}
}
