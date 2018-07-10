package dev.sutd.hdb;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;
import dev.sutd.hdb.WifiCluster.ClusterIDScanInfo;
import dev.sutd.hdb.WifiCluster.ClusterObject;
import dev.sutd.hdb.WifiCluster.ClusterProcessor;
import dev.sutd.hdb.WifiCluster.CosineSimilarity;
import dev.sutd.hdb.WifiCluster.RunClusterer;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import models.POIProperties;
import models.POIVisitInfo;
import models.ScanInformation;
import models.ScanObject;

public class ClusterWifiPoi {
    // WiFi related
    private final int MIN_SCANS = 4; // minimum number of scans required in database to perform clustering

    private final long MIN_POI_DURATION = 720000; // 3 * 60 * 1000 * 4 milliseconds
    SharedPreferences sharedPreferences;
    Realm realm;
    String device_id;
    ClusterWifiPoi(){
        realm = RealmController.with(MyApp.getInstance()).getRealm();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        device_id = sharedPreferences.getString("DEVICE_ID","");
    }
    public void initiateClusteringProcess(double threshold, double threshold_matching){
        long startTime = System.currentTimeMillis();
        ArrayList<ScanInformation> receivedData =RealmController.with(MyApp.getInstance()).getScanInformation();
        System.out.println("Received data size: "+receivedData.size());
        if(receivedData.size()>=MIN_SCANS){

            ArrayList<ArrayList<ScanInformation>> clusterList = executeClustering(receivedData, threshold);
            // validate
            System.out.println(clusterList.size());
            if(clusterList!=null) {
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                //txt2.setText("No. of Unique POI: " + String.valueOf(clusterList.size())+"  "+"Process time: "+elapsedTime+"ms");
                ClusterProcessor processor = new ClusterProcessor();
                ArrayList<ClusterIDScanInfo> clusIDScanInfo= processor.sortWithTime(clusterList); // get time sorted cluster IDs
                //txt3.append("\n");

                ////
                ArrayList<ClusterObject> clusterObjectList = new ArrayList<>();
                ArrayList<Long> timestamps = new ArrayList<>();
                ClusterObject clusObject;
                boolean isLast = false;
                int size = clusIDScanInfo.size();
                for(int k=0;k<clusIDScanInfo.size()-1;k++){
                    if(clusIDScanInfo.get(k).getClusterID()==clusIDScanInfo.get(k+1).getClusterID()) {
                        timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                        if(isLast){
                            clusObject = new ClusterObject();
                            clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                            clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                            clusObject.setTimestamps(timestamps);
                            boolean passByStatus = checkIfJustPassby(timestamps);
                            if(!passByStatus) {
                                clusterObjectList.add(clusObject);
                            }
                        }
                    }
                    if(clusIDScanInfo.get(k).getClusterID()!=clusIDScanInfo.get(k+1).getClusterID()) {
                        timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                        clusObject = new ClusterObject();
                        clusObject.setTimestamps(timestamps);
                        clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                        clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                        boolean passByStatus = checkIfJustPassby(timestamps);
                        if(!passByStatus) {
                            clusterObjectList.add(clusObject);
                        }
                        timestamps = new ArrayList<>();
                    }
                    if((k==size-3) && (clusIDScanInfo.get(k).getClusterID()==clusIDScanInfo.get(k+1).getClusterID())){
                        isLast = true;
                    }
                    if(size==1){
                        timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                        clusObject = new ClusterObject();
                        clusObject.setTimestamps(timestamps);
                        clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                        clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                        boolean passByStatus = checkIfJustPassby(timestamps);
                        if(!passByStatus) {
                            clusterObjectList.add(clusObject);
                        }
                    }

                }
                for(int p=0;p<clusterObjectList.size();p++){
                    long sTime = processor.getStartTime(clusterObjectList.get(p));
                    long eTime = processor.getEndTime(clusterObjectList.get(p));
                    //txt3.append(clusterObjectList.get(p).getClusterID()+" - "+processor.getDateTime(sTime)+" - "+processor.getDateTime(eTime)+"\n");
                }
                compareClustersWithExistingPOI(clusterObjectList, threshold_matching);
                ////
            }
            else{
                //Toast.makeText(MainActivity.this,"Cluster point list is NULL",Toast.LENGTH_SHORT).show();
            }
        }
        else if(receivedData.size()<MIN_SCANS && receivedData.size()>0){
            //txt.setText("No. of records in database: "+receivedData.size());
            //Toast.makeText(MainActivity.this,"Unable to perform clustering. Minimum of "+MIN_SCANS+" scans required",Toast.LENGTH_SHORT).show();
        }
        else{
            //Toast.makeText(MainActivity.this,"Unable to perform clustering. Database is empty",Toast.LENGTH_SHORT).show();
        }

    }

