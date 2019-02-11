package dev.sutd.hdb;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.conn.util.InetAddressUtils;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import models.Data;
import models.GeoActivity;
import models.NipunActivity;
import models.ScanInformation;
import models.ScanObject;
import models.Sound;



public class MainService extends Service implements SensorEventListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    // intialization sensors
    private SensorManager mSensorManager;

    private Sensor LightSensor;
    //Google play Services activity Recognition
    String prev_gact ="";
    long last_location_time = 0;
    Location last_loc= null;
    long last_wifi_time =0;
    int lastShownNotificationId=1;

    static Context context;

    //Location
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static long UPDATE_INTERVAL_IN_MILLISECONDS = 300000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    Realm realm;
    double lightSensorValue;
    PendingIntent pi2;
    static BroadcastReceiver br;
    WifiManager wifiManager;
    static JobScheduler mJobScheduler;
    static final int REQUEST_ACCESS_GEO_LOCATION=100;


    private BroadcastReceiver googleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String activity = intent.getStringExtra("activity");
            double confidence = intent.getDoubleExtra("confidence",0);
            updateActivityStatus(activity,confidence);
            //Log.d("googlereceiver", activity+": " + confidence);
        }
    };

    @Override
    public void onCreate() {
        Log.i("MAin Service", "onCreate");
        //startForeground(1,new Notification());
        //showNotification();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        LightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        setupWiFi();
        buildGoogleApiClient();
        //setSoundAlarm();
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ACTION_LOCATION_BROADCAST));
        context = this;
        LocalBroadcastManager.getInstance(this).registerReceiver(googleReceiver, new IntentFilter("ACTIVITY_RECOGNITION_GOOGLE"));
        //Initailizing realm
        realm = RealmController.with(MyApp.getInstance()).getRealm();
        //startUploadService();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        //}

        mSensorManager.registerListener(this, LightSensor, 300000);

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            //Log.i(LOG_TAG, "Received Start Foreground Intent ");
            showNotification();
        //createAndShowForegroundNotification(this, 1);
        Log.d("Main Service", "Main service");
            Log.i("Main Service", "onStartCommand");
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            LightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            setSoundAlarm();

            LocalBroadcastManager.getInstance(this).registerReceiver(googleReceiver, new IntentFilter("ACTIVITY_RECOGNITION_GOOGLE"));
            //Initailizing realm
            realm = RealmController.with(MyApp.getInstance()).getRealm();
            startUploadProcess();
            //startUploadService();

            mSensorManager.registerListener(this, LightSensor, 300000);

            if (!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();

        return Service.START_STICKY;
    }


    public void processSound(){
        //update the local db
        if(realm.isInTransaction()){
            realm.commitTransaction();
        }
        realm.beginTransaction();
        Sound s = realm.createObject(Sound.class);
        s.setTime(System.currentTimeMillis());
        s.setBattery(getBatteryStatus());
        s.setIp_address(getIP());
        s.setNetwork_id(getNetworkDetails());
        s.setLight(lightSensorValue);
        s.setDecibel(0);
        s.setSocio_activity("");
        realm.commitTransaction();

        // Check if the last stored activity is not still and no activity update for last 5 mins, then add still into db and then change the sampling rate for location updates to 5 mins!
        GeoActivity lastAct = RealmController.with(MyApp.getInstance()).getLastDailyGeoData(System.currentTimeMillis());
        long timeDiff = System.currentTimeMillis()-lastAct.getTime();
        if(timeDiff>300000 && !(lastAct.getgAct().split(":")[0].equals("Still"))){
            if(realm.isInTransaction()){
                realm.commitTransaction();
            }
            realm.beginTransaction();
            GeoActivity g = realm.createObject(GeoActivity.class);
            g.setgAct("Still:100");
            g.setTime(System.currentTimeMillis());

            //Log.i("activity", getDateString(System.currentTimeMillis()));
            realm.commitTransaction();
            stopLocationUpdate();
            //5min interval
            UPDATE_INTERVAL_IN_MILLISECONDS=300000;
            startLocationUpdate();

        }

        getWifiAP();

    }
    public static void startUploadProcess(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(setUploadJobScheculer()!=null){
                mJobScheduler = (JobScheduler)
                        MyApp.getContext().getSystemService( Context.JOB_SCHEDULER_SERVICE );
                mJobScheduler.schedule(setUploadJobScheculer().build());
                Log.e("uploadJob", "started upload process");

            }
        }
        else{
            if(!isMyServiceRunning(UploadService.class)){
                startUploadService();
            }
        }
    }
    private static JobInfo.Builder setUploadJobScheculer(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(1,
                    new ComponentName(MyApp.getContext().getPackageName(),
                            UploadJobService.class.getName()));
            // Saying that it is a periodic task, that should be called every 6 hrs once,
            // if the phone is connected to wifi and needs to start when rebooted
            // If there is no wifi for 2 full day, then call the service anyhow.
            builder.setMinimumLatency(21600000)
                    .setOverrideDeadline(86400000)
                    //.setPeriodic(900000)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                   ;
            //mJobScheduler.schedule(builder.build());
            return builder;
        }
        return null;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivityFrontend.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);



        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_stat_name);

        Notification.Builder notification = new Notification.Builder(this)
                .setContentTitle("DSBJ")
                .setTicker("DSBJ")
                .setContentText("Click to view Events near you")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification.getNotification());

    }
    private void createAndShowForegroundNotification(Service yourService, int notificationId) {
        Intent notificationIntent = new Intent(this, MainActivityFrontend.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService,
                "com.example.your_app.notification.CHANNEL_ID_FOREGROUND", // Channel id
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top
        builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("DSBJ")
                .setContentText("Click to view Events near you")
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();

        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager nm = (NotificationManager) yourService.getSystemService(Activity.NOTIFICATION_SERVICE);
            nm.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = notificationId;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = "Click to see events near you.";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if (nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }

    }

    public static void startUploadService(){
        Intent intent = new Intent(context, UploadService.class);

        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //10800000
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 3600000, pintent);
        context.startService(new Intent(context, UploadService.class));

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            lightSensorValue = event.values[0];
        }
    }

    public void setSoundAlarm(){
         br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                try{

                    processSound();


                }catch(Exception e){}
            }
        };
        registerReceiver(br, new IntentFilter("dev.sutd.hdb.soundRecog"));
       pi2 = PendingIntent.getBroadcast( this, 0, new Intent("dev.sutd.hdb.soundRecog"),
                0 );
        AlarmManager am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 300000, pi2);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {

        try{
        pi2.cancel();

        unregisterReceiver(br);
        stopLocationUpdate();
        //Intent broadcastIntent = new Intent("com.dev.sutd.hdb.RestartService");
        //sendBroadcast(broadcastIntent);
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
        super.onDestroy();

    }






    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_GEO_LOCATION);
        } else {

            if (mGoogleApiClient.isConnected()) {
                Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (l != null) {

                }

                startLocationUpdate();
                Intent intent = new Intent(this, ActivityRecognizedService.class);
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 3000, pendingIntent);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onLocationChanged(Location location) {
        double dist =0;
        int flag = 0;
        if(last_loc == null || last_location_time ==0){
            flag =1;
        }

        else {
            dist = location.distanceTo(last_loc);
            if(location.getTime()>last_location_time || dist>0){
                flag =1;
            }
        }

        if(flag ==1){
            if(realm.isInTransaction()){
                realm.commitTransaction();
            }
            realm.beginTransaction();
            Data d = realm.createObject(Data.class);
            d.setTime(location.getTime());
            d.setLatitude(location.getLatitude());
            d.setLongitude(location.getLongitude());
            d.setAccuracy(location.getAccuracy());
            d.setAltitude(location.getAltitude());
            d.setSpeed(location.getSpeed());
            d.setWifiAPs("");//getWifiAP());
            realm.commitTransaction();
            last_loc = location;
            last_location_time = location.getTime();
           // Log.i("added_loc", location.getTime()+"");
        }




    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("MainService", "onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS/2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        if(mGoogleApiClient.isConnected()) {
            initLocationRequest();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdate() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();



    }




    public void updateActivityStatus(String acty, double confi){

        if(!acty.equals(prev_gact)&& confi>70 ) {
            if(realm.isInTransaction()){
                realm.commitTransaction();
            }
            realm.beginTransaction();
            GeoActivity g = realm.createObject(GeoActivity.class);
            g.setTime(System.currentTimeMillis());
            g.setgAct(acty + ":" + confi);
            realm.commitTransaction();
            prev_gact = acty;

            if (acty.equals("Still")) {
                UPDATE_INTERVAL_IN_MILLISECONDS = 300000;
            } else if (acty.equals("Bicycle") || acty.equals("Foot") || acty.equals("Running")) {
                UPDATE_INTERVAL_IN_MILLISECONDS = 120000;
            } else if (acty.equals("Vehicle")) {
                UPDATE_INTERVAL_IN_MILLISECONDS = 60000;


            }
            //This is for adaptive sampling of GPS
            if (mGoogleApiClient.isConnected()){
                stopLocationUpdate();
                startLocationUpdate();
            }
        }


    }

    private static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MyApp.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private int getBatteryStatus(){

        Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra("level", -1);
        double scale = batteryIntent.getIntExtra("scale", -1);
        double level = -1;
        int battery =-1;
        if (rawlevel >= 0 && scale > 0) {
            level = (rawlevel*100) / scale;
            battery = (int)level;
        }

        return battery;
    }

    private String getNetworkDetails(){
        Context context = this;
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager;
        String nw="Unknown";
        try{
            //mobile
            NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();

            //wifi
            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();

            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                nw = "mobile";
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                nw = ssid;
                nw =  nw.replace("\"","");

            }
        }catch(Exception e){
            nw = "Unknown";
        }

        return nw;
    }


    private String getIP(){
        String ipaddress ="Unknown";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()) ) {
                        ipaddress = inetAddress.getHostAddress();

                    }
                }
            }
        } catch (SocketException ex) {

        }
        return ipaddress;
    }

    private void setupWiFi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
             wifiManager.setWifiEnabled(true);
        }
    }

    public void getWifiAP(){
        List<ScanResult> results;
        wifiManager.startScan();
        results = wifiManager.getScanResults();
        RealmList<ScanObject> scanObjectList = new RealmList<>();

        //System.out.println("Scan result list size: "+results.size());
        for(int i=0;i<results.size();i++){
            String mac = results.get(i).BSSID;
            int rssi = results.get(i).level;
            ScanObject scanObject = new ScanObject();
            scanObject.setBSSID(mac);
            scanObject.setRSSI(rssi);
            scanObjectList.add(scanObject);
        }
        if((last_wifi_time==0 || last_wifi_time<System.currentTimeMillis())&& last_wifi_time!=System.currentTimeMillis()) {
            Log.e("Main Service", "added ScanInfo");
            last_wifi_time = System.currentTimeMillis();
            final RealmList<ScanObject> sol = scanObjectList;
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    ScanInformation scanInfoToSave = bgRealm.createObject(ScanInformation.class);
                    scanInfoToSave.setTimestamp(System.currentTimeMillis());
                    scanInfoToSave.setScanObjectList(sol);
                    //Log.e("Main Service", "added ScanInfo");
                    //System.out.println("LIST SIZE: "+sol.size());
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    // Transaction was a success.
                    // Toast.makeText(getApplicationContext(), "Saved to database", Toast.LENGTH_SHORT).show();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    // Transaction failed and was automatically canceled.
                    Toast.makeText(getApplicationContext(), "Error in saving!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




}
