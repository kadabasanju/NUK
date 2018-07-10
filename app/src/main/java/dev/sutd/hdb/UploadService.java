package dev.sutd.hdb;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;
import io.realm.Realm;
import io.realm.RealmResults;
import models.Data;
import models.GeoActivity;
import models.NipunActivity;
import models.Sound;

public class UploadService extends Service {
	
	 
	 final static int maxNumber = 100;

	static String userId;
	static Realm realm;
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

		 //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			 // = sharedpreferences.getString("DEVICE_ID", "DEVICE_ID");
			//	System.out.println("device_id   "+ userId);

		 		//callType = intent.getStringExtra("ServiceCall");
				ConnectionDetector cd = new ConnectionDetector(this);
				boolean connected;
				try {
					connected = cd.executeTask();
				
				if(connected)
				{
					System.out.println("Connected");
					//realm = RealmController.with(MyApp.getInstance()).getRealm();
					uploadData();




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
				} catch (JSONException e) {
					e.printStackTrace();
				}


		 return super.onStartCommand(intent, flags, startId);
     }
     

    	 
     

	public static void  uploadData() throws JSONException, ExecutionException, InterruptedException {

				SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
				userId = sharedpreferences.getString("DEVICE_ID", "DEVICE_ID");
				realm = RealmController.with(MyApp.getInstance()).getRealm();
	    	
	      		//Log.i("success", "uploading");

				//Uploading location data
				RealmResults<Data> results =RealmController.with(MyApp.getInstance()).getPendingUpload();

					int numOfLoops = (results.size()/maxNumber) +1;
					if(results.size()%maxNumber==0){
						numOfLoops -= 1;
					}

	  				for(int j = 0; j<numOfLoops; j++){
						int maxIndex = (j+1)*maxNumber > results.size()?results.size():(j+1)*maxNumber;
						ArrayList<Data>tempAa = new ArrayList();
						JSONObject responseDetailsJson = new JSONObject();
						JSONArray jsonArray = new JSONArray();
						for(int k =j*maxNumber; k<maxIndex; k++) {
							if (results.get(k) == null) {
								break;
							}

							tempAa.add(results.get(k));
							//.out.println(results.get(k).getTime());
						}
						sendHttpPost(tempAa);

					}


				// Uploading geoactivity data
				RealmResults<GeoActivity> geoResults =RealmController.with(MyApp.getInstance()).getGeoPendingUpload();

				numOfLoops = (geoResults.size()/maxNumber) +1;
				if(geoResults.size()%maxNumber==0){
					numOfLoops -= 1;
				}

				for(int j = 0; j<numOfLoops; j++){
					int maxIndex = (j+1)*maxNumber > geoResults.size()?geoResults.size():(j+1)*maxNumber;
					ArrayList<GeoActivity>tempga = new ArrayList<>();

					for(int k =j*maxNumber; k<maxIndex; k++) {
						if (geoResults.get(k) == null) {
							break;
						}

						tempga.add(geoResults.get(k));

					}


					sendHttpGeoPost(tempga);
				}



		// Uploading sound data
		RealmResults<Sound> soundResults =RealmController.with(MyApp.getInstance()).getSoundPendingUpload();

		numOfLoops = (soundResults.size()/maxNumber) +1;
		if(soundResults.size()%maxNumber==0){
			numOfLoops -= 1;
		}

		for(int j = 0; j<numOfLoops; j++){
			int maxIndex = (j+1)*maxNumber > soundResults.size()?soundResults.size():(j+1)*maxNumber;
			ArrayList<Sound>tempsa = new ArrayList();

			for(int k =j*maxNumber; k<maxIndex; k++) {
				if (soundResults.get(k) == null) {
					break;
				}

				tempsa.add(soundResults.get(k));
			}


			sendHttpSoundPost(tempsa);
		}

	  		
 	

	    
	}



