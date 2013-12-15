package com.harmoneye.analysis;

class ScalarMovingAverageAccumulator {
	double data;
	int frameCount;

	public double getAverage() {
		return data;
	}

	public double add(double currentValue) {
		frameCount++;

		double weight = 1.0 / frameCount;

		data = (1 - weight) * data + weight * currentValue;

		return data;
	}

	public void reset() {
		frameCount = 0;
	}
}