package dev.sutd.hdb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mklimek.sslutilsandroid.SslUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import RestfulUploads.ApiUrl;
import RestfulUploads.ConnectionDetector;
import im.delight.android.webview.AdvancedWebView;

public class FrontEndAdvancedActivity extends AppCompatActivity implements AdvancedWebView.Listener {


    SharedPreferences sharedPreferences;
        private AdvancedWebView mWebView;
        static final int REQUEST_ACCESS_GEO_LOCATION = 100;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ConnectionDetector cd = new ConnectionDetector(this);
            boolean connected;
            Bundle extras = getIntent().getExtras();
            String msg = extras.getString("PAGENAME");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isRegistered = sharedPreferences.contains("USER_ID");
            boolean dataConsent = sharedPreferences.getBoolean("DATA_CONSENT",true);
            if(dataConsent){
                MyUtils.startPowerSaverIntent(FrontEndAdvancedActivity.this);
            }
            try {

                connected = cd.executeTask();

                if (connected) {

                    checkForLocationPermission();
                    requestWindowFeature(Window.FEATURE_NO_TITLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    setContentView(R.layout.activity_front_end_advanced);

                    mWebView = (AdvancedWebView) findViewById(R.id.advancedWebview);
                    mWebView.setListener(this, this);

                    WebSettings webSettings = mWebView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    //webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                    webSettings.setDomStorageEnabled(true);
                    //webSettings.setAppCacheEnabled(true);
                    //webSettings.setDatabaseEnabled(true);
                    webSettings.setGeolocationEnabled(true);
                    //webSettings.setUseWideViewPort(true);
                    ////webSettings.getSaveFormData();
                    webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
                    //webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    //webSettings.setEnableSmoothTransition(true);
                    //mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
                    if (18 < Build.VERSION.SDK_INT ){
                        //18 = JellyBean MR2, KITKAT=19
                        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                    }




                       try{
                            // Get cert from raw resource...
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            InputStream caInput = getResources().openRawResource(R.raw.ca); // stored at \app\src\main\res\raw
                            final Certificate certificate = cf.generateCertificate(caInput);
                            caInput.close();

                           mWebView.setWebChromeClient(new WebChromeClient(){
                                                          public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {

                                                              callback.invoke(origin, true, false);
                                                          }
                                                      });
                            mWebView.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                                    Log.e(TAG, error.toString());
                                    SslCertificate sslCertificateServer = error.getCertificate();
                                    Certificate pinnedCert = getCertificateForRawResource(R.raw.nukampung_sutd_edu_sg, FrontEndAdvancedActivity.this);
                                    Certificate serverCert = convertSSLCertificateToCertificate(sslCertificateServer);

                                    if(pinnedCert.equals(serverCert)) {
                                        handler.proceed();
                                    } else {
                                        super.onReceivedSslError(view, handler, error);
                                        Log.e(TAG, pinnedCert.toString());
                                    }
                                }
                            });



                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if(msg.equals("LOGIN")) {
                        if(!isRegistered) {
                            final String postData = "username=" + URLEncoder.encode(sharedPreferences.getString("USERNAME", ""), "UTF-8") + "&password=" + URLEncoder.encode(sharedPreferences.getString("PASSWORD", ""), "UTF-8");
                            mWebView.postUrl(ApiUrl.serverURL + "/login", postData.getBytes());

                        }
                        else{
                            final String postData = "username=" + URLEncoder.encode(sharedPreferences.getString("USERNAME", ""), "UTF-8") + "&password=" + URLEncoder.encode(sharedPreferences.getString("PASSWORD", ""), "UTF-8");
                            mWebView.postUrl(ApiUrl.serverURL + "/registerInterest", postData.getBytes());
                            //String user_id = sharedPreferences.getString("USER_ID","");
                            //mWebView.loadUrl(ApiUrl.serverURL + "/registerInterest/"+user_id);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("USER_ID");
                            editor.commit();
                        }


                        Toast.makeText(this, "Please wait while we load the data", Toast.LENGTH_SHORT).show();
                        final String firebaseId = sharedPreferences.getString("FIREBASE_ID","");
                        if(firebaseId.length()>0) {
                            sendHttpPostNuk();
                        }

                    }

                    else if(msg.equals("FORGOT")){
                        mWebView.loadUrl(ApiUrl.serverURL + "/forgot");

                        //mWebView.setWebViewClient(new WebViewClient());

                    }
                } else {

                    Intent errorIntent = new Intent(FrontEndAdvancedActivity.this, ErrorActivity.class);
                    startActivity(errorIntent);
                    finish();
                    Toast.makeText(this, "There seems to be no internet! Please try again", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // ...
        }

        @SuppressLint("NewApi")
        @Override
        protected void onResume() {
            super.onResume();
            //mWebView.onResume();
            // ...
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPause() {
            //mWebView.onPause();
            // ...
            super.onPause();
        }

        @Override
        protected void onDestroy() {
           // mWebView.onDestroy();
            // ...
            super.onDestroy();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
            super.onActivityResult(requestCode, resultCode, intent);
            mWebView.onActivityResult(requestCode, resultCode, intent);
            // ...
        }

        @Override
        public void onBackPressed() {
            if (!mWebView.onBackPressed()) { return; }
            // ...
            super.onBackPressed();
        }

        @Override
        public void onPageStarted(String url, Bitmap favicon) { }

        @Override
        public void onPageFinished(String url) { }

        @Override
        public void onPageError(int errorCode, String description, String failingUrl) {
            Intent errorIntent = new Intent(FrontEndAdvancedActivity.this, ErrorActivity.class);
            startActivity(errorIntent);
            finish();

        }


        @Override
        public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

        @Override
        public void onExternalPageRequest(String url) { }

    private void sendHttpPostNuk( ) {
        final String firebaseId = sharedPreferences.getString("FIREBASE_ID","");
        final String username = sharedPreferences.getString("USERNAME", "");
        final String deviceId = sharedPreferences.getString("DEVICE_ID", "");
        String url = ApiUrl.serverURL+"/updatefid";
        final String deviceName = getDeviceName();

        StringRequest patchRequest = new StringRequest(Request.Method.PATCH, url,
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
                        Toast.makeText(getApplicationContext(), "Error! Please try again. Make sure you have internet access", Toast.LENGTH_SHORT).show();
                        Log.e("Error.Response", error.toString());
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

                }
                return super.parseNetworkResponse(response);
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  formDetailsJson = new HashMap<String, String>();

                formDetailsJson.put("username", username);
                formDetailsJson.put("firebaseid",firebaseId);
                formDetailsJson.put("deviceid",deviceId);
                formDetailsJson.put("devicename",deviceName);



                return formDetailsJson;
            }
        };
        //queue.add(postRequest);

// Adding the request to the queue along with a unique string tag
        MyApp.getInstance().addToRequestQueueHttps(patchRequest, "postRequest");



    }



