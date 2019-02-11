package dev.sutd.hdb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.concurrent.ExecutionException;

import RestfulUploads.ApiUrl;

public class WebRegistrationActivity extends AppCompatActivity {

    EditText username, password, rePassword, email, url , birthYear;
    //RadioGroup gender;
    //boolean dataCollectionAcceptance = false;
    static final int REQUEST_READ_PHONE_STATE =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_registration);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        email = (EditText) findViewById(R.id.email);





    }



    public void register(View v) {

                         if (email.getText().toString().isEmpty() || username.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "email and username are mandatory fields!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {

                             /*int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

                             if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                             } else {
                                 //TODO
                             }*/
                            GetDeviceId dev = new GetDeviceId(WebRegistrationActivity.this);
                            String deviceId = dev.getCombinedId();
                            if (deviceId.isEmpty()) {
                                deviceId = username.getText().toString();
                            }


                            String[] userData = {deviceId, username.getText().toString(),  email.getText().toString(),
                                    getDeviceName(), password.getText().toString()};

                                Intent intent = new Intent(WebRegistrationActivity.this, TermsActivity.class);
                                intent.putExtra("registration_array", userData);
                                startActivity(intent);

                        }




    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }*/

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



    public void login (View v){
        Intent reg = new Intent(getApplicationContext(), LoginActivity.class);
        //reg.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(reg);
        finish();

    }
}
