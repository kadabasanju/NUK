package dev.sutd.hdb;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ScrollView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivityFrontend extends AppCompatActivity {
    Intent intent2;
   // private PendingIntent pendingIntent;
    SharedPreferences sharedpreferences;
    //private static final int ALARM_REQUEST_CODE = 133;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean registered =  sharedpreferences.contains("DEVICE_ID");



        if(registered) {


            Log.e("Device ID", sharedpreferences.getString("DEVICE_ID", "DEVICE_ID"));


            FirebaseMessaging.getInstance().subscribeToTopic("all");
            String firebaseToken = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("FIREBASE_ID", firebaseToken);
            editor.commit();
            //System.out.println("FIREBASE ID MAIN ACTIVITY : "+firebaseToken + "   : : :");

            if(sharedpreferences.contains("DATA_CONSENT")) {
                boolean dataConsent = sharedpreferences.getBoolean("DATA_CONSENT",true);
                intent2 = new Intent(getBaseContext(), MainService.class);
                if (dataConsent) {
                    //MyUtils.startPowerSaverIntent(MainActivityFrontend.this);
                    if (!isMyServiceRunning(MainService.class)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent2);
                        } else {
                            startService(intent2);
                        }
                        //startService(intent2);
                    }
                }
                else {
                    if (isMyServiceRunning(MainService.class)) {
                        stopService(intent2);
                    }
                }
            }
            Intent frontend = new Intent(getApplicationContext(), FrontEndAdvancedActivity.class);
            frontend.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            frontend.putExtra("PAGENAME","LOGIN");

            startActivity(frontend);
            finish();

        }
        else {
            // user is not registered in show registration screen
            Intent register = new Intent(getApplicationContext(), WebRegistrationActivity.class);
            register.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(register);
            finish();
        }

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;

    }

}
