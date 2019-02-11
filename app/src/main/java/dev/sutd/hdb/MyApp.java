

package dev.sutd.hdb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import com.crashlytics.android.Crashlytics;


import io.fabric.sdk.android.Fabric;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApp extends Application {
    private static MyApp instance;
    SharedPreferences sharedpreferences;
    private  RequestQueue requestQueue;
    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initRealm();
    }


    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        //MultiDex.install(this);
    }

    private void initRealm() {

        sharedpreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String keyStr = sharedpreferences.getString("realm_pwd", "");
        byte[] key = new byte[64];
        if(keyStr.equals("")){

            new SecureRandom().nextBytes(key);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String keyS =  Base64.encodeToString(key, Base64.DEFAULT);

            editor.putString("realm_pwd", keyS);
            editor.commit();
        }
        else {

            key =  Base64.decode(keyStr, Base64.DEFAULT);
            //Log.d(keyStr, new String(key));
        }
        //String string ="aefsyjabgdndbsgsjabatabahagatvbfdueugewdvshfhriutfkjnbvhjgttydwa";
        //byte[] b = string.getBytes(Charset.forName("UTF-8"));
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("hdb.realm")
                .schemaVersion(1)
                .encryptionKey(key)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }



    /**
     public method to add the Request to the the single
     instance of RequestQueue created above.Setting a tag to every
     request helps in grouping them. Tags act as identifier
     for requests and can be used while cancelling them
     **/
    public void addToRequestQueueHttps(Request request,String tag)
    {
        request.setTag(tag);
        getRequestQueueHttps(1).add(request);

    }

    public void addToRawRequestQueueHttps(Request request,String tag)
    {
        request.setTag(tag);
        getRequestQueueHttps(2).add(request);

    }

    public RequestQueue getRequestQueue()
    {
        if (requestQueue==null)
            requestQueue= Volley.newRequestQueue(getApplicationContext());

        return requestQueue;
    }

    /**
     public method to add the Request to the the single
     instance of RequestQueue created above.Setting a tag to every
     request helps in grouping them. Tags act as identifier
     for requests and can be used while cancelling them
     **/
   /* public void addToRequestQueue(Request request,String tag)
    {
        request.setTag(tag);
        getRequestQueue().add(request);

    }*/
    /**
     Cancel all the requests matching with the given tag
     **/

    public void cancelAllRequests(String tag)
    {
        getRequestQueue().cancelAll(tag);
    }





    //public static void allowMySSL(){
    public RequestQueue getRequestQueueHttps(int type) {
        if (requestQueue == null) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = null;
                if(type==1){
                    caInput = MyApp.getInstance().getResources().openRawResource(R.raw.ca);
                }
                else if(type == 2){
                    caInput = MyApp.getInstance().getResources().openRawResource(R.raw.raw_server_ca_bundle);
                }

                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }
                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);
                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                // Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
                HttpsURLConnection.setDefaultSSLSocketFactory(context
                        .getSocketFactory());
                requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack(null, context.getSocketFactory()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }


        }
        return requestQueue;

    }




}