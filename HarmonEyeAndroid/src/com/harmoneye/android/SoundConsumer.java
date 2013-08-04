package com.harmoneye.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SoundConsumer implements Runnable {

	private static final int AUDIO_SAMPLE_RATE = 44100;
	private static final int AUDIO_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	// signed short to [-1; 1]
	private static final double SHORT_TO_DOUBLE = 2 / (double) 0xffff;

	private boolean running;
	private AudioRecord recorder;

	private int bufferSizeInBytes;
	private int bufferSizeInSamples;
	private short[] buffer;

	private MainActivity activity;

	public SoundConsumer(MainActivity activity) {
		this.activity = activity;

		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNELS, AUDIO_FORMAT);

		if (bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE || bufferSizeInBytes == AudioRecord.ERROR) {
			throw new UnsupportedOperationException("Could not initialize the buffer.");
		}

		bufferSizeInSamples = bufferSizeInBytes / 2;
		buffer = new short[bufferSizeInSamples];
		Log.i(MainActivity.LOG_TAG, "Buffer initialized with size: " + bufferSizeInBytes + " B");
	}

	public void run() {
		running = true;
		try {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNELS,
				AUDIO_FORMAT, bufferSizeInBytes);
			if (recorder == null) {
				updateTextView("Could not initialize the AudioRecord.");
				return;
			}
			recorder.startRecording();
			while (running) {
				recorder.read(buffer, 0, bufferSizeInSamples);
				double rms = getRms(buffer);
				updateTextView("> " + doubleToStars(rms));
			}
			recorder.stop();
		} finally {
			if (recorder != null) {
				recorder.release();
			}
		}
	}

	public synchronized void stop() {
		running = false;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	private static double getRms(short[] values) {
		double sum = 0;
		for (short value : values) {
			double amplitide = value * SHORT_TO_DOUBLE;
			sum += amplitide * amplitide;
		}
		return Math.sqrt(sum / (double) values.length);
	}

	private static String doubleToStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int starCount = (int) Math.round(amplitude * 100);
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		return sb.toString();
	}

	private void updateTextView(final String text) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.printText(text);
			}
		});
	}
}