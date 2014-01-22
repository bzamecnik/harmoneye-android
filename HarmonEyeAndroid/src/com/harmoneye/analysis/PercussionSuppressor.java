package com.harmoneye.analysis;

import com.harmoneye.math.stats.Median;

/**
 * Suppresses percussive components and retain harmonic ones. Implemented via 1D
 * median filtering of subsequent CQ spectra in the time-domain.
 * 
 * Fitzgerald, D.: "Harmonic/Percussive Separation using Median Filtering", 13th
 * International Conference on Digital Audio Effects (DAFX10), Graz, Austria,
 * 2010.
 */
public class PercussionSuppressor {
	private int size;
	private int historySize;
	private double[][] historicValues;
	private double[] filteredValues;
	private double[] historyColumn;

	private Median medianFilter;

	public PercussionSuppressor(int size, int historySize) {
		this.size = size;
		this.historySize = historySize;
		this.historicValues = new double[historySize][size];
		for (int i = 0; i < historySize; i++) {
			historicValues[i] = new double[size];
		}
		historyColumn = new double[historySize];
		filteredValues = new double[size];
		medianFilter = new Median(historySize, true);
	}

	public double[] filter(double[] values) {
		shift(historicValues);
		add(historicValues, values);
		double[] median = getMedian(historicValues);
		return median;
	}

	private void shift(double[][] historicValues) {
		for (int i = 1; i < historySize; i++) {
			double[] from = historicValues[i];
			double[] to = historicValues[i - 1];
			System.arraycopy(from, 0, to, 0, size);
		}
	}

	private void add(double[][] historicValues, double[] values) {
		System.arraycopy(values, 0, historicValues[historySize - 1], 0, size);
	}

	private double[] getMedian(double[][] historicValues) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < historySize; j++) {
				historyColumn[j] = historicValues[j][i];
			}
			filteredValues[i] = medianFilter.evaluate(historyColumn);
		}
		return filteredValues;
	}

}
