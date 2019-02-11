package models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

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
    /*public ArrayList<ScanObject> getTestScanObjectList() {
        JSONArray jArr = null;
        ArrayList<ScanObject> res = new ArrayList<>();
        try {
            jArr = new JSONArray(this.scanObjectList);
            for(int i =0;i<jArr.length();i++){
                JSONObject jo = jArr.getJSONObject(i);
                ScanObject so = new ScanObject();
                so.setBSSID(jo.getString("BSSID"));
                so.setRSSI(jo.getInt("RSSI"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }*/
    public RealmList<ScanObject> getScanObjectList() {
        return this.scanObjectList;
    }
    public void setScanObjectList(RealmList<ScanObject> scanObjectList) {
        //this.scanObjectList = new RealmList<>();
        this.scanObjectList.addAll(scanObjectList);
    }

}
