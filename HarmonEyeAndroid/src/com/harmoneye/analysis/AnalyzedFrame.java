package com.harmoneye.analysis;

import com.harmoneye.math.cqt.CqtContext;

public class AnalyzedFrame {

	private final double[] allBins;
	private final double[] octaveBins;
	private final CqtContext ctx;

	public AnalyzedFrame(CqtContext ctx, double[] allBins, double[] octaveBins) {
		this.allBins = allBins;
		this.octaveBins = octaveBins;
		this.ctx = ctx;
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
}
