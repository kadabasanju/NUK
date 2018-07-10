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
    ArrayList<ScanInformation> listOfWifi;
    ArrayList<ArrayList<ScanInformation>> clusterPoints;
    int minNumElements = 4;
    double maxDistance = 0.5;

    public ArrayList<ArrayList<ScanInformation>> setupAndPerformClusterer(ArrayList<ScanInformation> wifiList, double threshold) {
        this.listOfWifi = wifiList;
        //System.out.println("WiFi list size: "+wifiList.size());
        DBSCANClusterer<ScanInformation> clusterer;
        try {
            clusterer = new DBSCANClusterer(listOfWifi, minNumElements, maxDistance, new DistanceMetricNumbers());
            clusterer.setMinimalNumberOfMembersForCluster(minNumElements);
            clusterer.setMaximalDistanceOfClusterMembers(threshold);
            clusterer.setInputValues(listOfWifi);
            clusterer.setDistanceMetric(new DistanceMetricNumbers());

            clusterPoints = clusterer.performClustering();
            //System.out.println("Cluster Points");
            //System.out.println(clusterPoints.size());
        }
        catch (DBSCANClusteringException e){
            System.out.println(e.getMessage());
        }
        System.out.println("return cluster points size: "+clusterPoints.size());
        return clusterPoints;
    }

}
