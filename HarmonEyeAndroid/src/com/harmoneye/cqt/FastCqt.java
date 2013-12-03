package com.harmoneye.cqt;

import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.transform.TransformUtils;

import com.harmoneye.util.InPlaceFieldMatrix;
import com.harmoneye.util.LinkedListNonZeroSparseFieldMatrix;
import com.harmoneye.util.NonZeroSparseFieldMatrix;

public class FastCqt implements Cqt {

	protected CqtContext ctx;
	protected CqtCalculator calc;

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	private InPlaceFieldMatrix<Complex> spectralKernels;
	private Complex[] transformedSignal;
	private double[][] dataRI;

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
		transformedSignal = new Complex[spectralKernels.getRowDimension()];
	}

	@Override
	public Complex[] transform(double[] signal) {
		calc.padRight(signal, dataRI[0]);
		Arrays.fill(dataRI[1], 0);

		FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);

		// TODO: in-place toComplexArray 
		Complex[] spectrum = TransformUtils.createComplexArray(dataRI);

		spectralKernels.operate(spectrum, transformedSignal);

		Complex normalizationFactor = ctx.getNormalizationFactor();
		for (int i = 0; i < transformedSignal.length; i++) {
			transformedSignal[i] = transformedSignal[i].multiply(normalizationFactor);
		}

		return transformedSignal;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
		int rows = ctx.getTotalBins();
		int columns = ctx.getSignalBlockSize();
		//System.out.println("rows x columns: " + rows + "x" + columns + ", total: " + rows * columns);
		NonZeroSparseFieldMatrix<Complex> kernels = new NonZeroSparseFieldMatrix<Complex>(field, rows, columns);
		for (int k = 0; k < rows; k++) {
			kernels.setRow(k, calc.conjugate(calc.spectralKernel(k)));
		}
		kernels.transpose();
		spectralKernels = new LinkedListNonZeroSparseFieldMatrix<Complex>(kernels);
	}

	public CqtContext getContext() {
		return ctx;
	}
}
