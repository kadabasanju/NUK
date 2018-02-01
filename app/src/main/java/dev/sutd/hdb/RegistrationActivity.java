package dev.sutd.hdb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import models.Data;

public class RegistrationActivity extends Activity {
	private EditText name,phone, age, email;

	private RadioGroup gender;
	private RadioButton radio; 

	SharedPreferences sharedpreferences;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		name = (EditText)findViewById(R.id.name);
		phone = (EditText)findViewById(R.id.phone);

		gender = (RadioGroup)findViewById(R.id.radioGender);
		age = (EditText)findViewById(R.id.age);
		email = (EditText)findViewById(R.id.email);


		

	}

	

	
		public String getDeviceName() {
			  String manufacturer = Build.MANUFACTURER;
			  String model = Build.MODEL;
			  if (model.startsWith(manufacturer)) {
			    return capitalize(model);
			  } else {
			    return capitalize(manufacturer) + " " + model;
			  }
			}


			private String capitalize(String s) {
			  if (s == null || s.length() == 0) {
			    return "";
			  }
			  char first = s.charAt(0);
			  if (Character.isUpperCase(first)) {
			    return s;
			  } else {
			    return Character.toUpperCase(first) + s.substring(1);
			  }
			} 
	
	
	public void register(View v) throws InterruptedException, ExecutionException{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to submit?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						int selectedId = gender.getCheckedRadioButtonId();
						radio = (RadioButton) findViewById(selectedId);
						String genderText = radio.getText().toString();
						if (name.getText().toString().isEmpty() || phone.getText().toString().isEmpty() || age.getText().toString().isEmpty()
								) {
							Toast.makeText(getApplicationContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
							return;
						} else {


							GetDeviceId dev = new GetDeviceId(RegistrationActivity.this);
							String deviceId = dev.getCombinedId();
							if (deviceId.isEmpty()) {
								deviceId = phone.getText().toString();
							}


							//db.addRegistration(1, name.getText().toString(), phone.getText().toString(), deviceId);
							String[] userData = {deviceId, name.getText().toString(), phone.getText().toString(), age.getText().toString(), email.getText().toString(),
									genderText, getDeviceName()};


							try {
								sendHttpPost(userData);
								if (httpRes == 1) {

									sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
									Editor editor = sharedpreferences.edit();
									editor.putString("DEVICE_ID", deviceId);

									editor.commit();

									Intent cluster = new Intent(getApplicationContext(), MainActivity.class);
									cluster.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(cluster);
									finish();
								} else {

									Toast.makeText(getApplicationContext(), "Error Registering! Please try again. Make sure you have internet access", Toast.LENGTH_SHORT).show();
									return;

								}
							} catch (JSONException e) {
								e.printStackTrace();
							}


						}
					}

				});
        builder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();

					}
				});
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
     
	}

int httpRes =0;
	private void sendHttpPost(String[] data) throws JSONException {



		JSONArray jsonArray = new JSONArray();
		try{

				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("deviceId", data[0]);
				formDetailsJson.put("name", data[1]);
				formDetailsJson.put("phone", data[2]);
				formDetailsJson.put("age", data[3]);
				formDetailsJson.put("email", data[4]);
				formDetailsJson.put("gender", data[5]);
				formDetailsJson.put("device_name",data[6]);
				jsonArray.put(formDetailsJson);



		}catch(Exception e){

		}
		System.out.println(jsonArray.toString());

		final String dataString = jsonArray.toString();

		String url = ApiUrl.insertUserUrl;



		StringRequest postRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>()
				{
					@Override
					public void onResponse(String response) {
						// response
						httpRes = Integer.valueOf(response);
						Log.d("Response", response);
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error) {
						// error
						Log.d("Error.Response", error.toString());
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
		MyApp.getInstance().addToRequestQueue(postRequest, "postRequest");

	}
}



