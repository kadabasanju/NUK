package models;

import io.realm.RealmObject;

/**
 * Created by Sanj on 2/23/2018.
 */
public class Sound extends RealmObject {
    //Socio activity-noise
    private String socio_activity;
    private double decibel;
    //mics
    private int battery;
    private double light;
    //network stuff
    private String network_id;
    private String ip_address;
    private int uploaded;
    private long time;

    public Sound(){
        uploaded=0;
    }

    public String getSocio_activity() {
        return socio_activity;
    }

    public void setSocio_activity(String socio_activity) {
        this.socio_activity = socio_activity;
    }

    public double getDecibel() {
        return decibel;
    }

    public void setDecibel(double decibel) {
        this.decibel = decibel;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public String getNetwork_id() {
        return network_id;
    }

    public void setNetwork_id(String network_id) {
        this.network_id = network_id;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
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
