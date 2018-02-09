package dev.sutd.hdb;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Element;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import models.DataType;


public class SoundProcessing implements SensorEventListener{


	private boolean mIsRecording = false;
	private Context context;
	public static double aveDB;
	SensorManager sensorManager;
	Sensor light;
	double value;

	Timer timer = new Timer();

	SoundProcessing(Context cont){
		context = cont;
		startSensor();
	}
	
	public void startSensor(){
		 sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		    light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		    sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopService(){
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		value = event.values[0];
		stopService();
	}
	public void deduceAudio(){
		final AudioRecording audioRecording = new AudioRecording(context);
		if (!mIsRecording) {/* Handler */

			/* Handler */
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 1:

						String waveFile = audioRecording.stopRecording();

						/* audio processing */
						AudioProcessing audioProcess = new AudioProcessing(waveFile);// mRecording.getName());
						// aveDB = audioProcess.soundDB();

						// DataType recent = new DataType();
						// recent.getData(audioProcess.startProcessing());
						try{
						DataType recent = audioProcess.startProcessing();

						long time = System.currentTimeMillis();
						String tempDataInfo = time + "\n\nMusic:"
								+ recent.getMusic() + "\n\nSpeak:"
								+ recent.getSpeak() + "\n\nSoundDb:"
								+ valuePlace(recent.getAveragedb());
						//Log.i("sound", tempDataInfo);
						String activity="";
						if(recent.getSpeak().equals("true")){
							activity ="speaking";
							//if(recent.getSpeak().equals("true")){
							//activity = "music & speaking";
							//}

						}
						else{
							if(recent.getMusic().equals("true")){
								activity = "Music";
							}
							else{
								activity = "quite";
							}
						}
						String decibel = valuePlace(recent.getAveragedb());
							MainService.decibel = Double.valueOf(decibel);
							MainService.socioActivity=activity;
						}catch(Exception e){
							long time = System.currentTimeMillis();
							String activity = "";
							String decibel = "0";
							MainService.decibel = Double.valueOf(decibel);
							MainService.socioActivity=activity;
						}
					}
				}
			};

			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
					System.gc();
				}
			};

			// mRecorder.startRecording();
			audioRecording.startRecording();
			// startBufferedWrite(mRecording);
			timer.schedule(task, 10000);


		}
	}
	public String valuePlace(String valueBS) {
		Double value = Double.parseDouble(valueBS);
		DecimalFormat df = new DecimalFormat("00.00");
		return df.format(value);
	}


	
	}





