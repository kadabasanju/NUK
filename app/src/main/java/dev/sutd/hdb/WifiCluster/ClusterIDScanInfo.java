package dev.sutd.hdb.WifiCluster;


import java.util.ArrayList;

import io.realm.RealmList;
import models.ScanObject;

/**
 * Created by Hasala on 21/4/2018.
 */

public class ClusterIDScanInfo {
    private long timestamp;
    private int clusterID;
    private RealmList<ScanObject> scanObjectList;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public RealmList<ScanObject> getScanObjectList() {
        return scanObjectList;
    }
    public void setScanObjectList(RealmList<ScanObject> scanObjectList) {
        this.scanObjectList = scanObjectList;
    }

}
