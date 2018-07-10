package dev.sutd.hdb.WifiCluster;

import java.util.ArrayList;


import io.realm.RealmList;
import models.ScanObject;

/**
 * Created by Hasala on 22/4/2018.
 */

public class ClusterObject {
    private int clusterID;
    private ArrayList<Long> timestamps;
    private RealmList<ScanObject> fingerprint;

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public ArrayList<Long> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(ArrayList<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public RealmList<ScanObject> getScanObjectList() {
        return fingerprint;
    }

    public void setScanObjectList(RealmList<ScanObject> scanObjectList) {
        this.fingerprint = scanObjectList;
    }
}
