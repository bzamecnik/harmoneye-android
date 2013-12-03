package com.harmoneye;

public class PitchClassProfile {
	private double[] pitchClassBins;

	private int halftoneCount;
	private int binsPerHalftone;

	public PitchClassProfile(double[] pitchClassBins, int halftoneCount, int binsPerHalftone) {
		assert halftoneCount > 0;
		assert binsPerHalftone > 0;
		assert pitchClassBins != null;
		assert pitchClassBins.length == halftoneCount * binsPerHalftone;

		this.pitchClassBins = pitchClassBins;
		this.halftoneCount = halftoneCount;
		this.binsPerHalftone = binsPerHalftone;
	}

	public double[] getPitchClassBins() {
		return pitchClassBins;
	}
	
	public int getTotalBinCount() {
		return pitchClassBins.length;
	}

	public int getHalftoneCount() {
		return halftoneCount;
	}

	public int getBinsPerHalftone() {
		return binsPerHalftone;
	}

}
