package dev.sutd.hdb;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanj on 4/3/2018.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Intent i = new Intent(MyApp.getContext(), MainService.class);
        Map<String, String> data = message.getData();
        List<String> keys = new ArrayList<String>(data.keySet());

        for (String key: keys) {
            System.out.println(key + ": " + data.get(key));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dataConsent = true;
        if (sharedPreferences.contains("DATA_CONSENT")) {
            dataConsent = sharedPreferences.getBoolean("DATA_CONSENT", true);
        }
        if (isMyServiceRunning(MainService.class)) {
            MyApp.getContext().stopService(i);
        }
        // If this is from the server that sends a message to restart the service and the dataconsent is given, then start it.
        if (data.containsKey("body") && dataConsent) {


            //
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                MyApp.getContext().startForegroundService(i);
            } else {
                MyApp.getContext().startService(i);
            }

            Log.i("fcm notif", "MainService");

        }
        // If the user selects their prefence from profile edit page, then stop service using this.
        if (data.containsKey("acceptance")) {
            boolean accept = true;
            if (data.get("acceptance").equals( String.valueOf(0))) {
                accept = false;
                //System.out.println("accept -----  false");
            }
            Log.i("fcm notif",data.get("acceptance"));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DATA_CONSENT", accept);
            editor.commit();
           if(accept) {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   MyApp.getContext().startForegroundService(i);
               } else {
                   MyApp.getContext().startService(i);
               }
           }

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



    private void sendMyNotification(String message) {
        Intent i = new Intent(MyApp.getContext(), MainService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        MyApp.getContext().startService(i);

        Log.i("Starting service", "MainService");
        //On click of notification it redirect to this Activity
        /*Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Firebase Push notification")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());*/
    }
}
