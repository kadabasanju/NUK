package dev.sutd.hdb;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;
import io.realm.Realm;
import io.realm.RealmResults;
import models.Data;
import models.GeoActivity;
import models.Sound;

public class UploadService extends Service {
	
	 
	 final int maxNumber = 10000;
	 SharedPreferences sharedPref;
	 String device_id;
	 String ip;
	String callType="";
	String userId;
	Realm realm;
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

		 SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			 userId = sharedpreferences.getString("DEVICE_ID", "DEVICE_ID");


		 		//callType = intent.getStringExtra("ServiceCall");
				ConnectionDetector cd = new ConnectionDetector(this);
				boolean connected;
				try {
					connected = cd.executeTask();
				
				if(connected)
				{
					System.out.println("Connected");
					realm = RealmController.with(MyApp.getInstance()).getRealm();
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
     

    	 
     

	public void uploadData() throws JSONException, ExecutionException, InterruptedException {
		


	    	
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
							System.out.println(results.get(k).getTime());
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
					ArrayList<GeoActivity>tempga = new ArrayList();

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



	private void sendHttpPost(final ArrayList<Data>data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("latitude", data.get(i).getLatitude());
				formDetailsJson.put("longitude", data.get(i).getLongitude());
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("speed", data.get(i).getSpeed());
				formDetailsJson.put("altitude", data.get(i).getAltitude());
				formDetailsJson.put("accuracy",data.get(i).getAccuracy());
				formDetailsJson.put("wifiAP", data.get(i).getWifiAPs());
				jsonArray.put(formDetailsJson);

			}

		}catch(Exception e){

		}
		//System.out.println(jsonArray.toString());

		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertDataUrl;



		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response) {
						// response
						int httpRes = Integer.valueOf(response);
						if(httpRes==1) {
							for (int i = 0; i < data.size(); i++) {
								//System.out.println(tempAa.get(i).getTime());
								Data d = data.get(i);
								realm.beginTransaction();
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


				return params;
			}
		};
		//queue.add(postRequest);

	// Adding the request to the queue along with a unique string tag
		MyApp.getInstance().addToRequestQueue(postRequest,"postRequest");

	}

	private void sendHttpGeoPost(final ArrayList<GeoActivity>data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("activity",data.get(i).getnAct());
				formDetailsJson.put("gAct", data.get(i).getgAct());
				jsonArray.put(formDetailsJson);

			}

		}catch(Exception e){

		}


		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertDataUrl;



		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response) {
						// response
						int httpRes = Integer.valueOf(response);
						if(httpRes==1) {
							for (int i = 0; i < data.size(); i++) {
								//System.out.println(tempAa.get(i).getTime());
								GeoActivity d = data.get(i);
								realm.beginTransaction();
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


				return params;
			}
		};

		// Adding the request to the queue along with a unique string tag
		MyApp.getInstance().addToRequestQueue(postRequest,"postRequest");

	}

	private void sendHttpSoundPost(final ArrayList<Sound>data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{
			for(int i = 0; i < data.size(); i++){
				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", userId);
				formDetailsJson.put("time", data.get(i).getTime());
				formDetailsJson.put("network_id", data.get(i).getNetwork_id());
				formDetailsJson.put("ip_address", data.get(i).getIp_address());
				formDetailsJson.put("battery", data.get(i).getBattery());
				formDetailsJson.put("light", data.get(i).getLight());
				formDetailsJson.put("socioAct", data.get(i).getSocio_activity());
				formDetailsJson.put("decibel", data.get(i).getDecibel());
				jsonArray.put(formDetailsJson);

			}

		}catch(Exception e){

		}


		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertDataUrl;



		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response) {
						// response
						int httpRes = Integer.valueOf(response);
						if(httpRes==1) {
							for (int i = 0; i < data.size(); i++) {
								//System.out.println(tempAa.get(i).getTime());
								Sound d = data.get(i);
								realm.beginTransaction();
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


				return params;
			}
		};

		// Adding the request to the queue along with a unique string tag
		MyApp.getInstance().addToRequestQueue(postRequest,"postRequest");

	}







}
