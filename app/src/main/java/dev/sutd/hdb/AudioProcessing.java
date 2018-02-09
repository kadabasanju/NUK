package dev.sutd.hdb;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.renderscript.Element;

import java.io.File;
import java.io.IOException;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import util.WavFile;
import util.WavFileException;
import models.DataType;

public class AudioProcessing {
	public static final int SAMPLE_RATE = 44100;

	public String audioName;
	// public static final String AudioName = Environment
	// .getExternalStorageDirectory() + "/SoundDetect/2211.raw";
	public byte[] audiodata;
	public boolean humanVoice = false;
	public boolean music = false;
	public double[] audio_double_data;
	public double[] audio_filter_data;
	public double max_data = 0;
	public static double[] aCo = { 1, -5.860765860583556, 14.331876262224391,
			-18.718178812173540, 13.771018467868856, -5.411175119705352,
			0.887225088964557 };

	public static double[] bCo = { 0.000102839128416, 0, -0.000308517385250, 0,
			0.000308517385250, 0, -0.000102839128416 };
	public double LEN = 0;
	public double aveV = 0;
	public double emplitudeSum = 0;
	public int FRAME = 1764; // Frame size is 1764 = 44100*0.04 （40ms）
	private double[] RMS; // Root Mean Square

	private int sustain = 0;
	private int sustainT = 0;

	DataType data = new DataType();

	String audioRecordPath = Environment.getExternalStorageDirectory()
			+ "/SoundDetect/";

	AudioProcessing(String AUDIONAME) {
		this.audioName = AUDIONAME;
	}

	public DataType startProcessing() {
		boolean[] result = new boolean[2];
		readWaveFile();

		/* sound DB */
		data.averagedb = soundDB();

		/* human voice */
		data.speak = humanVoiceDetect();
		
		// Intent sendIntent = new Intent();
		// sendIntent.putExtra("data", data);
		deleteFile(new File(audioRecordPath));
		return data;

	}

