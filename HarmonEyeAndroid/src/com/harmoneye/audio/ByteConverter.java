package com.harmoneye.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConverter {

	// signed short to [-1; 1]
	private static final double normalizationFactor = 2 / (double) 0xffff;

	public static void littleEndianBytesToDoubles(byte[] bytes, double[] doubles) {
		assert bytes.length == 2 * doubles.length;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < doubles.length && byteBuffer.hasRemaining(); i++) {
			doubles[i] = normalizationFactor * byteBuffer.getShort();
		}
	}

}
