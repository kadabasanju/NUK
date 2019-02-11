package models;

import io.realm.RealmObject;

/**
 * Created by Hasala on 4/6/2018.
 */

public class POIVisitInfoObject  {

    private String deviceID;
    private String POI_ID;
    private long startTime;
    private long endTime;

    public POIVisitInfoObject(String deviceID, String POI_ID, long startTime, long endTime) {
        this.deviceID = deviceID;
        this.POI_ID = POI_ID;
        this.startTime = startTime;
        this.endTime = endTime;
    }



    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getPOI_ID() {
        return POI_ID;
    }

    public void setPOI_ID(String POI_ID) {
        this.POI_ID = POI_ID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}

