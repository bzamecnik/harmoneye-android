package com.harmoneye.math.window;

import org.apache.commons.math3.util.FastMath;

public class BlackmanWindow implements WindowFunction {

	private static final double TWO_PI = 2 * FastMath.PI;

	@Override
	public double value(double x) {
		double alpha = 0.16;
		double a0 = 0.5 * (1 - alpha);
		double a1 = 0.5;
		double a2 = 0.5 * alpha;
		return a0 - a1 * FastMath.cos(TWO_PI * x) + a2
			* FastMath.cos(2 * TWO_PI * x);
	}

}