    public void compareClustersWithExistingPOI(ArrayList<ClusterObject> clusterObjectList, double threshold_matching){
        for(int i=0; i<clusterObjectList.size();i++){
            ClusterObject clusObject = clusterObjectList.get(i);
            ArrayList<POIProperties> listOfPOI = RealmController.with(MyApp.getInstance()).getPOIProperties();

            String POI_ID = device_id+String.format("%04d", listOfPOI.size()+1); // POI ID = device IMEI + 4 digit incrementer
            ArrayList<Long> timestamps = clusObject.getTimestamps();
            Collections.sort(timestamps);
            long startTime = timestamps.get(0);
            Collections.reverse(timestamps);
            long endTime = timestamps.get(0);
            //see if no existing POI available
            if(listOfPOI.size()==0){

                saveToPOIProperties(POI_ID,clusObject.getScanObjectList());
                saveToPOIVisitInfo("deviceID",POI_ID,startTime,endTime);


            }
            //if POI list is NOT empty
            else{
                for(int j=0;j<listOfPOI.size();j++){
                    POIProperties poiProperties = listOfPOI.get(j);
                    double score = computeSimilarity(poiProperties,clusObject);
                    if(score >=threshold_matching){
                        saveToPOIVisitInfo(device_id,poiProperties.getPOI_ID(),startTime,endTime);
                    }
                    else{
                        saveToPOIProperties(POI_ID,clusObject.getScanObjectList());
                        saveToPOIVisitInfo(device_id,POI_ID,startTime,endTime);
                    }
                }
            }
            uploadDataToServer();
            // Delete all scanInformation from database after clustering.
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<ScanInformation> rows = realm.where(ScanInformation.class).findAll();
                    rows.clear();
                }
            });

        }
    }


    public void saveToPOIProperties(final String POI_ID, final RealmList<ScanObject> fingerprint){
        realm.beginTransaction();
        POIProperties pp = realm.createObject(POIProperties.class);
        pp.setPOI_ID(POI_ID);
        pp.setUploaded(0);
        pp.setFingerprint(fingerprint);
        realm.commitTransaction();
    }


    public void saveToPOIVisitInfo(final String device_ID, final String POI_ID, final long startTime, final long endTime){
        realm.beginTransaction();
        POIVisitInfo pv = realm.createObject(POIVisitInfo.class);
        pv.setDeviceID(device_ID);
        pv.setEndTime(endTime);
        pv.setStartTime(startTime);
        pv.setPOI_ID(POI_ID);
        realm.commitTransaction();

    }
    // This method executed the DBSCAN clustering algorithm
    public ArrayList<ArrayList<ScanInformation>> executeClustering(ArrayList<ScanInformation> receivedData, double threshold){
        System.out.println("Execute clustering pass data size: "+receivedData.size());
        RunClusterer clusterer = new RunClusterer();
        ArrayList<ArrayList<ScanInformation>> clusterList = clusterer.setupAndPerformClusterer(receivedData,threshold);
        return clusterList;
    }

    // check if the cluster is just a pass by location
    public boolean checkIfJustPassby(ArrayList<Long> tStamps){
        boolean isPassBy;
        Collections.sort(tStamps);
        long sT = tStamps.get(0);
        Collections.reverse(tStamps);
        long eT = tStamps.get(0);
        long tGap = eT - sT;
        if(tGap<MIN_POI_DURATION){
            isPassBy = true;
        }
        else{
            isPassBy = false;
        }
        return  isPassBy;
    }

    // compute similarity score with existing POI
    public double computeSimilarity(POIProperties poiProperties, ClusterObject clusterObject){
        Map<CharSequence,Integer> fingerprint_new = new LinkedHashMap<CharSequence, Integer>();
        Map<CharSequence,Integer> fingerprint_POI = new LinkedHashMap<CharSequence, Integer>();
        for(int x=0; x<poiProperties.getFingerprint().size();x++){
            fingerprint_POI.put(poiProperties.getFingerprint().get(x).getBSSID(), poiProperties.getFingerprint().get(x).getRSSI());
        }
        for(int y=0; y<clusterObject.getScanObjectList().size();y++){
            fingerprint_new.put(clusterObject.getScanObjectList().get(y).getBSSID(), clusterObject.getScanObjectList().get(y).getRSSI());
        }
        double score =0;
        CosineSimilarity similarity = new CosineSimilarity();
        score = similarity.cosineSimilarity(fingerprint_new,fingerprint_POI);
        return score;
    }

    private void uploadDataToServer(){
        ConnectionDetector cd = new ConnectionDetector(MyApp.getInstance());

        boolean connected;
        try {
            connected = cd.executeTask();

            if(connected)
            {
                // sending POI properties to server
                ArrayList<POIProperties> listOfProperties = RealmController.with(MyApp.getInstance()).getPendingPOIProperties();
                int maxNumber = 100;
                int numOfLoops = (listOfProperties.size()/maxNumber) +1;
                if(listOfProperties.size()%maxNumber==0){
                    numOfLoops -= 1;
                }

                for(int j = 0; j<numOfLoops; j++){
                    int maxIndex = (j+1)*maxNumber > listOfProperties.size()?listOfProperties.size():(j+1)*maxNumber;
                    ArrayList<POIProperties>tempsa = new ArrayList();

                    for(int k =j*maxNumber; k<maxIndex; k++) {
                        if (listOfProperties.get(k) == null) {
                            break;
                        }

                        tempsa.add(listOfProperties.get(k));
                    }


                    //sendPOIHttpPost(tempsa);
                    sendPOIInfoToServer(tempsa);
                }


                //Sending visit info to server
                ArrayList<POIVisitInfo> listOfVisits = RealmController.with(MyApp.getInstance()).getPOIVisits();
                maxNumber = 100;
                numOfLoops = (listOfVisits.size()/maxNumber) +1;
                if(listOfVisits.size()%maxNumber==0){
                    numOfLoops -= 1;
                }

                for(int j = 0; j<numOfLoops; j++){
                    int maxIndex = (j+1)*maxNumber > listOfVisits.size()?listOfVisits.size():(j+1)*maxNumber;
                    ArrayList<POIVisitInfo>tempsa = new ArrayList();

                    for(int k =j*maxNumber; k<maxIndex; k++) {
                        if (listOfVisits.get(k) == null) {
                            break;
                        }

                        tempsa.add(listOfVisits.get(k));
                    }


                    sendVisitHttpPost(tempsa);
                }



            }
            else {
                System.out.println("Not Connected");
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendPOIHttpPost(final ArrayList<POIProperties>data) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        try{
            for(int i = 0; i < data.size(); i++){
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("POI_ID", data.get(i).getPOI_ID());
                JSONArray array = new JSONArray();
                RealmList<ScanObject> scanArr = data.get(i).getFingerprint();
                for(int j =0;j<scanArr.size();j++){
                    JSONObject obj = new JSONObject();
                    ScanObject scanObj = scanArr.get(j);
                    obj.put("RSSI",scanObj.getRSSI());
                    obj.put("MAC",scanObj.getBSSID());
                    array.put(obj);
                }
                formDetailsJson.put("fingerprint", array);

                jsonArray.put(formDetailsJson);
            }

        }catch(Exception e){

        }
        System.out.println(jsonArray.toString());
        final String dataString = jsonArray.toString();
        //System.out.println(dataString);
        String url = ApiUrl.insertPreparedUrl;
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response

                            int httpRes = Integer.valueOf(response);
                            if (httpRes == 1) {
                                //deleting all visitor info from local db
                                for (int i = 0; i < data.size(); i++) {
                                    //System.out.println(tempAa.get(i).getTime());
                                    if(realm.isInTransaction()){
                                        realm.commitTransaction();
                                    }
                                    POIProperties d = data.get(i);
                                    realm.beginTransaction();
                                    d.setUploaded(1);
                                    realm.commitTransaction();
                                }

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.e("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("data", dataString);
                    params.put("type", "5");
                    params.put("device_id", device_id);
                    System.out.println(params);
                    return params;
                }
            };
            //queue.add(postRequest);

            // Adding the request to the queue along with a unique string tag
            MyApp.getInstance().addToRequestQueue(postRequest, "postRequest");
        }catch(NumberFormatException e){
            Log.e("upload", "error from php");
        }

    }

    private void sendVisitHttpPost(final ArrayList<POIVisitInfo>data) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        try{
            for(int i = 0; i < data.size(); i++){
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("POI_ID", data.get(i).getPOI_ID());
                formDetailsJson.put("start_time", data.get(i).getStartTime());
                formDetailsJson.put("end_time", data.get(i).getEndTime());
                jsonArray.put(formDetailsJson);
            }

        }catch(Exception e){

        }
        System.out.println(jsonArray.toString());
        final String dataString = jsonArray.toString();
        //System.out.println(dataString);
        String url = ApiUrl.insertPreparedUrl;
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response

                            int httpRes = Integer.valueOf(response);
                            if (httpRes == 1) {
                                //deleting all visitor info from local db
                                for (int i = 0; i < data.size(); i++) {
                                    if(realm.isInTransaction()){
                                        realm.commitTransaction();
                                    }
                                    POIVisitInfo p = data.get(i);
                                    realm.beginTransaction();
                                    p.removeFromRealm();
                                    realm.commitTransaction();
                                }

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.e("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("data", dataString);
                    params.put("type", "6");
                    params.put("device_id", device_id);
                    System.out.println(params);
                    return params;
                }
            };
            //queue.add(postRequest);

            // Adding the request to the queue along with a unique string tag
            MyApp.getInstance().addToRequestQueue(postRequest, "postRequest");
        }catch(NumberFormatException e){
            Log.e("upload", "error from php");
        }

    }


    public void sendPOIInfoToServer(final ArrayList<POIProperties>data ) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray();
                    try{
                        for(int i = 0; i < data.size(); i++){
                            JSONObject formDetailsJson = new JSONObject();
                            formDetailsJson.put("POI_ID", data.get(i).getPOI_ID());
                            formDetailsJson.put("device_id", device_id);
                            JSONArray array = new JSONArray();
                            RealmList<ScanObject> scanArr = data.get(i).getFingerprint();
                            for(int j =0;j<scanArr.size();j++){
                                JSONObject obj = new JSONObject();
                                ScanObject scanObj = scanArr.get(j);
                                obj.put("RSSI",scanObj.getRSSI());
                                obj.put("MAC",scanObj.getBSSID());
                                array.put(obj);
                            }
                            formDetailsJson.put("fingerprint", array);

                            jsonArray.put(formDetailsJson);
                        }

                    }catch(Exception e){

                    }

                    String body = jsonArray.toString();
                    URL url = new URL("http://103.24.77.43/test/testComp.php");
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-encoding", "gzip");
                    conn.setRequestProperty("Content-type", "application/octet-stream");
                    GZIPOutputStream dos = new GZIPOutputStream(conn.getOutputStream());
                    dos.write(body.getBytes());
                    dos.flush();
                    dos.close();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();

                    String decodedString = "";
                    while ((decodedString = in.readLine()) != null) {
                        builder.append(decodedString);
                    }
                    in.close();
                    Log.e("Data", builder.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        thread.start();
    }

}
