package com.harmoneye.android;

import com.harmoneye.RmsAnalyzer;
import com.harmoneye.SoundConsumer;
import com.harmoneye.Visualizer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Capture implements Runnable {

	private static final int AUDIO_SAMPLE_RATE = 44100;
	private static final int AUDIO_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	// signed short to [-1; 1]
	private static final double SHORT_TO_DOUBLE = 2 / (double) 0xffff;

	private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));

	private AudioRecord recorder;

	private SoundConsumer soundConsumer;
	private MainActivity activity;

	private boolean running;

	private int bufferSizeInBytes;
	private int bufferSizeInSamples;
	private short[] rawSamples;
	private double[] amplitudes;

	public Capture(MainActivity activity) {

		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNELS, AUDIO_FORMAT);

		if (bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE || bufferSizeInBytes == AudioRecord.ERROR) {
			throw new UnsupportedOperationException("Could not initialize the buffer.");
		}

		bufferSizeInSamples = bufferSizeInBytes / 2;
		rawSamples = new short[bufferSizeInSamples];
		amplitudes = new double[bufferSizeInSamples];
		Log.i(MainActivity.LOG_TAG, "Buffer initialized with size: " + bufferSizeInBytes + " B");

		this.activity = activity;
		Visualizer<Double> visualizer = new GraphVisualizer(activity, activity.getGraphViewSeries());
		this.soundConsumer = new RmsAnalyzer(visualizer);
	}

	public void run() {
		running = true;
		try {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNELS, AUDIO_FORMAT,
				bufferSizeInBytes);
			if (recorder == null) {
				printText("Could not initialize the AudioRecord.");
				return;
			}
			recorder.startRecording();
			while (running) {
				recorder.read(rawSamples, 0, bufferSizeInSamples);
				toAmplitudes(rawSamples, amplitudes);
				toDecibel(amplitudes);
				soundConsumer.consume(amplitudes);
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

	private void printText(final String text) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.printText(text);
			}
		});
	}

	private void toAmplitudes(short[] buffer, double[] amplitudes) {
		for (int i = 0; i < bufferSizeInSamples; i++) {
			amplitudes[i] = buffer[i] * SHORT_TO_DOUBLE;
		}
	}

	private void toDecibel(double[] amplitudes) {
		for (int i = 0; i < amplitudes.length; i++) {
			double amplitude = Math.abs(amplitudes[i]);
			double referenceAmplitude = 1;
			double amplitudeDb = 20 * Math.log10(amplitude / referenceAmplitude);
			if (amplitudeDb < DB_THRESHOLD) {
				amplitudeDb = DB_THRESHOLD;
			}
			double scaledAmplitudeDb = amplitudeDb / DB_THRESHOLD;
			amplitudes[i] = scaledAmplitudeDb;
		}
	}	
}