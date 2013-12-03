package com.harmoneye.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.AbstractFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrix;

public final class LinkedListNonZeroSparseFieldMatrix<T extends FieldElement<T>> extends AbstractFieldMatrix<T>
	implements InPlaceFieldMatrix<T> {

	private final List<List<SparseEntry<T>>> rows;
	private final int rowCount;
	private final int columnCount;
	private final int nonzeroEntryCount;

	public LinkedListNonZeroSparseFieldMatrix(FieldMatrix<T> other) {
		super(other.getField(), other.getRowDimension(), other.getColumnDimension());

		rowCount = other.getRowDimension();
		columnCount = other.getColumnDimension();

		rows = new ArrayList<List<SparseEntry<T>>>();
		T zero = getField().getZero();
		int nonzeroEntryCount = 0;
		for (int i = 0; i < rowCount; i++) {
			List<SparseEntry<T>> sparseColumns = new LinkedList<SparseEntry<T>>();
			for (int j = 0; j < columnCount; j++) {
				T entry = other.getEntry(i, j);
				if (!zero.equals(entry)) {
					sparseColumns.add(new SparseEntry<T>(entry, j));
					++nonzeroEntryCount;
				}
			}
			rows.add(sparseColumns);
		}
		this.nonzeroEntryCount = nonzeroEntryCount;
	}

	@Override
	public T[] operate(T[] vector) {
		T[] resultVector = buildArray(getField(), rowCount);
		operate(vector, resultVector);
		return resultVector;
	}

	@Override
	public void operate(T[] vector, T[] resultVector) {
		if (vector.length != columnCount) {
			throw new DimensionMismatchException(vector.length, columnCount);
		}

		T zero = getField().getZero();
		int rowIndex = 0;
		for (List<SparseEntry<T>> row : rows) {
			T sum = zero;
			for (SparseEntry<T> columnEntry : row) {
				T entry = columnEntry.getEntry();
				int columnIndex = columnEntry.getIndex();
				sum = sum.add(entry.multiply(vector[columnIndex]));
			}
			resultVector[rowIndex] = sum;
			rowIndex++;
		}
	}

	@Override
	public FieldMatrix<T> createMatrix(int rowDimension, int columnDimension) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldMatrix<T> copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getEntry(int row, int column) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEntry(int row, int column, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addToEntry(int row, int column, T increment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void multiplyEntry(int row, int column, T factor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRowDimension() {
		return rowCount;
	}

	@Override
	public int getColumnDimension() {
		return columnCount;
	}

	private static class SparseEntry<T> {

		T entry;
		int index;

		public SparseEntry(T entry, int index) {
			this.entry = entry;
			this.index = index;
		}

		public T getEntry() {
			return entry;
		}

		public int getIndex() {
			return index;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LinkedListNonZeroSparseFieldMatrix[");
		sb.append("rows=").append(rowCount);
		sb.append(", columns=").append(rowCount);
		sb.append(", nonZeroEntryCount=").append(nonzeroEntryCount).append("\n");
		sb.append("entries=[\n");
//		for (List<SparseEntry<T>> columns : rows) {
//			sb.append("[");
//			for (SparseEntry<T> entry : columns) {
//				sb.append("(").append(entry.getIndex()).append(") ").append(entry.getEntry()).append(", ");
//			}
//			sb.append("]");
//			sb.append("\n");
//		}
		sb.append("]\n");
		sb.append("]");
		return sb.toString();
	}
}