package com.harmoneye.math.stats;

import java.util.Arrays;

// This is a simple O(N*log(N)) median algorithm.
// TODO: implement the O(N) one.
public class Median {
	private double[] sortedValues;
	private int size;
	private boolean isSizeEven;
	private int midIndex;
	private boolean inPlaceSort;

	public Median(int size, boolean inPlaceSort) {
		this.size = size;
		this.inPlaceSort = inPlaceSort;
		sortedValues = new double[size];
		isSizeEven = size % 2 == 0;
		midIndex = size / 2;
	}

	public double evaluate(double[] values) {
		if (!inPlaceSort) {
			System.arraycopy(values, 0, sortedValues, 0, size);
			Arrays.sort(sortedValues);
		}
		if (isSizeEven) {
			return 0.5 * (values[midIndex] + values[midIndex - 1]);
		} else {
			return values[midIndex];
		}
	}
}
