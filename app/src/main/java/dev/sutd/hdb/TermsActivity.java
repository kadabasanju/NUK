package dev.sutd.hdb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import RestfulUploads.ApiUrl;

public class TermsActivity extends AppCompatActivity {
    RadioGroup radioAccept;
    String[] registration_array;
    String accept = "true";
    boolean accBool = true;
    Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_terms);
        radioAccept = (RadioGroup)findViewById(R.id.radioAccept);
        Intent intent = getIntent();
        registration_array = intent.getStringArrayExtra("registration_array");
        mButton = findViewById(R.id.btn);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });

    }

    public void updateUser(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to submit?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        int selectedId = radioAccept.getCheckedRadioButtonId();
                        RadioButton radio = (RadioButton) findViewById(selectedId);
                        String radio_text = radio.getText().toString();

                        if (radio_text.equals("Opt out of research")) {
                            accept = "false";
                            accBool = false;
                        }

                        try {
                            sendHttpPostNuk(registration_array);
                        } catch (JSONException e) {
                            e.printStackTrace();
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


    private void sendHttpPostNuk(final String[] data) throws JSONException {
        final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = ApiUrl.serverURL+"/appregister";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("USER_ID", response);
                        editor.commit();
                        //Log.e("Register Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //String errorMsg = "Error Registering! Please try again. Make sure you have internet access";
                        String errorMsg = "This Nickname or email id has already been registered. Please pick another one or go to forgot password!";
                        // error
                        Toast.makeText(getApplicationContext(),errorMsg , Toast.LENGTH_SHORT).show();
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
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;

                if(mStatusCode==200){


                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("DEVICE_ID", data[0]);
                    editor.putString("USERNAME", data[1]);
                    editor.putString("PASSWORD", data[4]);
                    editor.putBoolean("DATA_CONSENT", accBool);
                    editor.putBoolean("AUTO_START", false);

                    editor.commit();

                    Intent mainIntent = new Intent(getApplicationContext(), MainActivityFrontend.class);
                    //Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);

                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    ActivityCompat.finishAffinity(TermsActivity.this);


                }
                return super.parseNetworkResponse(response);
            }
            @Override
            protected Map<String, String> getParams()
            {
                Log.e("acceptance", accept);
                Map<String, String>  formDetailsJson = new HashMap<String, String>();
                formDetailsJson.put("deviceId", data[0]);
                formDetailsJson.put("username", data[1]);
                //formDetailsJson.put("birth_year", data[2]);
                formDetailsJson.put("email", data[2]);
                //formDetailsJson.put("gender", data[4]);
                formDetailsJson.put("deviceName",data[3]);
                formDetailsJson.put("password",data[4]);
                formDetailsJson.put("acceptance",accept);


                return formDetailsJson;
            }
        };


// Adding the request to the queue along with a unique string tag
        MyApp.getInstance().addToRequestQueueHttps(postRequest, "postRequest");



    }

}
