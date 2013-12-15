package com.harmoneye.audio;

/**
 * Lock-free ring buffer.
 * 
 * Two concurrent threads might overwrite each other's data.
 */
public class DoubleRingBuffer {

	private final int bufferSize;
	private double[] buffer;
	private volatile int endIndex = 0;

	public DoubleRingBuffer(int bufferSize) {
		this.bufferSize = bufferSize;
		this.buffer = new double[bufferSize];
	}

	/**
	 * Appends the given data to the end of the buffer.
	 * 
	 * Older data get overwritten.
	 * 
	 * @param samples
	 */
	public void write(double[] samples) {
		int index = endIndex;
		endIndex = incrementIndex(endIndex, samples.length);
		for (int i = 0; i < samples.length; i++) {
			buffer[index] = samples[i];
			index = incrementIndex(index, 1);
		}
	}

	/**
	 * Reads last {@code length} elements appended to the buffer (from the end)
	 * into the provided {@code result} array.
	 * @param length can be lower or equal to result.length
	 * @param result
	 */
	public void readLast(int length, double[] result) {
		int index = incrementIndex(endIndex, -length);
		for (int i = 0; i < length; i++) {
			result[i] = buffer[index];
			index = incrementIndex(index, 1);
		}
	}

	private int incrementIndex(int value, int increment) {
		return (value + increment + bufferSize) % bufferSize;
	}
}
