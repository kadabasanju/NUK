package dev.sutd.hdb;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * Created by Sanj on 2/8/2018.
 */
public class ActivityRecognizedService extends IntentService {

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }



    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            String act = "";
            double confi =0;
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    //Log.e("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    act = "Vehicle";
                    confi = activity.getConfidence();
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    //Log.e( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    act = "Bicycle";
                    confi = activity.getConfidence();
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    //Log.e( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    act = "Foot";
                    confi = activity.getConfidence();
                    break;
                }
                case DetectedActivity.RUNNING: {
                    //Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    act = "Running";
                    confi = activity.getConfidence();
                    break;
                }
                case DetectedActivity.STILL: {
                    //Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    act = "Still";
                    confi = activity.getConfidence();
                    break;
                }

                case DetectedActivity.WALKING: {
                    //Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    act = "Foot";
                    confi = activity.getConfidence();
                    break;
                }

            }
            //broadcast this!
            if(act!="") {
                Intent intent = new Intent("ACTIVITY_RECOGNITION_GOOGLE");
                // You can also include some extra data.
                intent.putExtra("activity", act);
                intent.putExtra("confidence", confi);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }

        }
    }

}