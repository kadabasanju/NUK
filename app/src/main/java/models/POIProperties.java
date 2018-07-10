package models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Hasala on 4/6/2018.
 */

public class POIProperties extends RealmObject{
    private String POI_ID;
    private RealmList<ScanObject> fingerprint;

    private int uploaded=0;



    public String getPOI_ID() {
        return POI_ID;
    }

    public void setPOI_ID(String POI_ID) {
        this.POI_ID = POI_ID;
    }

    public RealmList<ScanObject> getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(RealmList<ScanObject> fingerprint) {
        this.fingerprint.addAll(fingerprint);
    }

    public int getUploaded(){return this.uploaded;}

    public void setUploaded(int uploaded){
        this.uploaded = uploaded;
    }
}
