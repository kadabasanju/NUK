package dev.sutd.hdb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Environment;
import android.text.format.Time;
import android.widget.ProgressBar;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class AudioRecording {
	public static final int SAMPLE_RATE = 44100;

	private boolean isRecord = false;
	private AudioRecord mRecorder;
	private NoiseSuppressor ns;
	private File mRecording;
	private short[] mBuffer;

	private int bufferSize = 0;
	private ProgressBar mProgressBar;
	private final Context context;
	private static final String SoundPath = Environment
			.getExternalStorageDirectory() + "/SoundDetect/";

	public AudioRecording(Context context) {
		this.context = context;

	}

	/* start recording */

	public void startRecording() {
		initRecordr();
		if (mRecorder == null) {
			initRecordr();
		}
		if (mRecorder != null) {
			mRecorder.startRecording();
			// if (NoiseSuppressor.isAvailable()) {
			// Log.e("yougesswhat",
			// " Noise Suppressor is" + NoiseSuppressor.isAvailable());
			// ns = NoiseSuppressor.create(mRecorder.getAudioSessionId());
			//
			// try {
			// ns.setEnabled(true);
			// } catch (IllegalStateException e) {
			// Log.e("sutd", "noise suppressor cannot enabled!", e);
			// }
			// }

			isRecord = true;
			mRecording = getFile("raw");
			startBufferedWrite(mRecording);

		}
	}

	/* stop recording */
	@SuppressLint("NewApi")
	public String stopRecording() {
		File waveFile = getFile("wav");
		if (mRecorder != null) {

			mRecorder.stop();
			try {

				rawToWave(mRecording, waveFile);
				
			} catch (Exception e) {
				
			}
			isRecord = false;
			mRecorder.stop();
			mRecorder.release();
			// ns.release();
			mRecorder = null;
		}

		return waveFile.getName();
	}

	private void initRecordr() {
		// TODO Auto-generated method stub
		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new short[bufferSize];
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize);
	}

	private void startBufferedWrite(final File file) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(
							new FileOutputStream(file)));
					while (isRecord) {
						double sum = 0;
						int readSize = mRecorder.read(mBuffer, 0,
								mBuffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(mBuffer[i]);
							sum += mBuffer[i] * mBuffer[i];
						}
						if (readSize > 0) {
							final double amplitude = sum / readSize;

						}
					}
				} catch (Exception e) {
					
				} finally {

					if (output != null) {
						try {
							output.flush();
						} catch (Exception e) {
							// TODO: handle exception
							
						} finally {
							try {
								output.close();
							} catch (Exception e) {
								// TODO: handle exception
								
							}
						}
					}
				}
			}
		}).start();
	}

	private File getFile(final String suffix) {
		// TODO Auto-generated method stub
		Time time = new Time();
		time.setToNow();
		File file = new File(SoundPath);
		if (!file.exists()) {
			file.mkdir();
		}
		return new File(SoundPath, time.format("%Y%m%d%H%M%S") + "." + suffix);
		// return new File(SoundPath, midleRAW + "." + suffix);
	}

	private void rawToWave(final File rawFile, final File waveFile)
			throws IOException {

		byte[] rawData = new byte[(int) rawFile.length()];
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(rawFile));
			input.read(rawData);
		} finally {
			if (input != null) {
				input.close();
			}
		}

		DataOutputStream output = null;
		try {
			output = new DataOutputStream(new FileOutputStream(waveFile));
			// WAVE header
			// see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
			writeString(output, "RIFF"); // chunk id
			writeInt(output, 36 + rawData.length); // chunk size
			writeString(output, "WAVE"); // format
			writeString(output, "fmt "); // subchunk 1 id
			writeInt(output, 16); // subchunk 1 size
			writeShort(output, (short) 1); // audio format (1 = PCM)
			writeShort(output, (short) 1); // number of channels
			writeInt(output, SAMPLE_RATE); // sample rate
			writeInt(output, SAMPLE_RATE * 2); // byte rate
			writeShort(output, (short) 2); // block align
			writeShort(output, (short) 16); // bits per sample
			writeString(output, "data"); // subchunk 2 id
			writeInt(output, rawData.length); // subchunk 2 size
			// Audio data (conversion big endian -> little endian)
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(shorts);
			ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				bytes.putShort(s);
			}
			output.write(bytes.array());
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	private void writeInt(final DataOutputStream output, final int value)
			throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
		output.write(value >> 16);
		output.write(value >> 24);
	}

	private void writeShort(final DataOutputStream output, final short value)
			throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
	}

	private void writeString(final DataOutputStream output, final String value)
			throws IOException {
		for (int i = 0; i < value.length(); i++) {
			output.write(value.charAt(i));
		}
	}

}
