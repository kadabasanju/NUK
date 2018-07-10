package dev.sutd.hdb;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;

public class VisitsActivity extends AppCompatActivity {

    ArrayAdapter adapter;
    ListView visitList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visits);
        Bundle b = getIntent().getExtras();
        long startTime = b.getLong("startTime");
        long endTime = b.getLong("endTime");

        visitList = (ListView)findViewById(R.id.visitsList);
        ConnectionDetector cd = new ConnectionDetector(this);
        boolean connected;
        try {
            connected = cd.executeTask();

            if(connected)
            {
                Toast.makeText(this, "Please wait while we load the data", Toast.LENGTH_SHORT).show();

                fetchData(startTime,endTime);

            }
            else {
                Toast.makeText(this, "There seems to be no internet! Please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }


    private void fetchData(long startTime, long endTime){

        //System.out.println(jsonArray.toString());
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = ApiUrl.fetchVisitsUrl+"?start_date="+startTime+"&end_date="+endTime+"&device_id="+sharedpreferences.getString("DEVICE_ID","");


        try {
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject obj = new JSONObject(response);

                                if(obj.getString("success").equals("1")){
                                    JSONArray arr = obj.getJSONArray("returned_values");
                                    System.out.println("success = "+arr.length());
                                    if(arr.length()>0) {
                                        String[] strArr = new String[arr.length()];
                                        for(int i =0; i<arr.length();i++){
                                            strArr[i]=arr.optString(i);
                                        }

                                        //System.out.println(listItems.toString());
                                        adapter = new ArrayAdapter(VisitsActivity.this, android.R.layout.simple_list_item_1, strArr);
                                        visitList.setAdapter(adapter);

                                    }
                                    else{
                                        Toast.makeText(VisitsActivity.this, "No records for the day!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else{
                                    Toast.makeText(VisitsActivity.this, "There was some problem fetching data!", Toast.LENGTH_SHORT).show();
                                }
                            }catch(JSONException je){
                                je.printStackTrace();
                            }
                            //System.out.println(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.e("Error.Response", error.toString());
                        }
                    }
            ) ;
            MyApp.getInstance().addToRequestQueue(getRequest, "getRequest");
        }catch(NumberFormatException e){
            Log.e("upload", "error from php");
        }

    }

}
