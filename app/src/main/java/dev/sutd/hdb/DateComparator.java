package dev.sutd.hdb;

import java.util.Comparator;

import models.Cluster;


public class DateComparator implements Comparator<Cluster> {


    @Override
    public int compare(Cluster arg0, Cluster arg1) {
        long time1 = Long.valueOf(arg0.getTime());
        long time2 = Long.valueOf(arg1.getTime());
        if(time1>time2)
        {
            return 1;
        }
        else if(time2>time1)
        {
            return -1;
        }else{

        return 0;}
    }
}
