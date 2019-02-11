package RestfulUploads;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import dev.sutd.hdb.MyApp;
import dev.sutd.hdb.RealmController;
import io.realm.Realm;


public class Util {
    public void sendHttpPost(final JSONArray data, String url, final int type, final ArrayList<?> dataArr) throws JSONException {
        final Realm realm = RealmController.with(MyApp.getInstance()).getRealm();
        try{
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.i("act response", response);
                            int httpRes = Integer.valueOf(response);
                            if(httpRes ==1){
                                for(int i=0;i<dataArr.size();i++){
                                    /*realm.beginTransaction();
                                    dataArr.get(i).setUploaded(1);
                                    realm.commitTransaction();*/
                                }
                            }

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("data", data.toString());
                    params.put("type",String.valueOf(type));
                    return params;
                }
            };

            // Adding the request to the queue along with a unique string tag
           // MyApp.getInstance().addToRequestQueue(postRequest,"postRequest");
        }catch(NumberFormatException e){
            Log.e("upload", "error from php");
        }
    }

}
