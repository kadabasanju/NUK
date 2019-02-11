package dev.sutd.hdb;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.widget.CalendarView;
import android.widget.ScrollView;
import android.widget.Toast;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import java.util.List;

import java.util.TimeZone;

import models.Cluster;
import models.Data;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;

    long startTime, endTime;
    ScrollView mainScroll;
    CalendarView cv;
    Intent intent2;
    static final int REQUEST_ACCESS_GEO_LOCATION = 100;
    static final int REQUEST_READ_ACCESS_STORAGE = 101;
    static final int REQUEST_WRITE_ACCESS_STORAGE = 102;
    private PendingIntent pendingIntent;
    //Alarm Request Code
    private static final int ALARM_REQUEST_CODE = 133;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean registered =  sharedpreferences.contains("DEVICE_ID");
        boolean auto_start  = sharedpreferences.contains("AUTO_START");

        if(registered) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int storageReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int storageWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_GEO_LOCATION);
            } else if (storageReadPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_ACCESS_STORAGE);
                }
                else if(storageWritePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_ACCESS_STORAGE);

                }
                else{

                //TODO
            }
            Log.e("Device ID", sharedpreferences.getString("DEVICE_ID", "DEVICE_ID"));
            // Cases when auto start is blocked by a few phones.
            String str = android.os.Build.MANUFACTURER;

            Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

            triggerAlarmManager();


            setContentView(R.layout.activity_main);
            mainScroll=(ScrollView)findViewById(R.id.scrollView);
            cv=(CalendarView)findViewById(R.id.calModView);
            mainScroll.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    //Log.v(TAG,”PARENT TOUCH”);
                    findViewById(R.id.calModView).getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
            });
            cv.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    //Log.v(TAG,”CHILD TOUCH”);
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            initialiseCalendar();
            FirebaseMessaging.getInstance().subscribeToTopic("all");
            String firebaseToken = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("FIREBASE_ID", firebaseToken);
            editor.commit();
            System.out.println("FIREBASE ID MAIN ACTIVITY : "+firebaseToken + "   : : :");

            if(sharedpreferences.contains("DATA_CONSENT")) {
                boolean dataConsent = sharedpreferences.getBoolean("DATA_CONSENT",true);
                intent2 = new Intent(getBaseContext(), MainService.class);
                if (dataConsent) {
                    MyUtils.startPowerSaverIntent(this);
                    if (!isMyServiceRunning(MainService.class)) {
                        //startForegroundService(intent2);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent2);
                        } else {
                            startService(intent2);
                        }
                    }
                }
                else {
                    if (isMyServiceRunning(MainService.class)) {
                        stopService(intent2);
                    }
                }
            }
            
        }
        else {
            // user is not registered in show registration screen
            Intent register = new Intent(getApplicationContext(), WebRegistrationActivity.class);
            register.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(register);
            finish();
        }

    }

    //Trigger alarm manager with entered time interval
    public void triggerAlarmManager() {


        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10800000, pendingIntent);//set alarm manager with entered timer by converting into milliseconds
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

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    public void frontend(View v){
        Intent frontendIntent = new Intent(getApplicationContext(), FrontEndAdvancedActivity.class);
        frontendIntent.putExtra("PAGENAME","LOGIN");
        frontendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(frontendIntent);
    }

    public void initialiseCalendar(){
        //CalendarView cv = (CalendarView) findViewById(R.id.calModView);
        cv.setShowWeekNumber(false);
        cv.setFirstDayOfWeek(2);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        calendar1.setTime(new Date());

        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);

        calendar1.add(Calendar.DAY_OF_YEAR, 0);
        Date newDate = calendar1.getTime();
        startTime = newDate.getTime();
        endTime = startTime + (24*3600*1000);
        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {


                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
                cal.setTime(new Date());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                String dateInString = day + "-" + (month + 1) + "-" + year + " 00:00:00";

                Date date = new Date();

                try {
                    date = sdf.parse(dateInString);


                    cal.setTime(date);

                    startTime = cal.getTimeInMillis();

                    endTime = startTime + (24 * 60 * 60 * 1000);



                    //dayInString = "on " + day + "/" + (month + 1) + "/" + year;
                    //Toast.makeText(context.getApplicationContext(), day + "/" + (month+1) + "/" + year, Toast.LENGTH_SHORT).show();

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        });


    }

    public void clusterData(View v) throws IOException {
        ArrayList<Data> dataArray = RealmController.with(MyApp.getInstance()).getDailyData(startTime, endTime);
        ArrayList<Cluster> clu = new ArrayList<Cluster>();
       for(int i =0; i<dataArray.size(); i++){

                Cluster cluster = new Cluster(dataArray.get(i).getLatitude(), dataArray.get(i).getLongitude(), dataArray.get(i).getTime(), 0, dataArray.get(i).getAccuracy());
                cluster.setActivity("");
                clu.add(cluster);



        }


        if(clu.size() !=0){

            getClusters(clu);
        }
        else{
            Toast.makeText(this, "No Location Updates", Toast.LENGTH_LONG).show();
        }
    }

    public void dailyView(View v){
        Intent intent = new Intent(this, GraphViewActivity.class);
        intent.putExtra("startTime",startTime);
        intent.putExtra("endTime", endTime);
        startActivity(intent);
    }

    public void timelineView(View v){
        Intent intent = new Intent(this, TimeLineActivity.class);
        intent.putExtra("startTime",startTime);
        intent.putExtra("endTime", endTime);
        startActivity(intent);
    }

    public void getClusters(ArrayList<Cluster> clu) throws IOException {
        ArrayList<Cluster> resultNodes = new ArrayList<Cluster>();
        Collections.sort(clu, new DateComparator());
        final ArrayList<Cluster> raw = clu;
        ClusterAlgoNew ca = new ClusterAlgoNew(this);
        resultNodes = ca.getClusterList(clu, endTime);

        // To draw path
        ArrayList<ArrayList<Cluster>> pathList = new ArrayList<ArrayList<Cluster>>();
        for(int j =0; j<resultNodes.size()-1; j++){
            ArrayList<Cluster> path = new ArrayList<Cluster>();
            path.add(resultNodes.get(j));
            long time1 = resultNodes.get(j).getTime() + (long)resultNodes.get(j).getDuration()*1000;
            long time2 = resultNodes.get(j+1).getTime();

            for(int i =0; i<raw.size(); i++)
            {
                long t = raw.get(i).getTime();
                if(t >= time1  && t < time2 )
                {
                    if(raw.get(i).getAccuracy()<100)
                    {

                        path.add(raw.get(i));}
                }
                else if (t>=time2){
                    break;
                }
            }
            path.add(resultNodes.get(j+1));
            pathList.add(path);
        }
        circleMarker(resultNodes, pathList);
    }




    private void circleMarker(ArrayList<Cluster> mpArray, ArrayList<ArrayList<Cluster>> path) throws IOException{
        WriteToFile wtf  = new WriteToFile();
        String pos="";
        StringBuilder pathCord = new StringBuilder("");
        for(int i = 0; i < mpArray.size();i++){
            String info = "citymap['Node"+ (i+1)+"'] = {\n"+
                    "center:new google.maps.LatLng("+mpArray.get(i).getLatitude() +", "+ mpArray.get(i).getLongitude()+"),\n"+
                    "population: 400};\n";//+ mpArray.get(i).getDuration() +"};\n";
            pos = pos.concat(info);

        }
        pathCord.append("\n var polylineCoordinates1"+" = [\n");
        for(int j = 0; j < path.size();j++){
            ArrayList<Cluster> pathPts = path.get(j);

            for(int i =0; i<pathPts.size(); i++){
                String cord = "new google.maps.LatLng("+ pathPts.get(i).getLatitude() + "," + pathPts.get(i).getLongitude()+"),\n";


                pathCord.append(cord);

            }

        }

        String polyString = "];\n"+
                "var lineSymbol = {\n"+
                "path: google.maps.SymbolPath.CIRCLE,\n"+
                "scale: 8,\n"+
                "strokeColor: '#393'\n"+
                "};\n"+

                " line = new google.maps.Polyline({\n"+
                "path: polylineCoordinates1"+",\n"+
                " icons: [{\n"+
                "    icon: lineSymbol,\n"+
                "     offset: '100%'\n"+
                "  }],\n"+
                "strokeColor: '#000000',\n"+
                "strokeOpacity: 1.0,\n"+
                "strokeWeight: 2,\n"+
                "map:map,\n"+
                //"editable: true\n"+
                "});\n"+
                "animateCircle();}\n"+
                "function animateCircle() {\n"+
                " var count = 0;\n"+
                "window.setInterval(function() {\n"+
                " count = (count + 1) % 200;\n"+

                "var icons = line.get('icons');\n"+
                "icons[0].offset = (count / 2) + '%';\n"+
                "line.set('icons', icons);\n"+
                "}, 60);\n"+
                "\n";

        pathCord.append(polyString);



        wtf.deleteMapFile();
        wtf.buildRound(pos, pathCord);
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);


    }

    public void loadVisitData(View v){
        Intent intent = new Intent(this, VisitsActivity.class);
        intent.putExtra("startTime",startTime);
        intent.putExtra("endTime", endTime);
        String device_id = sharedpreferences.getString("DEVICE_ID", "");
        intent.putExtra("device_id", device_id);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_GEO_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;
            case REQUEST_READ_ACCESS_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;
            case REQUEST_WRITE_ACCESS_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

}
