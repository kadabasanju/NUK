package models;

import io.realm.RealmObject;

/**
 * Created by Hasala on 4/6/2018.
 */

public class POIVisitInfo extends RealmObject {

    private String deviceID;
    private String POI_ID;
    private long startTime;
    private long endTime;

    /*public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    private int uploaded=0;
*/
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

