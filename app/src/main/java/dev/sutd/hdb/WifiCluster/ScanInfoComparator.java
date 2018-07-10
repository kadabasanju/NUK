package dev.sutd.hdb.WifiCluster;

import java.util.Comparator;

import dev.sutd.hdb.WifiCluster.ClusterIDScanInfo;


/**
 * Created by Hasala on 21/4/2018.
 */

public class ScanInfoComparator implements Comparator<ClusterIDScanInfo> {
    @Override
    public int compare(ClusterIDScanInfo s1, ClusterIDScanInfo s2) {
        return Long.compare(s1.getTimestamp(), s2.getTimestamp());
    }
}
