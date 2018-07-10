package models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Hasala on 21/4/2018.
 */

public class ScanInformation extends RealmObject{
    private long timestamp;
    private RealmList<ScanObject> scanObjectList;

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public RealmList<ScanObject> getScanObjectList() {
        return scanObjectList;
    }
    public void setScanObjectList(RealmList<ScanObject> scanObjectList) {
        this.scanObjectList.addAll(scanObjectList);
    }

}
