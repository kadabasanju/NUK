package dev.sutd.hdb;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
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
import io.realm.Realm;
import io.realm.RealmResults;
import models.Data;
import models.GeoActivity;
import models.Sound;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UploadJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {

        mJobHandler.sendMessage( Message.obtain( mJobHandler, 1, params ));
        // tHis needs to be true to say that we will explicitly specify the jobFinished.
        // If it is set to false, the system decides that the method completed on time and needs no confirmation from the developer.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages( 1 );
        MainService.startUploadProcess();
        return false;
    }

    private Handler mJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage( Message msg ) {
            UploadService.uploadData();
            // To tell the job scheduler that the task has been completed and can dismiss the task!

            jobFinished( (JobParameters) msg.obj, false );
            MainService.startUploadProcess();
            return true;
        }

    } );


}
