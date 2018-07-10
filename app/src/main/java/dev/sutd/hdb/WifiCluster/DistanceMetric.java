package dev.sutd.hdb.WifiCluster;

import dev.sutd.hdb.WifiCluster.DBSCANClusteringException;

/**
 * Interface for the implementation of distance metrics.
 *
 * @author <a href="mailto:cf@christopherfrantz.org>Christopher Frantz</a>
 *
 * @param <V> Value type to which distance metric is applied.
 */
public interface DistanceMetric<V> {
    //public double calculateDistance(V val1, V val2) throws DBSCANClusteringException;
    //public double calculateDistance(Map<CharSequence,Integer> vector1, Map<CharSequence,Integer> vector2) throws DBSCANClusteringException;
    public double calculateDistance(V val1, V val2) throws DBSCANClusteringException;
}