	// TODO Auto-generated method stub
	private void readWaveFile() {
		File file = new File(audioRecordPath, audioName);
		if (file.exists()) {

			try {
				WavFile wavFile = WavFile.openWavFile(file);
				int numChannels = wavFile.getNumChannels();
				long numFrames = wavFile.getNumFrames();
				long sampleRate = wavFile.getSampleRate();
				int BufferSize = 44100 * 10;
				int newSize = 0;
				BufferSize = (int) numFrames;
				int dropLen = (int) (5 * 0.04 * SAMPLE_RATE);
				newSize = BufferSize - dropLen;
				
				// audio_double_data = new double[BufferSize];
				double[] audioOriginalData = new double[BufferSize];
				audio_double_data = new double[newSize];
				audio_filter_data = new double[newSize];
				audiodata = new byte[BufferSize];
				LEN = newSize;
				int times = wavFile.readFrames(audioOriginalData, BufferSize);
				for (int i = 0; i < newSize; i++) {
					audio_double_data[i] = audioOriginalData[i + dropLen];
				}

				wavFile.close();

			

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WavFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private boolean humanVoiceDetect() {
		// TODO Auto-generated method stub
		/* Filter */
		filter();
		double[] audioFilterData = meanAmplitude();
		double[] RMS = RMS(audioFilterData);
		double Judge = 0;
		/* judge */
		if (aveV < 0.01) {
			Judge = aveV * 3;
		} else if (emplitudeSum > 20000) {
			Judge = aveV * 1.5;
		} else {
			Judge = aveV * 2;
		}

		for (int i = 0; i < RMS.length - 3; i++) {
			if (RMS[i] > Judge) {
				int k = 1;
				// if (i + k > RMS.length - 1) {
				// break;
				// } else {
				while ((i + k < RMS.length - 1) && (RMS[i + k] > Judge)) {

					k = k + 1;
					if (i + k > RMS.length) {
						break;
					}
					if (k == 4) {
						sustainT += 1;
					}

				}
				if (k > sustain) {
					sustain = k;
				}

				i = i + k;
				// }
			}
		}
		data.sustainT = sustainT;
		data.sustainS = sustain;
		if (sustainT > 2 && sustain > 9) {
			
			return true;
		} else {
			return false;
		}
	}

	/* Filter */
	protected void filter() {
		for (int i = 0; i < audio_double_data.length; i++) {
			for (int j = 0; (j <= i) && (j < aCo.length); j++) {
				audio_filter_data[i] += bCo[j] * audio_double_data[i - j]
						- aCo[j] * audio_filter_data[i - j];
			}
			// audio_filter_data[i] = sum;
			// sum = 0;
		}
		
	}

	/* average of amplitude */
	protected double[] meanAmplitude() {
		/* audioFilterData is absolute value of audio_filter_data */
		double[] audioFilterData = new double[audio_filter_data.length];

		for (int i = 0; i < audio_filter_data.length; i++) {
			audioFilterData[i] = Math.abs(audio_filter_data[i]);
			emplitudeSum += audioFilterData[i];
		}

		aveV = emplitudeSum / LEN;
		
		return audioFilterData;
	}

	/* RMS */
	protected double[] RMS(double[] audioFilterData) {
		int times = (int) Math.floor(LEN / FRAME);
		RMS = new double[times];
		double sum2 = 0;
		for (int i = 0; i < times; i++) {
			for (int k = 0; k < FRAME; k++) {
				sum2 = sum2 + Math.pow(audioFilterData[i * FRAME + k], 2);
			}
			RMS[i] = Math.sqrt(sum2 / FRAME);
			sum2 = 0;
		}
		return RMS;
	}

	/* audio DB */
	public double soundDB() {
		int frameSize = 256;
		int overLap = 128;
		int times = (int) Math.floor(LEN / frameSize);
		double[] audioDB = new double[times];
		double summDB = 0;
		double aveDB;
		double Max = 0;
		for (int i = 0; i < times; i++) {
			double sum = 0;
			for (int j = 0; j < frameSize; j++) {
				double temp = Math.abs(audio_double_data[i * frameSize + j]);
				if (temp > Max) {
					Max = temp;
				}
				sum = sum + Math.pow(audio_double_data[i * frameSize + j], 2);
			}
			audioDB[i] = 10 * Math.log10(sum);
			summDB += audioDB[i];
			
		}
		/* normalize */
		for (int i = 0; i < LEN; i++) {
			audio_double_data[i] = audio_double_data[i] / Max;
		}

		if (audioDB.length > 1) {
			aveDB = summDB / audioDB.length;

			return aveDB;
		} else {
			return 0;
		}

	}

	/* ZCR Zero Crossing Rate */
	protected double ZCR(int Frame, int times, double[] source) {
		double sum_temp = 0;
		double zcrSum = 0;
		double[] zCR = new double[times];
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < Frame; j++) {
				if (i * Frame + j > 1) {

					sum_temp = sum_temp
							+ Math.abs(Math.signum(source[i * Frame + j])
									- Math.signum(source[i * Frame + j - 1]));
				}
			}
			zCR[i] = sum_temp / 2;
			sum_temp = 0;
			zcrSum += zCR[i];
			
		}
		return zcrSum;
	}

	/* SRF Spectral Roll-off */
	protected double SRF(int Frame, int times, double[] source) {
		double sum_energy = 0;
		double energy = 0;

		double[] y = new double[Frame];
		double srfSum = 0;
		int K_f = 0;
		for (int i = 0; i < Frame; i++) {
			y[i] = 0;
		}
		for (int i = 0; i < times; i++) {
			double[] sRF = new double[times];
			double[] tempS = new double[2 * Frame];
			for (int j = 0; j < Frame; j++) {
				tempS[i] = source[i * Frame + j];
			}
			// FFT fft = new FFT(Frame);
			// fft.fft(tempS, y);
			/* DFT */
			// double[] outreal = new double[Frame];
			// double[] outimag = new double[Frame];
			// DFT dft = new DFT();
			// DFT.computeDft(tempS, y, outreal, outimag);
			DoubleFFT_1D fft = new DoubleFFT_1D(Frame);
			fft.complexForward(tempS);
			for (int j = 0; j < Frame; j++) {
				// Log.i("DFTresult", "outreal:" + outreal[j] + "outimag"
				// + outimag[j]);
				sum_energy += Math.pow(tempS[2 * j], 2)
						+ Math.pow(tempS[2 * j + 1], 2);
			}
			for (int j = 0; j < Frame; j++) {
				energy += Math.pow(tempS[2 * j], 2)
						+ Math.pow(tempS[2 * j + 1], 2);
				if (energy >= 0.95 * sum_energy) {
					K_f = j;
					break;
				}
			}
			
			

			sRF[i] = K_f;
			srfSum += K_f;
			
			sum_energy = 0;
			energy = 0;
		}
		return srfSum;
	}

	// -----------------------------------------------------------------
	// private methods
	// ----------------------------------

	private AudioRecord getAudioRecord() {
		AudioRecord ar = null;
		try {
			ar = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
					SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					AudioRecord.getMinBufferSize(SAMPLE_RATE,
							AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT) * 10);
			// assertNotNull("Could not create AudioRecord", ar);
			// assertEquals("AudioRecord not initialized",
			// AudioRecord.STATE_INITIALIZED, ar.getState());
		} catch (IllegalArgumentException e) {
			// fail("AudioRecord invalid parameter");
		}
		return ar;
	}

	/* clear a folder */
	public void deleteFile(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				new File(dir, children[i]).delete();
			}
		}
	}

}