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

public class UploadService extends Service {
	
	 
	 final int maxNumber = 1000;
	 SharedPreferences sharedPref;
	 String device_id;
	 String ip;
	String callType="";
	String userId;

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

				Realm realm = RealmController.with(MyApp.getInstance()).getRealm();
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
						}

						//}
						int result = sendHttpPost(tempAa);

						// Once the data is uploaded to server, just update the uploaded value on the database!
						if(result==1){
							System.out.println("Updating the uploaded!! Number of records:" + tempAa.size());
							for(int i=0; i<tempAa.size();i++) {
								System.out.println(tempAa.get(i).getTime());
								Data d = tempAa.get(i);
								realm.beginTransaction();
								d.setUploaded(1);
								realm.commitTransaction();
							}
						}


					}
	  		
 	

	    
	}


	int httpRes = 0;
private int sendHttpPost(final ArrayList<Data>data) throws JSONException {



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
			formDetailsJson.put("activity",data.get(i).getActivity());
			formDetailsJson.put("network_id", data.get(i).getNetwork_id());
			formDetailsJson.put("ip_address", data.get(i).getIp_address());
			formDetailsJson.put("battery", data.get(i).getBattery());
			formDetailsJson.put("light", data.get(i).getLight());
			formDetailsJson.put("wifiAP", data.get(i).getWifiAPs());
			formDetailsJson.put("gAct", data.get(i).getgAct());
			formDetailsJson.put("socioAct", data.get(i).getSocio_activity());
			formDetailsJson.put("decibel", data.get(i).getDecibel());

			jsonArray.put(formDetailsJson);

		}

	}catch(Exception e){

	}
	System.out.println(jsonArray.toString());

	final String dataString = jsonArray.toString();

	String url = ApiUrl.insertDataUrl;



	StringRequest postRequest = new StringRequest(Request.Method.POST, url,
			new Response.Listener<String>()
			{
				@Override
				public void onResponse(String response) {
					// response
					httpRes = Integer.valueOf(response);
					//Log.d("Response", response);
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
return httpRes;
}







}
