package dev.sutd.hdb;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.achartengine.GraphicalView;
import org.qap.ctimelineview.TimelineRow;
import org.qap.ctimelineview.TimelineViewAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmResults;
import models.Cluster;
import models.Data;
import models.NodeRectangle;

/**
 * Created by Sanj on 2/1/2018.
 */
public class TimeLineActivity extends Activity {

    ArrayList<Long> errorTimes;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        long startTime = b.getLong("startTime");
        long endTime = b.getLong("endTime");
        setContentView(R.layout.timeline);
        RealmResults<Data> data =  RealmController.with(MyApp.getInstance()).getDailyData(startTime, endTime);
        ArrayList<Cluster> rawClu = getRawData(startTime, endTime, data);
        ArrayList<Cluster> clu = clusterData(startTime, endTime, rawClu);
        if(clu.size()>0) {
            try {
                draw_timeline(clu);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Cluster> clusterData(long startTime, long endTime,ArrayList<Cluster> clu){
        ArrayList<Cluster> resNodes = new ArrayList<Cluster>();

        if(clu.size() > 0){
            ClusterAlgoNew ca = new ClusterAlgoNew(this);
            resNodes = ca.getClusterList(clu,endTime);
            errorTimes = getNoData(clu,startTime,endTime);
        }
        else{
            return null;
        }
        return resNodes;
    }

    private ArrayList<Cluster> getRawData(long startTime, long endTime, RealmResults<Data> dataArray){

        ArrayList<Cluster> clu = new ArrayList<Cluster>();
        for(int i =0; i<dataArray.size(); i++){

            Cluster cluster = new Cluster(dataArray.get(i).getLatitude(), dataArray.get(i).getLongitude(), dataArray.get(i).getTime(), 0, dataArray.get(i).getAccuracy());
            cluster.setActivity(dataArray.get(i).getActivity());
            clu.add(cluster);

        }

        return clu;
    }
    private ArrayList<Long> getNoData(ArrayList<Cluster> clu, long startTime, long endTime){
        Collections.sort(clu, new DateComparator());
        ArrayList<Long> errorTimes = new ArrayList<Long>();
        int cluLen = clu.size()-1;
        long temp1,next1;
        for(int i =-1; i<cluLen; i++){
            if(i ==-1){
                temp1 = startTime;
                next1 =clu.get(0).getTime();
            }
            else{
                temp1 = clu.get(i).getTime();
                if(i == cluLen-1){
                    next1=endTime;
                }
                else{

                    next1 = clu.get(i+1).getTime();
                }
            }
            long du;
            du = (next1 - temp1)/1000;

            if(du>1260){
                errorTimes.add(temp1);
                errorTimes.add(next1);

            }
        }
        return errorTimes;
    }
    private Date getReadableTime(long time){
        SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd HH:mm:ss");

        Date d = new Date(time);
        String    strTime    =    formatter.format(d);
        Log.d("time", strTime);


        Date startDate=new Date();
        try {
            startDate = formatter.parse(strTime);
            String newDateString = formatter.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    private void draw_timeline(ArrayList<Cluster> cluster) throws IOException {
        // Create Timeline rows List
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();


        for(int i =0; i<cluster.size();i++) {
            // Create new timeline row (Row Id)
                        TimelineRow myRow = new TimelineRow(0);

            // To set the row Date (optional)
                        myRow.setDate(getReadableTime(cluster.get(i).getTime()));
            // To set the row Title (optional)
                        myRow.setTitle(getAddress(cluster.get(i)));
            // To set the row Description (optional)
                        myRow.setDescription("Activity");
            // To set the row bitmap image (optional)
                        myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.location));
            // To set row Below Line Color (optional)
                        myRow.setBellowLineColor(Color.argb(255, 0, 0, 0));
            // To set row Below Line Size in dp (optional)
                        myRow.setBellowLineSize(6);
            // To set row Image Size in dp (optional)
                        myRow.setImageSize(40);
            // To set background color of the row image (optional)
                        myRow.setBackgroundColor(Color.argb(255, 255, 255, 255));
            // To set the Background Size of the row image in dp (optional)
                        myRow.setBackgroundSize(60);
            // To set row Date text color (optional)
                        myRow.setDateColor(Color.argb(255, 0, 0, 0));
            // To set row Title text color (optional)
                        myRow.setTitleColor(Color.argb(255, 0, 0, 0));
            // To set row Description text color (optional)
                        myRow.setDescriptionColor(Color.argb(255, 0, 0, 0));

            // Add the new row to the list
            timelineRowsList.add(myRow);
            //timelineRowsList.add(myRow);

        }
    if(timelineRowsList.size()>0) {
    // Create the Timeline Adapter
        ArrayAdapter<TimelineRow> myAdapter = new TimelineViewAdapter(this, 0, timelineRowsList,
                //if true, list will be sorted by date
                false);

    // Get the ListView and Bind it with the Timeline Adapter
        ListView myListView = (ListView) findViewById(R.id.timeline_listView);
        myListView.setAdapter(myAdapter);
    }
    }

    private String getAddress(Cluster clu) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(clu.getLatitude(), clu.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        return address ;
    }

}
