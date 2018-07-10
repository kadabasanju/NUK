package models;

import io.realm.RealmObject;

/**
 * Created by Hasala on 18/4/2018.
 */

public class ScanObject extends RealmObject {
    private String BSSID; // MAC address of the AP
    private int RSSI; // RSSI value in dBm

    public String getBSSID() {
        return BSSID;
    }
    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }
    public int getRSSI() {
        return RSSI;
    }
    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }
}
