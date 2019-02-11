package dev.sutd.hdb;

import android.content.SharedPreferences;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;

import static dev.sutd.hdb.FrontEndAdvancedActivity.convertSSLCertificateToCertificate;
import static dev.sutd.hdb.FrontEndAdvancedActivity.getCertificateForRawResource;

public class VisitsActivity extends AppCompatActivity {

    ArrayAdapter adapter;
    ListView visitList;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        Bundle b = getIntent().getExtras();
        long startTime = b.getLong("startTime");
        long endTime = b.getLong("endTime");
        String device_id = b.getString("device_id");

        visitList = (ListView)findViewById(R.id.visitsList);
        ConnectionDetector cd = new ConnectionDetector(this);
        //setContentView(R.layout.web_registration);
        boolean connected;
        try {
            connected = cd.executeTask();

            if(connected)
            {
                setContentView(R.layout.data_plot);
                webView = (WebView)findViewById(R.id.dataWebView);
                webView.setWebViewClient(new WebViewClient());
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setAppCacheEnabled(true);
                webSettings.setDatabaseEnabled(true);
                if (18 < Build.VERSION.SDK_INT ){
                    //18 = JellyBean MR2, KITKAT=19
                    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                }

                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    InputStream caInput = getResources().openRawResource(R.raw.raw_server_ca_bundle); // stored at \app\src\main\res\raw
                    final Certificate certificate = cf.generateCertificate(caInput);
                    caInput.close();

                    Log.e("VISITS", caInput.toString());
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                            Log.e("VISITS", error.toString());
                            SslCertificate sslCertificateServer = error.getCertificate();
                            Certificate pinnedCert = getCertificateForRawResource(R.raw.raw_data_server, VisitsActivity.this);
                            Certificate serverCert = convertSSLCertificateToCertificate(sslCertificateServer);

                            if (pinnedCert.equals(serverCert)) {
                                handler.proceed();
                            } else {
                                super.onReceivedSslError(view, handler, error);
                                Log.e("VISITS", pinnedCert.toString());
                            }
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }


                webView.loadUrl("https://103.24.77.43/nuk_analysis/nuk/layer_one.php?device_id="+device_id+"&start_date="+startTime+"&end_date="+endTime);


                Toast.makeText(this, "Please wait while we load the data", Toast.LENGTH_SHORT).show();

                //fetchData(startTime,endTime);

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




    /*private void fetchData(long startTime, long endTime){

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

    }*/

}