   static String TAG="WebView";
   public static Certificate getCertificateForRawResource(int resourceId, Context context) {
       CertificateFactory cf = null;
       Certificate ca = null;
       Resources resources = context.getResources();
       InputStream caInput = resources.openRawResource(resourceId);

       try {
           cf = CertificateFactory.getInstance("X.509");
           ca = cf.generateCertificate(caInput);
       } catch (CertificateException e) {
           Log.e(TAG, "exception", e);
       } finally {
           try {
               caInput.close();
           } catch (IOException e) {
               Log.e(TAG, "exception", e);
           }
       }

       return ca;
   }

    public static Certificate convertSSLCertificateToCertificate(SslCertificate sslCertificate) {
        CertificateFactory cf = null;
        Certificate certificate = null;
        Bundle bundle = sslCertificate.saveState(sslCertificate);
        byte[] bytes = bundle.getByteArray("x509-certificate");

        if (bytes != null) {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
                certificate = cert;
            } catch (CertificateException e) {
                Log.e(TAG, "exception", e);
            }
        }

        return certificate;
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

     @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_GEO_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    private void checkForLocationPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("DSBJ would like to use your Location");
                alertBuilder.setMessage("In the next step, we will request for your permission to access your location. This will enable DSBJ to prompt the events near you");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(FrontEndAdvancedActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_ACCESS_GEO_LOCATION);
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_GEO_LOCATION);
                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

        }
    }

}
