package com.harmoneye.viz;

import android.graphics.Color;

public interface ColorFunction {
	/**
	 * Converts a value into a color.
	 * 
	 * @param value from interval [0.0; 1.0]
	 * @return color encoded as {@link Color}
	 */
	int toColor(float value);
}
