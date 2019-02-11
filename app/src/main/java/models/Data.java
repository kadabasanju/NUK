package models;

import io.realm.RealmObject;

/**
 * Created by Sanj on 1/19/2018.
 */
public class Data extends RealmObject {
    //GPS Location
    private double latitude;
    private double longitude;
    private double accuracy;
    private long time;
    private double speed;
    private double altitude;
    //activity Recognition by Google
   /* private String gAct;
    //Transportation mode
    private String activity;

    //Socio activity-noise
    private String socio_activity;
    private double decibel=0;
    //mics
    private int battery;
    private double light;
    //network stuff
    private String network_id;
    private String ip_address;*/
    private String wifiAPs;
    private int uploaded ;


    public Data(){
        uploaded=0;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

   /* public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
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
    }*/

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }


   public String getWifiAPs() {
        return wifiAPs;
    }

    public void setWifiAPs(String wifiAPs) {
        this.wifiAPs = wifiAPs;
    }

   /* public String getgAct() {
        return gAct;
    }

    public void setgAct(String gAct) {
        this.gAct = gAct;
    }*/
}
