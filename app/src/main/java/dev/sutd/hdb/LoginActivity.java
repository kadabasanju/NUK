package dev.sutd.hdb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import RestfulUploads.ApiUrl;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_login);

         username = (EditText)findViewById(R.id.username);
         password = (EditText) findViewById(R.id.password);



    }

    private void sendHttpPostNuk(final String[] data) throws JSONException {

        String url = ApiUrl.serverURL+"/applogin";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(getApplicationContext(), "Error Login! Please try again. Make sure you have internet access", Toast.LENGTH_SHORT).show();
                        Log.d("Error.Response", error.toString());
                        return;

                    }
                }

        ) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response)  {
                int mStatusCode = response.statusCode;

                    if (mStatusCode == 200) {
                        GetDeviceId dev = new GetDeviceId(LoginActivity.this);
                        String deviceId = dev.getCombinedId();
                        if (deviceId.isEmpty()) {
                            deviceId = data[0].toString();
                        }
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("DEVICE_ID", deviceId);
                        editor.putString("USERNAME", data[0]);
                        editor.putString("PASSWORD", data[1]);

                        editor.commit();

                        Intent cluster = new Intent(getApplicationContext(), MainActivityFrontend.class);
                        //Intent cluster = new Intent(getApplicationContext(), MainActivity.class);
                        cluster.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cluster);
                        finish();

                    }


                return super.parseNetworkResponse(response);
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  formDetailsJson = new HashMap<String, String>();

                formDetailsJson.put("username", data[0]);
                formDetailsJson.put("password",data[1]);


                return formDetailsJson;
            }
        };


// Adding the request to the queue along with a unique string tag
        MyApp.getInstance().addToRequestQueueHttps(postRequest, "postRequest");



    }

    private String convertData(byte[] data) {
        String strResult;
        try {
            strResult = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var4) {
            strResult = new String(data);
        }

        return strResult;
    }

    public void login(View v){

            if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Username and password cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    final String[] data = new String[]{username.getText().toString(), password.getText().toString()};
                    sendHttpPostNuk(data);
                } catch(JSONException e){
                    e.printStackTrace();
                }
        }
    }
    public void register(View v){
        Intent reg = new Intent(getApplicationContext(), WebRegistrationActivity.class);
        reg.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(reg);

    }

    public void forgotPassword(View v){
        Intent frontendIntent = new Intent(getApplicationContext(), FrontEndAdvancedActivity.class);
        frontendIntent.putExtra("PAGENAME","FORGOT");
        frontendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(frontendIntent);
    }
}
