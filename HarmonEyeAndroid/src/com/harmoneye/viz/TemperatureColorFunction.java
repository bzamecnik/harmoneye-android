package com.harmoneye.viz;

import android.graphics.Color;

public class TemperatureColorFunction implements ColorFunction {

	public int toColor(float value) {
		float h = (1.8f - value) % 1.0f;
		float s = 0.25f + 0.75f * value;
		float v = s;
		return Color.HSVToColor(new float[] { h, s, v });
	}
}
