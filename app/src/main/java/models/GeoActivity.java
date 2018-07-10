package models;

import io.realm.RealmObject;

/**
 * Created by Sanj on 2/23/2018.
 */
public class GeoActivity extends RealmObject {
    //activity Recognition by Google
    private String gAct;

    //Transportation mode

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




}
