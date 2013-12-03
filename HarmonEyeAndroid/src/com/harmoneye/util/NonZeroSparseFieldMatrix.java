package com.harmoneye.util;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.SparseFieldMatrix;

public class NonZeroSparseFieldMatrix<T extends FieldElement<T>> extends SparseFieldMatrix<T> {

	private final Field<T> field;

	public NonZeroSparseFieldMatrix(Field<T> field, int rowCount, int columnCount) {
		super(field, rowCount, columnCount);
		this.field = field;
	}

	@Override
	public T[] operate(final T[] v) {

		final int nRows = getRowDimension();
		final int nCols = getColumnDimension();
		if (v.length != nCols) {
			throw new DimensionMismatchException(v.length, nCols);
		}

		T zero = field.getZero();
		final T[] out = buildArray(field, nRows);
		for (int row = 0; row < nRows; ++row) {
			T sum = zero;
			for (int i = 0; i < nCols; ++i) {
				T entry = getEntry(row, i);
				if (!zero.equals(entry)) {
					sum = sum.add(entry.multiply(v[i]));
				}
			}
			out[row] = sum;
		}

		return out;
	}

}