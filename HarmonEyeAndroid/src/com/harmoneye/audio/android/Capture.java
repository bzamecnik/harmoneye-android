package com.harmoneye.audio.android;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.StopWatch;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.harmoneye.HarmonEyeActivity;
import com.harmoneye.analysis.AnalyzedFrame;
import com.harmoneye.analysis.MusicAnalyzer;
import com.harmoneye.viz.Visualizer;

// TODO: remove direct coupling with MusicAnalyzer!

public class Capture implements Runnable {

	// TODO: figure out the sample rate and other format information
	// automatically as it might vary over devices

	private static final int AUDIO_SAMPLE_RATE = 2 * 11025;
	private static final int AUDIO_BYTES_PER_SAMPLE = 2;
	private static final int AUDIO_BITS_PER_SAMPLE = 8 * AUDIO_BYTES_PER_SAMPLE;
	private static final int AUDIO_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	// signed short to [-1; 1]
	private static final double SHORT_TO_DOUBLE = 2 / (double) 0xffff;

	private AudioRecord recorder;
	private MusicAnalyzer musicAnalyzer;
	private Visualizer<AnalyzedFrame> visualizer;

	private AtomicBoolean running = new AtomicBoolean();
	private AtomicBoolean initialized = new AtomicBoolean();

	private int bufferSizeInBytes;
	private int bufferSizeInSamples;
	private short[] samples;
	private double[] amplitudes;

	public Capture(Visualizer<AnalyzedFrame> visualizer) {
		this.visualizer = visualizer;
	}

	private void initBuffers() {
		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNELS, AUDIO_FORMAT);

		if (bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE || bufferSizeInBytes == AudioRecord.ERROR) {
			throw new UnsupportedOperationException("Could not initialize the buffer.");
		}

		bufferSizeInSamples = bufferSizeInBytes / AUDIO_BYTES_PER_SAMPLE;
		samples = new short[bufferSizeInSamples];
		amplitudes = new double[bufferSizeInSamples];
		Log.i(HarmonEyeActivity.LOG_TAG, "Buffer initialized with size: " + bufferSizeInBytes + " B");
	}

	private void initComponents(Visualizer<AnalyzedFrame> visualizer) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		this.musicAnalyzer = new MusicAnalyzer(visualizer, AUDIO_SAMPLE_RATE, AUDIO_BITS_PER_SAMPLE);
		stopWatch.stop();
		Log.i(HarmonEyeActivity.LOG_TAG, "Initialized the MusicAnalyzer in " + stopWatch.getTime() + " ms");
	}

	public void run() {
		running.set(true);
		
		if (!initialized.get()) {
			initBuffers();
			initComponents(visualizer);
			initialized.set(true);
		}
		
		try {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNELS,
				AUDIO_FORMAT, bufferSizeInBytes);
			if (recorder == null) {
				Log.e(HarmonEyeActivity.LOG_TAG, "Could not initialize the AudioRecord.");
				return;
			}
			recorder.startRecording();
			while (running.get()) {
				// this is a blocking operation - waits until there's enough data
				recorder.read(samples, 0, bufferSizeInSamples);
				toAmplitudes(samples, amplitudes);
				removeMean(amplitudes);
				musicAnalyzer.consume(amplitudes);
			}
			recorder.stop();
		} finally {
			if (recorder != null) {
				recorder.release();
			}
		}
	}

	public void stop() {
		running.set(false);
	}

	public boolean isRunning() {
		return running.get();
	}

	private void toAmplitudes(short[] samples, double[] amplitudes) {
		for (int i = 0; i < bufferSizeInSamples; i++) {
			amplitudes[i] = samples[i] * SHORT_TO_DOUBLE;
		}
	}
	
	// remove DC bias
	private void removeMean(double[] values) {
		double mean = mean(values);
		for (int i = 0; i < values.length; i++) {
			values[i] -= mean; 
		}
	}
	
	private double mean(double[] values) {
		double mean = 0;
		for (int i = 0; i < values.length; i++) {
			mean += values[i];
		}
		mean /= values.length;
		return mean;
	}

	public MusicAnalyzer getMusicAnalyzer() {
		return musicAnalyzer;
	}
}
