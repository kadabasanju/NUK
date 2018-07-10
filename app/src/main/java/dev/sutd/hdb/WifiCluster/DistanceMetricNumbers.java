package dev.sutd.hdb.WifiCluster;

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
        //System.out.println("val 1: "+val1.getTimestamp());
        //System.out.println("val 2: "+val2.getTimestamp());
      /*  for(int q=0;q<val1.size();q++) {
            for(int x=0; x<val1.get(q).getScanObjects().size();x++){
                map1.put(val1.get(q).getScanObjects().get(x).getBSSID(), val1.get(q).getScanObjects().get(x).getRSSI());
            }
        }
        for(int k=0;k<val2.size();k++){
            for(int y=0; y<val1.get(k).getScanObjects().size();y++){
                map1.put(val1.get(k).getScanObjects().get(y).getBSSID(), val1.get(k).getScanObjects().get(y).getRSSI());
            }
        }*/
        for(int x=0; x<val1.getScanObjectList().size();x++){
            map1.put(val1.getScanObjectList().get(x).getBSSID(), val1.getScanObjectList().get(x).getRSSI());
            //String a1 = val1.getScanObjectList().get(x).getBSSID();
            //long b1 = val1.getScanObjectList().get(x).getRSSI();
            //System.out.println("val 1 "+"mac: "+a1+" rssi: "+b1);
        }
        //System.out.println("val 1: "+val1.getScanObjectList().size());
        for(int y=0; y<val2.getScanObjectList().size();y++){
            map2.put(val2.getScanObjectList().get(y).getBSSID(), val2.getScanObjectList().get(y).getRSSI());
            //String a2 = val2.getScanObjectList().get(y).getBSSID();
            //long b2 = val2.getScanObjectList().get(y).getRSSI();
            //System.out.println("val 2 "+"mac: "+a2+" rssi: "+b2);
        }
        //System.out.println("val 2: "+val2.getScanObjectList().size());
        CosineSimilarity similarity = new CosineSimilarity();
        //double score = 1-similarity.cosineSimilarity(map1,map2);
        double score = similarity.cosineSimilarity(map1,map2);
        //System.out.println("SCORE: "+score);
        return score;// returning inverse similarity
    }
}
