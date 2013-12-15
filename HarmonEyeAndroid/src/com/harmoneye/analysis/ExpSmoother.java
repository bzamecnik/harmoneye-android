package com.harmoneye.analysis;

class ExpSmoother {
	double[] data;
	double currentWeight;
	double previousWeight;

	public ExpSmoother(int size, double currentWeight) {
		data = new double[size];
		this.currentWeight = currentWeight;
		previousWeight = 1 - currentWeight;
	}

	public double[] smooth(double[] currentFrame) {
		assert data.length == currentFrame.length;

		for (int i = 0; i < data.length; i++) {
			data[i] = previousWeight * data[i] + currentWeight * currentFrame[i];
		}
		return data;
	}
}