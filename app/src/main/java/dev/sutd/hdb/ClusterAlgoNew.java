package dev.sutd.hdb;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import models.Cluster;


public class ClusterAlgoNew {
	private Context context;
	private SharedPreferences sharedpreferences;
	private int cluDuration;
	public ClusterAlgoNew(Context cont)
	{
		this.context = cont;
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		int du = sharedpreferences.getInt("CLUSTER_DURATION", 15);
		cluDuration = du*60;
	}
	
public ArrayList<Cluster> getClusterList(ArrayList<Cluster> clu, long endTime){
	
	ArrayList<Cluster> resultCluster = new ArrayList<Cluster>();
	ArrayList<Cluster> errorCluster = new ArrayList<Cluster>();
	 ArrayList<Cluster> resultNodes = new ArrayList<Cluster>();
	 Collections.sort( clu, new DateComparator());	
    Cluster temp = clu.get(0);
   	int cluLen = clu.size()-1;
   	ArrayList<Cluster> tempArray = new ArrayList<Cluster>();
   //Checking for time where there is no data:
   	Cluster temp1,next1;
   	for(int i =0; i<cluLen-1; i++){
   		temp1 = clu.get(i);
   		next1 = clu.get(i+1);
   		long du;
   		if(i<cluLen-2){
   		du = (next1.getTime() - temp1.getTime())/1000;}
   		else
   		{
   		du = (System.currentTimeMillis()-next1.getTime())/1000;
   		}
   		if(du>600){
   			errorCluster.add(temp1);
   			//Log.i(getReadableTime(Long.parseLong(temp1.getTime())), getReadableTime(Long.parseLong(next1.getTime())));
   		}

   	}
   	for (int i = 0; i < cluLen; i++ )
   	{
   		
   		Cluster next = clu.get(i+1);
   		long timeDiff = next.getTime() - temp.getTime();
   		
   		if(next.getAccuracy()>200||timeDiff<180000){
   			continue;
   		}
   		Location startLoc = new Location("");
   		startLoc.setLatitude(temp.getLatitude());
   		startLoc.setLongitude(temp.getLongitude());
   		
   		Location endLoc = new Location("");
   		endLoc.setLatitude(next.getLatitude());
   		endLoc.setLongitude(next.getLongitude());
   		
   		float dist = startLoc.distanceTo(endLoc);
   		double maxDist =  temp.getAccuracy() + next.getAccuracy()+20;
   		maxDist = Math.max(maxDist, 50.0);
   		
   		if(dist <= maxDist)
   		{	double latAvg1=0, longAvg1=0, accuracy1 =0, altitude = 0;
   			if(tempArray.size()<=1){
   			 latAvg1 = (temp.getLatitude() + next.getLatitude())/2;
			longAvg1 = (temp.getLongitude() + next.getLongitude())/2;
			accuracy1 = Math.max(temp.getAccuracy(), next.getAccuracy());
			//altitude = Math.max(temp.getAltitude(), next.getAltitude());
   			}
   			else{
   				ArrayList<Cluster>tempTempArray = tempArray;
   				tempTempArray.add(next);
   				for(int a =0; a < tempTempArray.size(); a++){
					latAvg1 += tempTempArray.get(a).getLatitude();
					longAvg1 += tempTempArray.get(a).getLongitude();
					accuracy1 = Math.max(accuracy1, tempTempArray.get(a).getAccuracy());
				}
				latAvg1 = latAvg1/tempTempArray.size();
				longAvg1 = longAvg1/tempTempArray.size();
   				
   			}
			
   			//double alti = Math.max(temp.getAltitude(), next.getAltitude());
			Cluster avgCluster = new Cluster(latAvg1, longAvg1, temp.getTime(), (long)temp.getDuration(), accuracy1);
			
   			tempArray.add(temp);
   			temp = avgCluster;
   			if(i==clu.size()-2)
       		{
       			long du = endTime - tempArray.get(0).getTime();
       			tempArray.add(next);	
				double lat = 0, longi = 0, accu = 0, alti=0;
				for(int a =0; a < tempArray.size(); a++){
					lat += tempArray.get(a).getLatitude();
					longi += tempArray.get(a).getLongitude();
					accu = Math.max(accu, tempArray.get(a).getAccuracy());
					//alti = Math.max(alti, tempArray.get(a).getAltitude());
				}
				lat = lat/tempArray.size();
				longi = longi/tempArray.size();
				
				Cluster avgClu = new Cluster(lat, longi, tempArray.get(0).getTime(), du/1000, accu);
				tempArray = new ArrayList<Cluster>();
   			resultCluster.add(avgClu);
       		}
   			
   		}
   		else{
   			if(tempArray.size() == 0){
   			long dura;
				dura = next.getTime() - temp.getTime();
				
   			temp.setDuration(dura/1000);
   			resultCluster.add(temp); temp = next;}
   			else{
   				tempArray.add(temp);
				long dura = next.getTime() - tempArray.get(0).getTime();
				double latAvg = 0, longAvg = 0, accuracy = 0, alti = 0;
				for(int a =0; a < tempArray.size(); a++){
					latAvg += tempArray.get(a).getLatitude();
					longAvg += tempArray.get(a).getLongitude();
					accuracy = Math.max(accuracy, tempArray.get(a).getAccuracy());
					//alti = Math.max(alti, tempArray.get(a).getAltitude());
				}
				latAvg = latAvg/tempArray.size();
				longAvg = longAvg/tempArray.size();
				
				Cluster avgCluster = new Cluster(latAvg, longAvg, tempArray.get(0).getTime(), dura/1000, accuracy );
				tempArray = new ArrayList<Cluster>();
   			resultCluster.add(avgCluster);
   			temp = next;
   			}
				
   			if(i==clu.size()-2)
       		{
       			long duration = endTime - next.getTime();
       			next.setDuration(duration/1000);
       			resultCluster.add(next);
       		}
   			
   				
   		}
   			
   	}
   	
   	ArrayList<Cluster> finalClusters = new ArrayList<Cluster>();
   		finalClusters = resultCluster;
   		
   
   	
   	int resLenNodes = finalClusters.size();
   	double maxDuration = cluDuration;
   	ArrayList<Cluster> interimNodes = new ArrayList<Cluster>();
   	for(int i =0; i<resLenNodes; i++){
   		Cluster node = finalClusters.get(i);
   		if(node.getDuration() > maxDuration ) 
   		{
   			if(i==0){ interimNodes.add(node);}
   			else
   			{	if(!(node.getTime()==(finalClusters.get(i-1).getTime())))
   					interimNodes.add(node);
   			}
   			
   		}
   	}
   	
  //Removing faulty nodes.
  	if(interimNodes.size()>0)
   	{
   	Cluster tempInt = interimNodes.get(0);
   	Cluster nextInt;
   	if(interimNodes.size()==1)
		{
			resultNodes.add(tempInt);
		}
   	else{
   	for(int i =0; i<interimNodes.size()-1; i++){
   		nextInt = interimNodes.get(i+1);
   		long du1 = tempInt.getTime() + (long)tempInt.getDuration()*1000;
   		long du2 = nextInt.getTime();
   		if((du2-du1)/1000 < 900 ) 
   		{
   			
   			Location startLoc = new Location("");
	   		startLoc.setLatitude(tempInt.getLatitude());
	   		startLoc.setLongitude(tempInt.getLongitude());
	   		
	   		Location endLoc = new Location("");
	   		endLoc.setLatitude(nextInt.getLatitude());
	   		endLoc.setLongitude(nextInt.getLongitude());
	   		double dist = startLoc.distanceTo(endLoc);
	   		if(dist < (tempInt.getAccuracy()+nextInt.getAccuracy()) && dist < 250)
	   		{
	   			long diff = nextInt.getTime()+ (long)nextInt.getDuration()*1000;
	   			long du = (diff-tempInt.getTime())/1000;
	   			tempInt.setDuration(du);
	   			//Log.i(tempInt.getTime(), i+"dist: "+dist);
	   		}
	   		else{
	   		resultNodes.add(tempInt);
	   		tempInt = nextInt;
	   			}
   		}
   		else{
   			resultNodes.add(tempInt);
   			tempInt = nextInt;
   		}
   		if(i == interimNodes.size()-2){
   			resultNodes.add(nextInt);
   			}
   		}
   	}

   	}
   	else{
   		resultNodes = interimNodes;
   	}
   	
   	//resultNodes = interimNodes;
   return resultNodes;
}	



}
