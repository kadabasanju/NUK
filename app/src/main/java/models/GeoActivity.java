package models;

import io.realm.RealmObject;

/**
 * Created by Sanj on 2/23/2018.
 */
public class GeoActivity extends RealmObject {
    //activity Recognition by Google
    private String gAct;
    private float g_confidence;
    //Transportation mode
    private String nAct;
    private float n_confidence;
    private int uploaded ;
    private long time;

    public GeoActivity(){
        uploaded=0;
    }

    public String getgAct() {
        return gAct;
    }

    public void setgAct(String gAct) {
        this.gAct = gAct;
    }

    public String getnAct() {
        return nAct;
    }

    public void setnAct(String nAct) {
        this.nAct = nAct;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getG_confidence() {
        return g_confidence;
    }

    public void setG_confidence(float g_confidence) {
        this.g_confidence = g_confidence;
    }

    public float getN_confidence() {
        return n_confidence;
    }

    public void setN_confidence(float n_confidence) {
        this.n_confidence = n_confidence;
    }
}