	public static void sendHttpPost(final ArrayList<Data>data) throws JSONException {

		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("lat", data.get(i).getLatitude());
				formDetailsJson.put("lng", data.get(i).getLongitude());
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("speed", data.get(i).getSpeed());
				formDetailsJson.put("alt", data.get(i).getAltitude());
				formDetailsJson.put("accu",data.get(i).getAccuracy());
				//formDetailsJson.put("wifiAP", data.get(i).getWifiAPs());
				//Log.i("location "+i, getDateString(data.get(i).getTime()));
				jsonArray.put(formDetailsJson);
			}

		}catch(Exception e){

		}
		//System.out.println(jsonArray.toString());

		final String dataString = jsonArray.toString();
		//System.out.println(dataString);
		String url = ApiUrl.insertPreparedUrl;


		try {
			StringRequest postRequest = new StringRequest(Request.Method.POST, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// response

							int httpRes = Integer.valueOf(response);
							if (httpRes == 1) {
								for (int i = 0; i < data.size(); i++) {
									//System.out.println(tempAa.get(i).getTime());
									Data d = data.get(i);
									if(realm.isInTransaction()){
										realm.commitTransaction();
									}
									realm.beginTransaction();
									d.setUploaded(1);
									realm.commitTransaction();
								}
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							// error
							Log.e("Error.Response", error.toString());
						}
					}
			) {
				@Override
				protected Map<String, String> getParams() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("data", dataString);
					params.put("type", "1");
					System.out.println(params);
					return params;
				}
			};
			//queue.add(postRequest);

			// Adding the request to the queue along with a unique string tag
			MyApp.getInstance().addToRequestQueue(postRequest, "postRequest");
		}catch(NumberFormatException e){
			Log.e("upload", "error from php");
		}

	}



	private static void sendHttpGeoPost(final ArrayList<GeoActivity>data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("gAct", data.get(i).getgAct());
				jsonArray.put(formDetailsJson);
			}

		}catch(Exception e){

		}


		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertPreparedUrl;


		try{
		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response) {
						// response
                        Log.i("act response", response);
						int httpRes = Integer.valueOf(response);
						if(httpRes==1) {
							for (int i = 0; i < data.size(); i++) {
								//System.out.println(tempAa.get(i).getTime());
								if(realm.isInTransaction()){
									realm.commitTransaction();
								}
                                realm.beginTransaction();
                                GeoActivity d = data.get(i);
								d.setUploaded(1);
								realm.commitTransaction();
							}
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error) {
						// error
						//Log.d("Error.Response", error.toString());
					}
				}
		) {
			@Override
			protected Map<String, String> getParams()
			{
				Map<String, String>  params = new HashMap<String, String>();
				params.put("data", dataString);
				params.put("type","2");


				return params;
			}
		};

		// Adding the request to the queue along with a unique string tag
		MyApp.getInstance().addToRequestQueue(postRequest,"postRequest");
		}catch(NumberFormatException e){
			Log.e("upload", "error from php");
		}
	}


	private static void sendHttpSoundPost(final ArrayList<Sound>data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("n_id", data.get(i).getNetwork_id());
				formDetailsJson.put("ip", data.get(i).getIp_address());
				formDetailsJson.put("bat", data.get(i).getBattery());
				formDetailsJson.put("light", data.get(i).getLight());

				jsonArray.put(formDetailsJson);

			}

		}catch(Exception e){

		}


		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertPreparedUrl;


		try {
			StringRequest postRequest = new StringRequest(Request.Method.POST, url,
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// response
                            Log.i("misc response", response);
							int httpRes = Integer.valueOf(response);
							if (httpRes == 1) {
								for (int i = 0; i < data.size(); i++) {
									//System.out.println(tempAa.get(i).getTime());
									if(realm.isInTransaction()){
										realm.commitTransaction();
									}
									Sound d = data.get(i);
									if(realm.isInTransaction()){
										realm.commitTransaction();
									}
									realm.beginTransaction();
									d.setUploaded(1);
									realm.commitTransaction();
								}
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							// error
							//Log.d("Error.Response", error.toString());
						}
					}
			) {
				@Override
				protected Map<String, String> getParams() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("data", dataString);
					params.put("type", "4");


					return params;
				}
			};

			// Adding the request to the queue along with a unique string tag
			MyApp.getInstance().addToRequestQueue(postRequest, "postRequest");
		}catch(NumberFormatException e){
			Log.e("upload", "error from php");
		}
	}


	/*public static byte[] compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes());
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}*/

 /*private JSONArray prepareData(ArrayList<Data>data){
	//String result ="";
	 Map<String,Map<String,Map<String,Data>>> map = new HashMap<>();
	 for(int i=0;i<data.size();i++) {
		 String[] date_chunk = getDateTimeChunks(data.get(i).getTime());
		 // checks if the date is present
	 	 if(map.containsKey(date_chunk[0])){
			 Map<String,Map<String,Data>> map_min = map.get(date_chunk[0]);
			 //checks if the minute is present
			 if(map_min.containsKey(date_chunk[1])){
				 Map<String,Data> map_sec = map_min.get(date_chunk[1]);
				 //checks if the second is present
				 if(map_sec.containsKey(date_chunk[2])){
					//do nothing
				 }
				 //
				 else{
					 //add the data.
					 map_sec.put(date_chunk[2],data.get(i));
				 }
			 }
			 //if map does not contain minute information, add it
			 else{
				 Map<String,Data> map_sec = new HashMap<>();
				 map_sec.put(date_chunk[2],data.get(i));
				 map_min.put(date_chunk[1],map_sec);
			 }

		 }
		 //if map does not contain hour information, add it
		 else{
			 Map<String,Map<String,Data>> map_min = new HashMap<>();
			 Map<String,Data> map_sec = new HashMap<>();
			 map_sec.put(date_chunk[2],data.get(i));
			 map_min.put(date_chunk[1],map_sec);
			 map.put(date_chunk[0],map_min);
		 }
	 }

	 System.out.println(map.toString());
	 Object[] mapkeys = map.keySet().toArray();
	 JSONArray jsonArray = new JSONArray();
	 for(int i=0;i<mapkeys.length;i++){
		 try {
			 JSONObject formDetailsJson = new JSONObject();
			 formDetailsJson.put("deviceId", userId);
			 formDetailsJson.put("time", mapkeys[i]);

			 JSONObject min_data = new JSONObject();
			 //keys for min map
			 Object[] minkeys = map.get(mapkeys[i]).keySet().toArray();
			 for(int j=0;j<minkeys.length;j++){

				 Object[]seckeys = map.get(mapkeys[i]).get(minkeys[j]).keySet().toArray();
				 JSONObject sec_data = new JSONObject();
				 //keys for sec map
				 for(int k=0;k<seckeys.length;k++){
					 JSONObject d = new JSONObject();
					 Data dat = map.get(mapkeys[i]).get(minkeys[j]).get(seckeys[k]);
					 d.put("latitude", dat.getLatitude());
					 d.put("longitude", dat.getLongitude());
					 d.put("speed", dat.getSpeed());
					 d.put("altitude", dat.getAltitude());
					 d.put("accuracy", dat.getAccuracy());
					 d.put("wifiAP", dat.getWifiAPs());
					 sec_data.put(seckeys[k].toString(),d);
				 }
				 min_data.put(minkeys[j].toString(),sec_data);

			 }

			 formDetailsJson.put("min",min_data);
			 jsonArray.put(formDetailsJson);
		 }catch (JSONException je){

		 }


	 }
	 //result = jsonArray.toString();

	 return jsonArray;
 }

private String[] getDateTimeChunks(long time){
	String[] res = new String[3];
	SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd HH");

	Date d = new Date(time);
	String    strTime    =    formatter.format(d);

	SimpleDateFormat formatter1    =   new    SimpleDateFormat    ("mm");
	String    min    =    formatter1.format(d);

	SimpleDateFormat formatter2    =   new    SimpleDateFormat    ("ss");
	String    sec    =    formatter2.format(d);

	res[0]=strTime;
	res[1]=min;
	res[2]=sec;
	return res;
}
*/



}
