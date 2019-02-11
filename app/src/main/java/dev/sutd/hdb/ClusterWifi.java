package dev.sutd.hdb;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import models.POIVisitInfoObject;
import models.ScanInformation;
import models.ScanObject;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ClusterWifi {


    int MIN_SCANS=4;
    Realm realm =Realm.getDefaultInstance();//RealmController.with(MyApp.getInstance()).getRealm(); //Realm.getDefaultInstance();

    private final long MIN_POI_DURATION = 720000; // 3 * 60 * 1000 * 4 milliseconds
    String device_id;


   public void clusterWifi() {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        device_id = sharedpreferences.getString("DEVICE_ID","");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initiateClusteringProcess(0.5,0.5);
            }
        }, 700);

    }


    public void initiateClusteringProcess(double threshold, double threshold_matching){
        long startTime = System.currentTimeMillis();
        ArrayList<ScanInformation> receivedData =queryFromDatabase();//RealmController.with(MyApp.getInstance()).queryFromDatabase();//queryFromDatabase();
        System.out.println("Received data size: "+receivedData.size());
        if(receivedData.size()>=MIN_SCANS){


            ArrayList<ArrayList<ScanInformation>> clusterList = executeClustering(receivedData, threshold);
            // validate
            System.out.println(clusterList.size());
            if(clusterList!=null) {
                //long stopTime = System.currentTimeMillis();
                //long elapsedTime = stopTime - startTime;

                ClusterProcessor processor = new ClusterProcessor();
                ArrayList<ClusterIDScanInfo> clusIDScanInfo= processor.sortWithTime(clusterList); // get time sorted cluster IDs


                ////
                ArrayList<ClusterObject> clusterObjectList = new ArrayList<>();
                ArrayList<Long> timestamps = new ArrayList<>();
                ClusterObject clusObject;
                boolean isLast = false;
                int size = clusIDScanInfo.size();
                Log.e("ClusterIdScanInfo", size+"");
                // if size is 1, it does not enter the for loop.
                if(size==1){
                    timestamps.add(clusIDScanInfo.get(0).getTimestamp());
                    clusObject = new ClusterObject();
                    clusObject.setTimestamps(timestamps);
                    clusObject.setClusterID(clusIDScanInfo.get(0).getClusterID());
                    clusObject.setScanObjectList(clusIDScanInfo.get(0).getScanObjectList()); // newly added
                    boolean passByStatus = checkIfJustPassby(timestamps);
                    if(!passByStatus) {
                        clusterObjectList.add(clusObject);
                    }
                }
                else {
                    for (int k = 0; k < clusIDScanInfo.size() - 1; k++) {
                        if (clusIDScanInfo.get(k).getClusterID() == clusIDScanInfo.get(k + 1).getClusterID()) {
                            timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                            if (isLast) {
                                clusObject = new ClusterObject();
                                clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                                clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                                clusObject.setTimestamps(timestamps);
                                boolean passByStatus = checkIfJustPassby(timestamps);
                                if (!passByStatus) {
                                    clusterObjectList.add(clusObject);
                                }
                            }
                        }
                        if (clusIDScanInfo.get(k).getClusterID() != clusIDScanInfo.get(k + 1).getClusterID()) {
                            timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                            clusObject = new ClusterObject();
                            clusObject.setTimestamps(timestamps);
                            clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                            clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                            boolean passByStatus = checkIfJustPassby(timestamps);
                            if (!passByStatus) {
                                clusterObjectList.add(clusObject);
                            }
                            timestamps = new ArrayList<>();
                        }
                        if ((k == size - 3) && (clusIDScanInfo.get(k).getClusterID() == clusIDScanInfo.get(k + 1).getClusterID())) {
                            isLast = true;
                        }
                        if (size == 1) {
                            timestamps.add(clusIDScanInfo.get(k).getTimestamp());
                            clusObject = new ClusterObject();
                            clusObject.setTimestamps(timestamps);
                            clusObject.setClusterID(clusIDScanInfo.get(k).getClusterID());
                            clusObject.setScanObjectList(clusIDScanInfo.get(k).getScanObjectList()); // newly added
                            boolean passByStatus = checkIfJustPassby(timestamps);
                            if (!passByStatus) {
                                clusterObjectList.add(clusObject);
                            }
                        }

                    }
                }
                /*for(int p=0;p<clusterObjectList.size();p++){
                    long sTime = processor.getStartTime(clusterObjectList.get(p));
                    long eTime = processor.getEndTime(clusterObjectList.get(p));

                }*/
                compareClustersWithExistingPOI(clusterObjectList, threshold_matching);
                ////
            }
            else{
                Log.e("Cluster Wifi","Cluster point list is NULL");
            }
        }
        else if(receivedData.size()<MIN_SCANS && receivedData.size()>0){

            Log.e("Cluster Wifi","Unable to perform clustering.");
        }
        else{
            Log.e("Cluster Wifi","Unable to perform clustering. Database is empty");
        }

    }


    public void compareClustersWithExistingPOI(ArrayList<ClusterObject> clusterObjectList, double threshold_matching){
        Log.e("CompareClusters", clusterObjectList.size()+"");
        ArrayList<POIVisitInfoObject> visitsList = new ArrayList<>();
        for(int i=0; i<clusterObjectList.size();i++){
            ClusterObject clusObject = clusterObjectList.get(i);
            ArrayList<POIProperties> listOfPOI = queryFromPOIProperties();
            String POI_ID = device_id +String.format("%04d", listOfPOI.size()+1); // POI ID = device IMEI + 4 digit incrementer
            ArrayList<Long> timestamps = clusObject.getTimestamps();
            Collections.sort(timestamps);
            long startTime = timestamps.get(0);
            Collections.reverse(timestamps);
            long endTime = timestamps.get(0);
            //see if no existing POI available
            if(listOfPOI.size()==0){
                Log.e("Cluster Wifi", "Saving to POI and visits");
                saveToPOIProperties(POI_ID,clusObject.getScanObjectList());
                POIVisitInfoObject obj = new POIVisitInfoObject("deviceID",POI_ID,startTime,endTime);
                visitsList.add(obj);
                saveToPOIVisitInfo("deviceID",POI_ID,startTime,endTime);
            }
            //if POI list is NOT empty
            if(listOfPOI.size()!=0){
                for(int j=0;j<listOfPOI.size();j++){
                    POIProperties poiProperties = listOfPOI.get(j);
                    double score = computeSimilarity(poiProperties,clusObject);
                    if(score >=threshold_matching){
                        Log.e("Cluster Wifi", "Saving visits only");
                        POIVisitInfoObject obj = new POIVisitInfoObject("deviceID",POI_ID,startTime,endTime);
                        visitsList.add(obj);
                        saveToPOIVisitInfo("deviceID",poiProperties.getPOI_ID(),startTime,endTime);
                    }
                    if(score <threshold_matching){
                        Log.e("Cluster Wifi", "Saving to POI and visits, POI list non empty");
                        saveToPOIProperties(POI_ID,clusObject.getScanObjectList());
                        POIVisitInfoObject obj = new POIVisitInfoObject("deviceID",POI_ID,startTime,endTime);
                        visitsList.add(obj);
                        saveToPOIVisitInfo("deviceID",POI_ID,startTime,endTime);
                    }
                }
            }

        }
        deleteAllAfterClustering();
        uploadDataToServer(visitsList);
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
    public ArrayList<ArrayList<ScanInformation>> executeClustering(ArrayList<ScanInformation> receivedData, double threshold){
        System.out.println("Execute clustering pass data size: "+receivedData.size());
        RunClusterer clusterer = new RunClusterer();
        ArrayList<ArrayList<ScanInformation>> clusterList = clusterer.setupAndPerformClusterer(receivedData,threshold);
        return clusterList;
    }

    // delete all the records from raw database, after clustering process
    public void deleteAllAfterClustering(){
        realm.executeTransaction(new Realm.Transaction() {
                                     @Override
                                     public void execute(Realm realm) {
                                         RealmResults<ScanInformation> result = realm.where(ScanInformation.class).findAll(); // delete records up to end of yesterday
                                         result.deleteAllFromRealm();
                                     }
                                 }
        );
        Log.i("Cluster Wifi","Deleted Scan Info from the database");
    }

    //query from database
    public ArrayList<ScanInformation> queryFromDatabase() {
        ArrayList<ScanInformation> listOfScans = new ArrayList<>();

        final RealmResults<ScanInformation> receivedData = realm.where(ScanInformation.class).findAll();

        listOfScans.addAll(realm.copyFromRealm(receivedData));
        return listOfScans;
    }
    // query from POI Properties
    public ArrayList<POIProperties> queryFromPOIProperties(){
        ArrayList<POIProperties> listOfPOI = new ArrayList<>();
        final RealmResults<POIProperties> receivedData = realm.where(POIProperties.class).findAll();
        listOfPOI.addAll(realm.copyFromRealm(receivedData));
        return listOfPOI;
    }

    public ArrayList<POIProperties> queryPendingPOIProperties(){
        ArrayList<POIProperties> listOfPOI = new ArrayList<>();
        final RealmResults<POIProperties> receivedData = realm.where(POIProperties.class).equalTo("uploaded", 0).findAll();
        listOfPOI.addAll(realm.copyFromRealm(receivedData));
        return listOfPOI;
    }

    public ArrayList<POIVisitInfo> queryFromPOIVisits(){
        ArrayList<POIVisitInfo> listOfPOI = new ArrayList<>();
        final RealmResults<POIVisitInfo> receivedData = realm.where(POIVisitInfo.class).equalTo("uploaded", 0).findAll();
        listOfPOI.addAll(realm.copyFromRealm(receivedData));
        return listOfPOI;
    }
    // save to POI Properties
    public void saveToPOIProperties(final String POI_ID, final RealmList<ScanObject> fingerprint){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                POIProperties POIPropertyToSave = bgRealm.createObject(POIProperties.class);
                POIPropertyToSave.setPOI_ID(POI_ID);
                POIPropertyToSave.setFingerprint(fingerprint);

                System.out.println("fingerprint size: "+fingerprint.size());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Transaction was a success.
                //Toast.makeText(getApplicationContext(), "New fingerprint added", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                Log.e("Cluste Wifi", "Error in saving!");
            }
        });
    }
    // save to POI Visit Info
    public void saveToPOIVisitInfo(final String device_ID, final String POI_ID, final long startTime, final long endTime){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                POIVisitInfo POIVisitInfoToSave = bgRealm.createObject(POIVisitInfo.class);
                POIVisitInfoToSave.setDeviceID(device_ID);
                POIVisitInfoToSave.setPOI_ID(POI_ID);
                POIVisitInfoToSave.setStartTime(startTime);
                POIVisitInfoToSave.setEndTime(endTime);
                //POIVisitInfoToSave.setUploaded(0);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Transaction was a success.
                //Toast.makeText(getApplicationContext(), "New visit info added", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
               Log.e("ClusterWifi" , "Error in saving visit info!");
            }
        });
    }

    private void uploadDataToServer(ArrayList<POIVisitInfoObject> visitsList){
        ConnectionDetector cd = new ConnectionDetector(MyApp.getInstance());

        boolean connected;
        try {
            connected = cd.executeTask();

            if(connected)
            {
                // sending POI properties to server
                ArrayList<POIProperties> listOfProperties = queryPendingPOIProperties();//RealmController.with(MyApp.getInstance()).getPendingPOIProperties();
                Log.e("POI LIST", listOfProperties.size()+"");
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
                UpdatePOI();


                //Sending visit info to server
                ArrayList<POIVisitInfoObject> listOfVisits = visitsList;//queryFromPOIVisits(); //RealmController.with(MyApp.getInstance()).getPOIVisits();
                maxNumber = 100;
                Log.i("Cluster Wifi visits",listOfVisits.size()+"" );
                numOfLoops = (listOfVisits.size()/maxNumber) +1;
                if(listOfVisits.size()%maxNumber==0){
                    numOfLoops -= 1;
                }

                for(int j = 0; j<numOfLoops; j++){
                    int maxIndex = (j+1)*maxNumber > listOfVisits.size()?listOfVisits.size():(j+1)*maxNumber;
                    ArrayList<POIVisitInfoObject>tempsa = new ArrayList();

                    for(int k =j*maxNumber; k<maxIndex; k++) {
                        if (listOfVisits.get(k) == null) {
                            break;
                        }

                        tempsa.add(listOfVisits.get(k));
                    }


                    sendVisitHttpPost(tempsa);
                }


                // Delete Scan Info from the database
                /*realm.executeTransaction(new Realm.Transaction() {
                                             @Override
                                             public void execute(Realm realm) {
                                                 RealmResults<ScanInformation> result = realm.where(ScanInformation.class).findAll(); // delete records up to end of yesterday

                                                 result.deleteAllFromRealm();
                                             }
                                         }
                );*/
                //Log.e("Cluster Wifi", "Cleared Scan Info");

                //delete from db after clustering!1
                /*realm.executeTransaction(new Realm.Transaction() {
                                             @Override
                                             public void execute(Realm realm) {
                                                 RealmResults<POIVisitInfo> result = realm.where(POIVisitInfo.class).findAll(); // delete records up to end of yesterday

                                                 result.deleteAllFromRealm();
                                             }
                                         }
                );
                Log.e("Cluster Wifi", "Cleared Visit Info");
                */
                //UpdateVisits();


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

    private void sendVisitHttpPost(final ArrayList<POIVisitInfoObject>data) throws JSONException {

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
                                Log.i(" visits data upload", "success");

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
            MyApp.getInstance().addToRawRequestQueueHttps(postRequest, "postRequest");
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

    void UpdatePOI() {
        // This query is fast because "character" is an indexed field
        RealmResults<POIProperties> poi = realm.where(POIProperties.class)
                .findAll();
        for(int i =0;i<poi.size();i++) {
            if(realm.isInTransaction()){
                realm.commitTransaction();
            }
                    realm.beginTransaction();
                    POIProperties poiProperties = poi.get(i);
                    poiProperties.setUploaded(1);
                    realm.commitTransaction();
        }

    }



}

