package dev.sutd.hdb.WifiCluster;

import java.util.ArrayList;

import dev.sutd.hdb.WifiCluster.DBSCANClusterer;
import dev.sutd.hdb.WifiCluster.DBSCANClusteringException;
import dev.sutd.hdb.WifiCluster.DistanceMetricNumbers;
import models.ScanInformation;


/**
 * Created by Hasala on 3/4/2018.
 */
// min elements - 4
// max distance - 0.5
public class RunClusterer {
    //ArrayList<Map<CharSequence,Integer>> listOfMaps;// Array List of Hashmaps for multiple scan data
    //ArrayList<ScanInformation> listOfWifi;

    int minNumElements = 3;
    double maxDistance = 0.5;

    public ArrayList<ArrayList<ScanInformation>> setupAndPerformClusterer(ArrayList<ScanInformation> wifiList, double threshold) {
        //this.listOfWifi = new ArrayList<>();
        //this.listOfWifi.addAll(wifiList);
        ArrayList<ArrayList<ScanInformation>> clusterPoints = new ArrayList<>();
        System.out.println("WiFi list size: "+wifiList.size());
        DBSCANClusterer<ScanInformation> clusterer ;
        try {
            clusterer = new DBSCANClusterer(wifiList, minNumElements, maxDistance, new DistanceMetricNumbers());
            clusterer.setMinimalNumberOfMembersForCluster(minNumElements);
            clusterer.setMaximalDistanceOfClusterMembers(threshold);
            clusterer.setInputValues(wifiList);
            clusterer.setDistanceMetric(new DistanceMetricNumbers());

            clusterPoints = clusterer.performClustering();
            //System.out.println("Cluster Points");
            System.out.println("cluster size: "+clusterPoints.size());
        }
        catch (DBSCANClusteringException e){
            System.out.println(e.getMessage());
        }
        System.out.println("return cluster points size: "+clusterPoints.size());
        return clusterPoints;
    }

}
