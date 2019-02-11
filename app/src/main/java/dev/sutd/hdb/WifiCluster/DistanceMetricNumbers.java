package dev.sutd.hdb.WifiCluster;

import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.sutd.hdb.WifiCluster.CosineSimilarity;
import dev.sutd.hdb.WifiCluster.DistanceMetric;
import models.ScanInformation;


/**
 * Distance metric implementation for numeric values.
 *
 * @author <a href="mailto:cf@christopherfrantz.org>Christopher Frantz</a>
 *
 */
public class DistanceMetricNumbers implements DistanceMetric<ScanInformation> {
    /*@Override
    public double calculateDistance(Number val1, Number val2) {
        return Math.abs(val1.doubleValue() - val2.doubleValue());
    }*/
    @Override
    public double calculateDistance(ScanInformation val1, ScanInformation val2){
        Map<CharSequence,Integer> map1 = new LinkedHashMap<CharSequence, Integer>();
        Map<CharSequence,Integer> map2 = new LinkedHashMap<CharSequence, Integer>();

        for(int x=0; x<val1.getScanObjectList().size();x++){
            System.out.print("MAp1 values :"+val1.getScanObjectList().get(x).getBSSID());
            map1.put(val1.getScanObjectList().get(x).getBSSID(), val1.getScanObjectList().get(x).getRSSI());

        }

        for(int y=0; y<val2.getScanObjectList().size();y++){
            System.out.print("MAp2 values :"+val2.getScanObjectList().get(y).getBSSID());
            map2.put(val2.getScanObjectList().get(y).getBSSID(), val2.getScanObjectList().get(y).getRSSI());

        }
        //Log.e("map size", map1.size()+"  :  "+map2.size());
        CosineSimilarity similarity = new CosineSimilarity();

        double score = similarity.cosineSimilarity(map1,map2);
        //Log.e("score", score+"");
        return score;// returning inverse similarity
    }
}
