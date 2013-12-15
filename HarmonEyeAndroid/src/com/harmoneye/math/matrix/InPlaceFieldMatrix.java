package com.harmoneye.math.matrix;

import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.linear.FieldMatrix;

public interface InPlaceFieldMatrix<T extends FieldElement<T>> extends FieldMatrix<T> {
	/**
	 * @param vector
	 * @param resultVector already allocated array for the result vector
	 */
	public void operate(T[] vector, T[] resultVector);
}
