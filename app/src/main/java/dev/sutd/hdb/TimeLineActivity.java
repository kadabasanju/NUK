package dev.sutd.hdb;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.achartengine.GraphicalView;
import org.qap.ctimelineview.TimelineRow;
import org.qap.ctimelineview.TimelineViewAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.realm.RealmResults;
import models.Cluster;
import models.Data;
import models.GeoActivity;
import models.NipunActivity;
import models.Node;
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
        TextView tv = (TextView)findViewById(R.id.date_text);
        tv.setText(getDateString(startTime));
        try {
            ArrayList<Data> data = RealmController.with(MyApp.getInstance()).getDailyData(startTime, endTime);
            ArrayList<Cluster> rawClu = getRawData(data);
                //Log.i("Raw Cluster",""+rawClu.size());
                ArrayList<Cluster> clu = clusterData(startTime, endTime, rawClu);
                //Log.i("Cluster",""+clu.size());
                //ArrayList<Node> nodes = getActivityCluster(clu );

                    if (clu.size() > 0) {
                        try {
                            draw_timeline(clu);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


        }catch (NullPointerException npe){

        }
    }

    private ArrayList<Cluster> clusterData(long startTime, long endTime,ArrayList<Cluster> clu){
        ArrayList<Cluster> resNodes = new ArrayList<Cluster>();
        ArrayList<Cluster> res = new ArrayList<>();
        if(clu.size() > 0){
            ClusterAlgoNew ca = new ClusterAlgoNew(this);
            resNodes = ca.getClusterList(clu,endTime);
            //Log.i("Clustered Data",resNodes.size()+"");
            res = getActivityCluster(resNodes, endTime, startTime);
            //errorTimes = getNoData(clu,startTime,endTime);
        }
        else{
            return null;
        }
        return res;
    }

    private ArrayList<Cluster> getActivityCluster(ArrayList<Cluster> clu, long tlEndTime, long startTime){
        ArrayList<Cluster> resClu = new ArrayList<>();
        for (int i=0; i<clu.size();i++){
            // This is for each cluster
            double endTime = clu.get(i).getTime()+ (clu.get(i).getDuration()*1000);

            // Getting the first activity for the cluster.
            GeoActivity firstAct = RealmController.getInstance().getLastDailyGeoData(clu.get(i).getTime());

            // Getting all other activities for the cluster
            List<GeoActivity> rgeoAct = RealmController.getInstance().getDailyGeoData(clu.get(i).getTime(),(long)endTime);
            Map<String,Long> map = new HashMap<String, Long>();
            Map<String,Long> gmap = new HashMap<String, Long>();
            // iterate till j-1 as we need the duration for the samples.
            List<GeoActivity> geoAct = new ArrayList<GeoActivity>();
            if(! (firstAct==null)){
                if(firstAct.getgAct()!=null&&firstAct.getTime()!=0) {
                    //Log.i("adding first activity "+i,firstAct.getnAct()+"   "+firstAct.getgAct());
                    geoAct.add(firstAct);
                    Log.i("clu:" + i + "act:" + firstAct.getgAct(), "time:" + getReadableTime(firstAct.getTime()));
                }
            }
            geoAct.addAll(rgeoAct);
            //System.out.println(geoAct.size()+"   "+i);
            long nnodeduration =0;
            for(int j=0;j<geoAct.size()-1;j++) {


                String gAct = geoAct.get(j).getgAct().split(":")[0];
                if (!gAct.equals("")) {
                    // if google activity exists, then update the duration
                    long stTime = geoAct.get(j).getTime();
                    if (j == 0) {
                        stTime = clu.get(i).getTime();
                    }
                    long eTime = geoAct.get(j + 1).getTime();
                    if (j == geoAct.size() - 2) {
                        eTime = (long) endTime;
                    }
                    if (i == clu.size() - 1 && j == geoAct.size() - 2) {
                        if (checkIfToday(startTime)) {
                            eTime = System.currentTimeMillis();
                        } else {
                            eTime = tlEndTime;
                        }
                    }
                    long duration = eTime - stTime;

                    if (gmap.containsKey(gAct)) {
                        Long dur = gmap.get(gAct);
                        dur += duration;
                        gmap.put(gAct, dur);
                    } else {
                        gmap.put(gAct, duration);
                    }
                    // If gact is vehicle see what nipun activity has suggested!
                    /*if (gAct.equals("Vehicle")) {
                        List<NipunActivity> nipunAct = RealmController.getInstance().getDailyNipunData(stTime, eTime);
                        if (nipunAct.size() > 0) {
                            nnodeduration += duration;
                            for (int h = 0; h < nipunAct.size() - 1; h++) {
                                if (nipunAct.get(h).getnAct() != "") {
                                    // if nipun activity exists, then update the duration
                                    String nAct = nipunAct.get(h).getnAct().split(":")[0];
                                    long nsTime = nipunAct.get(h).getTime();
                                    if (h == 0) {
                                        nsTime = stTime;
                                    }
                                    long neTime = nipunAct.get(h + 1).getTime();
                                    if (h == nipunAct.size() - 2) {
                                        neTime = eTime;
                                    }


                                    long nduration = neTime - nsTime;

                                    if (map.containsKey(nAct)) {
                                        Long dur = map.get(nAct);
                                        dur += nduration;
                                        map.put(nAct, dur);
                                    } else {
                                        map.put(nAct, nduration);
                                    }
                                }
                            }


                        }


                    }*/

                }
            }
                //CAlculate the percentage for each activity
                String actString = "Nact: ";
                String gactStr = "Gact: ";
                Object[] keys = map.keySet().toArray();
                long totDuration = (long) clu.get(i).getDuration() * 1000;
                if (i == clu.size() - 1) {
                    if (checkIfToday(startTime)) {
                        Log.d("it is today", "yes: " + startTime);
                        totDuration = System.currentTimeMillis() - clu.get(i).getTime();
                    } else {
                        Log.d("it is today", "no: " + tlEndTime);
                        totDuration = tlEndTime - clu.get(i).getTime();
                    }
                }
                for (int k = 0; k < map.size(); k++) {

                    long actDur = map.get(keys[k]);
                    String activity = keys[k].toString();
                    if (actDur > 0 && !activity.equals("")) {
                        //int percentage = (int) Math.round((actDur/totDuration)*100);

                        //Log.d("total_duration", totDuration+"");
                        long percentage = (long) ((float) actDur * 100 / nnodeduration);
                        //Log.d(keys[k].toString(), actDur + "");
                        if (percentage > 0) {
                            actString = actString + activity + " : " + percentage + "  ";
                        }

                    }
                }
                Object[] gkeys = gmap.keySet().toArray();
                for (int k = 0; k < gmap.size(); k++) {

                    long actDur = gmap.get(gkeys[k]);
                    String activity = gkeys[k].toString();
                    if (actDur > 0 && !activity.equals("")) {
                        //int percentage = (int) Math.round((actDur/totDuration)*100);

                        //Log.d("total_duration", totDuration+"");
                        long percentage = (long) ((float) actDur * 100 / totDuration);
                        //Log.d(keys[k].toString(), actDur + "");
                        if (percentage > 0) {
                            gactStr = gactStr + activity + " : " + percentage + "  ";
                        }

                    }
                }
                if (geoAct.size() == 1) {
                    //actString = geoAct.get(0).getnAct().split(":")[0] + " : 100 ";
                    gactStr = geoAct.get(0).getgAct().split(":")[0] + " : 100 ";
                }
                String actStr = getReadableTime(clu.get(i).getTime()) + "\n" + gactStr;
                if (!actString.equals("Nact: ")) {
                    actStr += "\n" + actString;
                }

                clu.get(i).setActivity(actStr);
                resClu.add(clu.get(i));

            // This is for the link between clusters
            long nlinkduration=0;
            if(i<clu.size()-1){
                long linkStartTime =clu.get(i).getTime()+(long)(clu.get(i).getDuration()*1000); //(long)endTime;
                //Log.i("link start:",getReadableTime(linkStartTime));
                long linkEndTime = clu.get(i + 1).getTime();
                //ArrayList<Data> ldata = RealmController.getInstance().getDailyData(linkStartTime,linkEndTime);
                List<GeoActivity> lrgeoAct = RealmController.getInstance().getDailyGeoData(linkStartTime,linkEndTime);
                Map<String,Long> lmap = new HashMap<String, Long>();
                Map<String,Long> lgmap = new HashMap<String, Long>();
                // Getting the first activity for the cluster.
                GeoActivity lfirstAct = RealmController.getInstance().getLastDailyGeoData(linkStartTime);
                //lfirstAct.setTime(clu.get(i).getTime());
                List<GeoActivity> lgeoAct = new ArrayList<GeoActivity>();
                if(!(lfirstAct==null)){
                    if(lfirstAct.getTime()!=0&&lfirstAct.getgAct()!=null){
                        lgeoAct.add(lfirstAct);
                    }
                }

                lgeoAct.addAll(lrgeoAct);
                Log.i("lrgeo", lgeoAct.size() + "");


                // iterate till j-1 as we need the duration for the samples.
                for(int j=0;j<lgeoAct.size();j++){


                    String gAct = lgeoAct.get(j).getgAct().split(":")[0];
                    Log.i("clu:"+i+" rec:"+j+" act:",gAct+" tym:"+getReadableTime(lgeoAct.get(j).getTime()));
                    if(!gAct.equals("")){
                        // if google activity exists, then update the duration
                        long stTime = lgeoAct.get(j).getTime();
                        if(j==0){
                            stTime = linkStartTime;
                        }
                        long eTime=0;
                        if(j<lgeoAct.size()-1){
                            eTime= lgeoAct.get(j + 1).getTime();
                        }
                        if(j==lgeoAct.size()-1){
                            eTime = (long)linkEndTime;
                        }
                        long duration = eTime-stTime;
                        if (lgmap.containsKey(gAct)) {
                            Long dur = lgmap.get(gAct);
                            // Getting the duration for this activity!

                            dur += duration;
                            lgmap.put(gAct, dur);
                        } else {
                            //long duration = lgeoAct.get(j + 1).getTime() - lgeoAct.get(j).getTime();
                            lgmap.put(gAct, duration);
                        }
                        // if activity is vehicle,
                        /*if(gAct.equals("Vehicle")){

                                List<NipunActivity> nipunAct = RealmController.getInstance().getDailyNipunData(stTime, eTime);
                                if (nipunAct.size() > 0) {
                                    nlinkduration += duration;
                                    for (int h = 0; h < nipunAct.size() - 1; h++) {
                                        if (nipunAct.get(h).getnAct() != "") {
                                            // if nipun activity exists, then update the duration
                                            String nAct = nipunAct.get(h).getnAct().split(":")[0];
                                            long nsTime = nipunAct.get(h).getTime();
                                            if (h == 0) {
                                                nsTime = stTime;
                                            }
                                            long neTime = 0;
                                            if(h<nipunAct.size()-1){
                                                neTime = nipunAct.get(h + 1).getTime();
                                            }
                                            if (h == nipunAct.size() - 1) {
                                                neTime = eTime;
                                            }


                                            long nduration = neTime - nsTime;

                                            if (map.containsKey(nAct)) {
                                                Long dur = map.get(nAct);
                                                dur += nduration;
                                                map.put(nAct, dur);
                                            } else {
                                                map.put(nAct, nduration);
                                            }
                                        }
                                    }


                                }

                        }*/




//dont touch
                    }

                }
                //CAlculate the percentage for each activity
                String lactString = "Nact: ";
                String lgactStr = "Gact: ";
                Object[] lkeys = lmap.keySet().toArray();
                long ltotDuration = linkEndTime-linkStartTime;
                for(int k =0; k<lmap.size();k++){

                    long actDur = lmap.get(lkeys[k]);
                    String activity = lkeys[k].toString();
                    if(actDur>0 && !activity.equals("")) {
                        //int percentage = (int) Math.round((actDur/totDuration)*100);

                        //Log.d("total_duration", totDuration+"");
                        long percentage = (long)((float)actDur*100/nlinkduration);
                        //Log.d(keys[k].toString(), actDur + "");
                        if(percentage>0) {
                            lactString = lactString + activity + " : " + percentage + "  ";
                        }
                    }
                }
                Object[] lgkeys = lgmap.keySet().toArray();
                for(int k =0; k<lgmap.size();k++){

                    long actDur = lgmap.get(lgkeys[k]);
                    String activity = lgkeys[k].toString();
                    if(actDur>0 && !activity.equals("")) {
                        //int percentage = (int) Math.round((actDur/totDuration)*100);

                        //Log.d("total_duration", totDuration+"");
                        long percentage = (long)((float)actDur*100/ltotDuration);
                        //Log.d(keys[k].toString(), actDur + "");
                        if(percentage>0) {
                            lgactStr = lgactStr + activity + " : " + percentage + "  ";
                        }
                    }
                }
                if (lgeoAct.size() == 1) {
                    //actString = geoAct.get(0).getnAct().split(":")[0] + " : 100 ";
                    lgactStr = lgeoAct.get(0).getgAct().split(":")[0] + " : 100 ";
                }
                Cluster linkCluster = new Cluster(0,0,0,0,0);
                String linkact = getReadableTime(linkStartTime) + "\n" +  lgactStr;
                if(!lactString.equals("Nact: ")){
                    linkact += "\n" + lactString;
                }
                linkCluster.setActivity(linkact);
                resClu.add(linkCluster);



            }
        }
        return resClu;
    }

    private ArrayList<Cluster> getRawData(ArrayList<Data> dataArray){

        ArrayList<Cluster> clu = new ArrayList<Cluster>();
        for(int i =0; i<dataArray.size(); i++){

            Cluster cluster = new Cluster(dataArray.get(i).getLatitude(), dataArray.get(i).getLongitude(), dataArray.get(i).getTime(), 0, dataArray.get(i).getAccuracy());
            cluster.setActivity("");
            clu.add(cluster);

        }

        return clu;
    }
    /*private ArrayList<Long> getNoData(ArrayList<Cluster> clu, long startTime, long endTime){
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
    }*/
    private String getReadableTime(long time){
        SimpleDateFormat formatter    =   new    SimpleDateFormat    ("HH:mm");

        Date d = new Date(time);
        String    strTime    =    formatter.format(d);
        //Log.d("time", strTime);


        /*Date startDate=new Date();
        try {
            startDate = formatter.parse(strTime);
            String newDateString = formatter.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        return strTime;
    }

    private String getDateString(long time){
        SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd");

        Date d = new Date(time);
        String    strTime    =    formatter.format(d);
        return strTime;
    }
    private void draw_timeline(ArrayList<Cluster> cluster) throws IOException {
        // Create Timeline rows List
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();


        for(int i =0; i<cluster.size();i++) {
            // Cluster
            if(cluster.get(i).getLatitude()>0) {
                // Create new timeline row (Row Id)
                TimelineRow myRow = new TimelineRow(0);

                // To set the row Date (optional)
                //myRow.setDate(getReadableTime(cluster.get(i).getTime()));

                // To set the row Title (optional)
                String address = getAddress(cluster.get(i));
                if(address.equals("Place")){
                    address = address + " "+ (i+1);
                }
                myRow.setTitle(address);
                // To set the row Description (optional)
                myRow.setDescription(cluster.get(i).getActivity());
                // To set the row bitmap image (optional)
                myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.location));
                // To set row Below Line Color (optional)
                myRow.setBellowLineColor(Color.argb(255, 0, 200, 0));
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
            else if(cluster.get(i).getLatitude()==0){
                // Create new timeline row (Row Id)
                TimelineRow myRow = new TimelineRow(0);

                // To set the row Date (optional)
                //myRow.setDate(getReadableTime(cluster.get(i).getTime()));

                // To set the row Title (optional)
               // myRow.setTitle(getAddress(cluster.get(i)));
                // To set the row Description (optional)
                myRow.setDescription(cluster.get(i).getActivity());
                // To set the row bitmap image (optional)
                myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.activity));
                // To set row Below Line Color (optional)
                myRow.setBellowLineColor(Color.argb(255, 0, 200, 0));
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
        String address = "Place";
        try {
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(clu.getLatitude(), clu.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
        }catch(IOException ioe){

        }
        return address ;
    }

    public boolean checkIfToday( long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);
        Calendar now = Calendar.getInstance();
        boolean res = false;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            res=true;
        }
        return res;
    }

}
