package dev.sutd.hdb.WifiCluster;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


import models.ScanInformation;
import models.ScanObject;

/**
 * Created by Hasala on 18/4/2018.
 * This class is used to do the processing related to cluster list returned by DBSCAN Clusterer
 */

public class ClusterProcessor {
    private ArrayList<ArrayList<ScanInformation>> rawClusterList;
    private ArrayList<Integer> clusterIDList;
    private ArrayList<Long> timestampList;
    private ArrayList<String> macList;
    private ArrayList<Integer> rssiList;
    private int count;

    private ArrayList<ScanInformation> allScansInClusters;
    private ArrayList<Integer> allClusterIDs;

    public void preProcessClusterList(ArrayList<ArrayList<ScanInformation>> clusterList){
        this.rawClusterList.addAll(clusterList);
        count = 0; // counter for array list index
        // loop through array of clusters
        for(int idx=0;idx<rawClusterList.size();idx++){
            ArrayList<ScanInformation> tmp_cluster = rawClusterList.get(idx); // looping through list of clusters
            int numOfScansInCluster = tmp_cluster.size();
            // loop through array of wifi scans
            for(int j=0;j<numOfScansInCluster;j++){
                ScanInformation tmp_scan = tmp_cluster.get(j); // list of wifi fingerprints, looping through list of scans
                // loop through each scan (list of scan result objects)
                for(int k=0;k<tmp_scan.getScanObjectList().size();k++) {
                    ScanObject tmp_list = tmp_scan.getScanObjectList().get(k); // looping through ScanResult object of each scan
                    //Long tmp_time = tmp_list.getTimeStamp();
                    String tmp_bssid = tmp_list.getBSSID();
                    int tmp_rssi = tmp_list.getRSSI();
                    clusterIDList.add(count,idx);
                    macList.add(count,tmp_bssid);
                    rssiList.add(count,tmp_rssi);
                    count++;
                }
            }
        }
    }
    public ArrayList<ClusterIDScanInfo> sortWithTime(ArrayList<ArrayList<ScanInformation>> clusterList){
        ArrayList<ClusterIDScanInfo> list_clusIDScanInfo = new ArrayList<>();
        //allScansInClusters = new ArrayList<>();
        //allClusterIDs = new ArrayList<>();
        for(int x=0;x<clusterList.size();x++){
            for(int y=0;y<clusterList.get(x).size();y++){
                ScanInformation tmp_Scan_Information = clusterList.get(x).get(y);
                ClusterIDScanInfo clusIDScanInfo = new ClusterIDScanInfo();
                clusIDScanInfo.setClusterID(x);
                clusIDScanInfo.setTimestamp(tmp_Scan_Information.getTimestamp());
                clusIDScanInfo.setScanObjectList(tmp_Scan_Information.getScanObjectList());
                list_clusIDScanInfo.add(clusIDScanInfo);
                //allScansInClusters.add(tmp_Scan_Information); // add the scan information object
                //allClusterIDs.add(x); // add the cluster ID
            }
        }
       // start sorting
        Collections.sort(list_clusIDScanInfo, new ScanInfoComparator());
        return list_clusIDScanInfo;
    }
    // datetime processor
    public String getDateTime(long time){
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateFormatted = formatter.format(date);
        return dateFormatted;

    }
    // get start time of cluster
    public long getStartTime(ClusterObject clusObject){
        ArrayList<Long> timestamps = clusObject.getTimestamps();
        Collections.sort(timestamps);
        return  timestamps.get(0);
    }
    // get end time of cluster
    public long getEndTime(ClusterObject clusterObject) {
        ArrayList<Long> timestamps = clusterObject.getTimestamps();
        Collections.reverse(timestamps);
        return timestamps.get(0);
    }
    public String getExportFileName(){
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("MMddHHmmss");
        String filename = "POI_RAW_"+formatter.format(date)+".txt";
        return filename;
    }
}
