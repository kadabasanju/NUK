package dev.sutd.hdb;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import models.Data;
import models.GeoActivity;
import models.NipunActivity;
import models.POIProperties;
import models.POIVisitInfo;
import models.ScanInformation;
import models.Sound;


/**
 * Created by Sanj on 5/19/2017.
 */
public class RealmController {
    private static RealmController instance;
    private  final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {

        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.refresh();
    }


    public RealmResults<models.Data> getPendingUpload() {

        return realm.where(models.Data.class).equalTo("uploaded", 0).findAll();
    }

    public RealmResults<models.Data> getData() {

        return realm.where(models.Data.class).findAll();
                //equalTo("trip_id", trip_id).findAll();
    }

    public ArrayList<Data> getDailyData(long startDate, long endDate) {
        ArrayList<Data> res = new ArrayList<>();
        RealmResults<models.Data> data = realm.where(models.Data.class).lessThanOrEqualTo("time", endDate).greaterThanOrEqualTo("time", startDate).findAll();
        for(int i=0;i<data.size();i++){
            res.add(data.get(i));
        }

        return res;
    }

    public RealmResults<models.Sound> getSoundPendingUpload() {

        return realm.where(models.Sound.class).equalTo("uploaded", 0).findAll();
    }

    public RealmResults<models.Sound> getSoundData() {

        return realm.where(models.Sound.class).findAll();
        //equalTo("trip_id", trip_id).findAll();
    }

    public ArrayList<Sound> getDailySoundData(long startDate, long endDate) {
        ArrayList<Sound> res = new ArrayList<>();
        RealmResults<models.Sound> data = realm.where(models.Sound.class).lessThanOrEqualTo("time", endDate).greaterThanOrEqualTo("time", startDate).findAll();
        for(int i=0;i<data.size();i++){
            res.add(data.get(i));
        }

        return res;
    }

    public RealmResults<models.GeoActivity> getGeoPendingUpload() {

        return realm.where(models.GeoActivity.class).equalTo("uploaded", 0).findAll();
    }



    public List<GeoActivity> getDailyGeoData(long startDate, long endDate) {

        List<GeoActivity> data = realm.where(models.GeoActivity.class).lessThanOrEqualTo("time", endDate).greaterThanOrEqualTo("time", startDate).findAll();


        return data;
    }

    public GeoActivity getLastDailyGeoData(long time) {
        GeoActivity data = new GeoActivity();
        List<GeoActivity> dat = realm.where(models.GeoActivity.class).lessThan("time", time).findAll();
        if(dat.size()>0) {
            data = dat.get(0);
        }
        return data;
    }


    public ArrayList<ScanInformation> getScanInformation() {
        ArrayList<ScanInformation> res = new ArrayList<>();
       //final RealmResults<ScanInformation> scans = realm.where(models.ScanInformation.class).findAll();
        final RealmResults<ScanInformation> receivedData = realm.where(ScanInformation.class).findAll();

        res.addAll(realm.copyFromRealm(receivedData));

        return res;
    }

    public ArrayList<ScanInformation> queryFromDatabase() {
        ArrayList<ScanInformation> listOfScans = new ArrayList<>();

        final RealmResults<ScanInformation> receivedData = realm.where(ScanInformation.class).findAll();

        listOfScans.addAll(realm.copyFromRealm(receivedData));
        return listOfScans;
    }

    public ArrayList<POIProperties> getPOIProperties(){
        ArrayList<POIProperties> listOfPOI = new ArrayList<>();
        RealmResults<POIProperties> pois = realm.where(models.POIProperties.class).findAll();
        listOfPOI.addAll(realm.copyFromRealm(pois));
        return listOfPOI;
    }

    public ArrayList<POIProperties> getPendingPOIProperties(){
        ArrayList<POIProperties> listOfPOI = new ArrayList<>();
        RealmResults<POIProperties> pois = realm.where(models.POIProperties.class).equalTo("uploaded", 0).findAll();
        listOfPOI.addAll(realm.copyFromRealm(pois));
        return listOfPOI;
    }
    public ArrayList<POIVisitInfo> getPOIVisits(){
        ArrayList<POIVisitInfo> listOfVisits = new ArrayList<>();
        RealmResults<POIVisitInfo> pois = realm.where(models.POIVisitInfo.class).findAll();
        listOfVisits.addAll(realm.copyFromRealm(pois));
        return listOfVisits;
    }

    /*public static <T extends RealmObject & IndentifierModel> void updateStatus(Class<T> clazz) {
        T item = realm.where(clazz)

                .findFirst();
    }*/

}
