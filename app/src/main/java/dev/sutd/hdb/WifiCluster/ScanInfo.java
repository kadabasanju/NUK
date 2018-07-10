package dev.sutd.hdb.WifiCluster;

import java.util.ArrayList;

import models.ScanObject;

/**
 * Created by Hasala on 19/4/2018.
 * Object to create time stamp of the scan and the list of scans (mac and rssi)
 */

public class ScanInfo {
    private long timeStamp;
    private ArrayList<ScanObject> scanObjectList;

    public ScanInfo(long time, ArrayList<ScanObject> scanList) {
        setTimeSamp(time);
        setScanObjects(scanList);
    }
    // setter methods
    public void setTimeSamp(long time){
        this.timeStamp = time;
    }
    public void setScanObjects(ArrayList<ScanObject> scanList){
        scanObjectList = new ArrayList<ScanObject>();
        this.scanObjectList.addAll(scanList);
    }
    // getter methods
    public long getTimeStamp(){
        return this.timeStamp;
    }
    public ArrayList<ScanObject> getScanObjects(){
        return this.scanObjectList;
    }


}
