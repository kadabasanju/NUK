package dev.sutd.hdb;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import models.ServiceInfo;


// This class is important to start the service automatically after rebooting the system.
public class BootUpReceiver extends BroadcastReceiver {
	private Realm realm;

	@Override
	public void onReceive(Context context, Intent intent) {
		realm = Realm.getDefaultInstance(); // initialize realm
		// TODO Auto-generated method stub

		saveServiceLog(System.currentTimeMillis());


		Intent i = new Intent(MyApp.getContext(), MainService.class);


		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
		boolean dataConsent = true;
		if (sharedPreferences.contains("DATA_CONSENT")) {
			dataConsent = sharedPreferences.getBoolean("DATA_CONSENT", true);
		}
		if (!dataConsent) {
			MyApp.getContext().stopService(i);
		}

		if (dataConsent && !isMyServiceRunning(MainService.class)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				MyApp.getContext().startForegroundService(i);
			} else {
				MyApp.getContext().startService(i);
			}



		}

	}










	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) MyApp.getContext().getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				Log.i ("isMyServiceRunning?", true+"");
				return true;
			}
		}
		Log.i ("isMyServiceRunning?", false+"");
		return false;
	}



	// save to service log
	private void saveServiceLog( final long timestamp){
		if(realm.isInTransaction()){
			realm.commitTransaction();
		}
		realm.beginTransaction();
		ServiceInfo si = realm.createObject(ServiceInfo.class);
		si.setTimestamp(timestamp);
		realm.commitTransaction();

	}
	

}
