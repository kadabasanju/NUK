package models;

import io.realm.RealmObject;

/**
 * Created by Sanj on 2/23/2018.
 */
public class NipunActivity extends RealmObject {
    //activity Recognition by Google

    //Transportation mode
    private String nAct;

    private int uploaded ;
    private long time;

    public NipunActivity(){
        uploaded=0;
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




}
