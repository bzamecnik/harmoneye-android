package com.harmoneye.analysis;

import com.harmoneye.math.cqt.CqtContext;

public class AnalyzedFrame {

	private final double[] allBins;
	private final double[] octaveBins;
	private final CqtContext ctx;
	private double[] detectedPitchClasses;

	public AnalyzedFrame(CqtContext ctx, double[] allBins, double[] octaveBins) {
		this(ctx, allBins, octaveBins, null);
	}

	public AnalyzedFrame(CqtContext ctx, double[] allBins, double[] octaveBins,
		double[] detectedPitchClasses) {
		this.allBins = allBins;
		this.octaveBins = octaveBins;
		this.ctx = ctx;
		this.detectedPitchClasses = detectedPitchClasses;
	}

	public double[] getAllBins() {
		return allBins;
	}

	public double[] getOctaveBins() {
		return octaveBins;
	}

	public CqtContext getCtxContext() {
		return ctx;
	}

	public double[] getDetectedPitchClasses() {
		return detectedPitchClasses;
	}
}
