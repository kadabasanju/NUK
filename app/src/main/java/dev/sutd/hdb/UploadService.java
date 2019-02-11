package dev.sutd.hdb;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import models.Data;
import models.GeoActivity;
import models.ScanInformation;
import models.ScanObject;
import models.Sound;

public class UploadService extends Service {

	static String userId;
	static Realm realm;
	static RealmResults<Data> locData;
	static RealmResults<GeoActivity> geoResults;
	static RealmResults<Sound> soundResults;
	static ConnectionDetector cd;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	 public void onCreate(){

	        super.onCreate();


	        
	    }
	 
	 @Override
     public void onDestroy() {
            // TODO Auto-generated method stub
            
            super.onDestroy();
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub

		  cd = new ConnectionDetector(this );
		 uploadData();

		 return super.onStartCommand(intent, flags, startId);
     }
     

    	public static void uploadData(){

			boolean connected;
			try {
				ConnectionDetector cd = new ConnectionDetector(MyApp.getInstance());
				connected = cd.executeTask();

				if(connected)
				{
					System.out.println("Connected");
					uploadAllData();
				}
				else {
					System.out.println("Not Connected");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
     

	public static void  uploadAllData()   {

				SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
				userId = sharedpreferences.getString("DEVICE_ID", "DEVICE_ID");
				realm = RealmController.with(MyApp.getInstance()).getRealm();

				locData =RealmController.with(MyApp.getInstance()).getPendingUpload();
				geoResults =RealmController.with(MyApp.getInstance()).getGeoPendingUpload();
				soundResults =RealmController.with(MyApp.getInstance()).getSoundPendingUpload();
				ArrayList<ScanInformation> wifiData= queryWifiFromDatabase();
				try{
					JSONObject jsonObj = new JSONObject();
					JSONArray locJsonArray = new JSONArray();
					for(int i = 0; i < locData.size(); i++){
						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("lat", locData.get(i).getLatitude());
						formDetailsJson.put("lng", locData.get(i).getLongitude());
						formDetailsJson.put("time", locData.get(i).getTime());
						formDetailsJson.put("speed", locData.get(i).getSpeed());
						formDetailsJson.put("alt", locData.get(i).getAltitude());
						formDetailsJson.put("accu",locData.get(i).getAccuracy());

						locJsonArray.put(formDetailsJson);
					}

					JSONArray geoArray = new JSONArray();
					for(int i = 0; i < geoResults.size(); i++){
						JSONObject formDetailsJson = new JSONObject();

						formDetailsJson.put("time", geoResults.get(i).getTime());
						formDetailsJson.put("act", geoResults.get(i).getgAct());
						geoArray.put(formDetailsJson);
					}

					JSONArray miscArray = new JSONArray();
					for(int i = 0; i < soundResults.size(); i++){
						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("time", soundResults.get(i).getTime());
						formDetailsJson.put("n_id", soundResults.get(i).getNetwork_id());
						formDetailsJson.put("ip", soundResults.get(i).getIp_address());
						formDetailsJson.put("bat", soundResults.get(i).getBattery());
						formDetailsJson.put("light", soundResults.get(i).getLight());
						miscArray.put(formDetailsJson);
					}
					JSONArray wifiJsonArray = new JSONArray();
					for(int i = 0; i < wifiData.size(); i++) {
						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("time", wifiData.get(i).getTimestamp());
						JSONArray array = new JSONArray();
						RealmList<ScanObject> scanArr = wifiData.get(i).getScanObjectList();
						for (int j = 0; j < scanArr.size(); j++) {
							JSONObject obj = new JSONObject();
							ScanObject scanObj = scanArr.get(j);
							obj.put("RSSI", scanObj.getRSSI());
							obj.put("MAC", scanObj.getBSSID());
							array.put(obj);
						}
						formDetailsJson.put("scans", array);

						wifiJsonArray.put(formDetailsJson);
					}

					jsonObj.put("device_id",userId);
					jsonObj.put("wifi",wifiJsonArray);
					jsonObj.put("locations",locJsonArray);
					jsonObj.put("act",geoArray);
					jsonObj.put("misc",miscArray);
					System.out.println(jsonObj.toString());
					UploadAsyncClass up = new UploadAsyncClass();
					up.execute(jsonObj.toString());

				}catch(Exception e){

				}

	}

	//query from database
	public static ArrayList<ScanInformation> queryWifiFromDatabase() {
		ArrayList<ScanInformation> listOfScans = new ArrayList<>();

		final RealmResults<ScanInformation> receivedData = realm.where(ScanInformation.class).findAll();

		listOfScans.addAll(realm.copyFromRealm(receivedData));
		return listOfScans;
	}


	public static void deleteAllAfterClustering(){
		realm.executeTransaction(new Realm.Transaction() {
									 @Override
									 public void execute(Realm realm) {
										 RealmResults<ScanInformation> result = realm.where(ScanInformation.class).findAll(); // delete records up to end of yesterday
										 result.deleteAllFromRealm();
									 }
								 }
		);
		Log.i("Upload service","Deleted Scan Info from the database");
	}
	public static void deleteAllAfterUploading(){
		realm.executeTransaction(new Realm.Transaction() {
									 @Override
									 public void execute(Realm realm) {
										 RealmResults<Data> result = realm.where(Data.class).findAll(); // delete records up to end of yesterday
										 result.deleteAllFromRealm();
									 }
								 }
		);
		realm.executeTransaction(new Realm.Transaction() {
									 @Override
									 public void execute(Realm realm) {
										 RealmResults<GeoActivity> result = realm.where(GeoActivity.class).findAll(); // delete records up to end of yesterday
										 result.deleteAllFromRealm();
									 }
								 }
		);
		realm.executeTransaction(new Realm.Transaction() {
									 @Override
									 public void execute(Realm realm) {
										 RealmResults<Sound> result = realm.where(Sound.class).findAll(); // delete records up to end of yesterday
										 result.deleteAllFromRealm();
									 }
								 }
		);

		Log.i("Upload service","Deleted all Info from the database");
	}



	public static class UploadAsyncClass extends AsyncTask<String, String, String> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			// implement API in background and store the response in current variable
			String current = "0";
			try{
                // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
                InputStream caInput = MyApp.getInstance().getResources().openRawResource(R.raw.raw_data_server);//new BufferedInputStream(new FileInputStream("raw_server_ca_bundle.crt"));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

// Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);




                String body = params[0];
				URL url = new URL(ApiUrl.insertCompressedData);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setSSLSocketFactory(context.getSocketFactory());
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-encoding", "gzip");
				conn.setRequestProperty("Content-type", "application/octet-stream");
				GZIPOutputStream dos = new GZIPOutputStream(conn.getOutputStream());
				dos.write(body.getBytes());
				dos.flush();
				dos.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder builder = new StringBuilder();

				String decodedString = "";
				while ((decodedString = in.readLine()) != null) {
					builder.append(decodedString);
				}
				in.close();
				Log.e("Upload", builder.toString());
				current = builder.toString();
				return current;


			} catch (Exception e) {
				e.printStackTrace();
			}
			return current;
		}

		@Override
		protected void onPostExecute(String s) {

			if(Integer.parseInt(s)==4){
				deleteAllAfterClustering();
				deleteAllAfterUploading();
				/*for (int i = 0; i < locData.size(); i++) {
					//System.out.println(tempAa.get(i).getTime());
					if(realm.isInTransaction()){
						realm.commitTransaction();
					}
					realm.beginTransaction();
					Data d = locData.get(i);
					d.deleteFromRealm();
					realm.commitTransaction();
				}
				for (int i = 0; i < geoResults.size(); i++) {
					//System.out.println(tempAa.get(i).getTime());
					if(realm.isInTransaction()){
						realm.commitTransaction();
					}
					realm.beginTransaction();
					GeoActivity d = geoResults.get(i);
					d.deleteFromRealm();
					realm.commitTransaction();
				}
				for (int i = 0; i < soundResults.size(); i++) {
					//System.out.println(tempAa.get(i).getTime());
					if(realm.isInTransaction()){
						realm.commitTransaction();
					}
					realm.beginTransaction();
					Sound d = soundResults.get(i);
					d.deleteFromRealm();
					realm.commitTransaction();
				}*/


			}

		}

	}

}
