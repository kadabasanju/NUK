package dev.sutd.hdb;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

import org.achartengine.GraphicalView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Cluster;
import models.Data;
import models.GeoActivity;
import models.NipunActivity;
import models.Node;
import models.NodeRectangle;
import models.Sound;

public class GraphViewActivity extends Activity {
	Canvas canvas;
	GraphicalView mChartView;
	View rootView;
	DemoView demoview;

	ArrayList<NodeRectangle> rects;
	ArrayList<Long> errorTimes;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		long startTime = b.getLong("startTime");
		//Log.i("startTime", ""+startTime);
		long endTime = b.getLong("endTime");
		List<Node> googleList = getGeoActivities(startTime, endTime);
		List<NipunActivity> nipunList = getNipunActivities(startTime, endTime);
		List<Sound> saList = getSocioActivities(startTime, endTime);
		ArrayList<Data> data =  RealmController.with(MyApp.getInstance()).getDailyData(startTime, endTime);
		//ArrayList<Data> d = convertRealmToArray(data);
		ArrayList<Cluster> rawClu = getRawData(startTime, endTime, data);
		ArrayList<Cluster> clu = clusterData(startTime, endTime, rawClu);
		//ArrayList<Node> predictedList = getPredictedList(rawClu);
		demoview = new DemoView(this,startTime, endTime, clu, rawClu, data,saList,googleList,nipunList);
		
		setContentView(demoview);
	}


	private List<Sound> getSocioActivities(long startTime, long endTime){
		List<Sound> sa = RealmController.with(MyApp.getInstance()).getDailySoundData(startTime,endTime);
		return sa;
	}

	private List<Node> getGeoActivities(long startTime, long endTime){
		GeoActivity firstAct = RealmController.with(MyApp.getInstance()).getLastDailyGeoData(startTime);
		List<Node> ga = new ArrayList<>();
		if(firstAct!= null){
			if(firstAct.getTime()!=0&&firstAct.getgAct()!=null) {
				//System.out.println(firstAct.getTime() + "  " + firstAct.getgAct());
				Node f = new Node(firstAct.getTime(), firstAct.getgAct());
				ga.add(f);
			}

		}

		List<GeoActivity> gArr = RealmController.with(MyApp.getInstance()).getDailyGeoData(startTime, endTime);
		for(int i=0;i<gArr.size();i++){
			Node a = new Node(gArr.get(i).getTime(),gArr.get(i).getgAct());
			ga.add(a);

		}
		return ga;
	}
	private List<NipunActivity> getNipunActivities(long startTime, long endTime){
		List<NipunActivity> na = new ArrayList<>();//RealmController.with(MyApp.getInstance()).getDailyNipunData(startTime, endTime);
		return na;
	}
	private ArrayList<Node> getPredictedList(ArrayList<Cluster> clu){
		ArrayList<Node>pred = new ArrayList<Node>();
		Collections.sort( clu, new DateComparator());	
		for(int i =0; i<clu.size()-1; i++)
		{ 	
			String act = "Unknown";
			Cluster curr = clu.get(i);
			Cluster next = clu.get(i+1);
			if(curr.getAccuracy()>250){
				continue;
			}
			Location start = new Location("");
			start.setLatitude(curr.getLatitude());
			start.setLongitude(curr.getLongitude());
			Location end = new Location("");
			end.setLatitude(next.getLatitude());
			end.setLongitude(next.getLongitude());
			float dist = start.distanceTo(end);
			long duration = next.getTime()-curr.getTime();
			double speed = (dist*3600)/(duration);
			if (speed<=1){
				act = "Still";
			} 
			else if (speed>1 && speed<=5){
				act = "On Foot";
				//Log.i("distance", ""+speed);
			}
			else if (speed>15 && speed<40){
				act = "Bus";
			}
			else if (speed>=40){
				act = "Train";
			}
			Node aa = new Node(curr.getTime(), act);
			pred.add(aa);
		}
		
		return pred;
	}
	
	private ArrayList<Cluster> clusterData(long startTime, long endTime,ArrayList<Cluster> clu){
		ArrayList<Cluster> resNodes = new ArrayList<Cluster>();

			if(clu.size() > 0){
				ClusterAlgoNew ca = new ClusterAlgoNew(this);
				resNodes = ca.getClusterList(clu,endTime);
				errorTimes = getNoData(clu,startTime,endTime);
			}
			else{
				return null;
			}
			return resNodes;	
    }
	
	private ArrayList<Cluster> getRawData(long startTime, long endTime, ArrayList<Data> dataArray){

		ArrayList<Cluster> clu = new ArrayList<Cluster>();
		for(int i =0; i<dataArray.size(); i++){

			Cluster cluster = new Cluster(dataArray.get(i).getLatitude(), dataArray.get(i).getLongitude(), dataArray.get(i).getTime(), 0, dataArray.get(i).getAccuracy());
			cluster.setActivity("");
			clu.add(cluster);

		}
		
			return clu;	
    }
	
	private ArrayList<Long> getNoData(ArrayList<Cluster> clu, long startTime, long endTime){
		 Collections.sort( clu, new DateComparator());
		 ArrayList<Long> errorTimes = new ArrayList<Long>();
		 int cluLen = clu.size()-1;
		 long temp1,next1;
		   	for(int i =-1; i<cluLen; i++){
		   		if(i ==-1){
		   			temp1 = startTime;
		   			next1 =clu.get(0).getTime();
		   		}
		   		else{
		   			temp1 = clu.get(i).getTime();
		   			if(i == cluLen-1){
			   			next1=endTime;
			   		}
			   		else{
			   		
			   		next1 = clu.get(i+1).getTime();
			   		}
		   		}
		   		long du;
		   		du = (next1 - temp1)/1000;
		   		
		   		if(du>1260){
		   			errorTimes.add(temp1);
		   			errorTimes.add(next1);

		   		}
		   	}
		return errorTimes;
	}
	private String getReadableTime(long time){
 		SimpleDateFormat    formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd HH:mm:ss");       
      	
      	Date d = new Date(time);
        String    strTime    =    formatter.format(d); 
        return strTime;
 	}


	private class DemoView extends View{
		ArrayList<Data> data;
		ArrayList<Node>pred;
		 private static final float MIN_ZOOM = 1f;
		 private static final float MAX_ZOOM = 5f;
	       private float scaleFactor = 1.f;
	       private ScaleGestureDetector detector;
	       private static final int NONE = 0;
	       private static final int DRAG = 1;
	       private static final int ZOOM = 2;

	       private int mode;
	       //These two variables keep track of the X and Y coordinate of the finger when it first
	       //touches the screen
	       private float startX = 0f;
	       private float startY = 0f;

	       //These two variables keep track of the amount we need to translate the canvas along the X
	       //and the Y coordinate
	       private float translateX = 0f;
	       private float translateY = 0f;
	        
	       //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
	       //panned.
	       private float previousTranslateX = 0f;
	       private float previousTranslateY = 0f;   
		ArrayList<Cluster> cluster, rawClu;
		long sTime, eTime;
		List<Sound> socioAct;
		List<Node> geoAct;
		List<NipunActivity> nipunAct;
		public DemoView(Context context, long sTime, long eTime, ArrayList<Cluster>clu, ArrayList<Cluster> rawCluster, ArrayList<Data> data, List<Sound> sa, List<Node>ga, List<NipunActivity> nipun){
			super(context);

			this.sTime = sTime;
			this.eTime = eTime;
			this.cluster = clu;
			this.rawClu = rawCluster;
			this.socioAct = sa;
			this.geoAct = ga;
			this.nipunAct = nipun;
			//this.pred = pred;
			this.data = data;
			detector = new ScaleGestureDetector(getContext(), new ScaleListener());
		}

		
		public boolean onTouchEvent(MotionEvent event) throws NullPointerException{
			boolean dragged = false;
			float touchX = event.getX();
			float touchY = event.getY();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN: int flag = 0;
											  for(int i =0; i< rects.size(); i++){
												  if(rects.get(i).contains(touchX, touchY))
												  {
													  flag = 1;
													  Intent intent = new Intent(Intent.ACTION_VIEW,
									                           Uri.parse("http://maps.google.com/maps?q=" + rects.get(i).getLatitude() + "," + rects.get(i).getLongitude()));
									                           startActivity(intent);
												  }
					 						  }
											  if(flag ==0){
												  mode = DRAG;
												  startX = event.getX() - previousTranslateX;
												  startY = event.getY() - previousTranslateY;}
											  break;

				case MotionEvent.ACTION_MOVE: translateX = event.getX() - startX;
											  translateY = event.getY() - startY;
											  double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
													  Math.pow(event.getY() - (startY + previousTranslateY), 2));
											  if(distance > 0) {
												  dragged = true;}              
											  break;

				case MotionEvent.ACTION_POINTER_DOWN:mode = ZOOM;
													 break;

				case MotionEvent.ACTION_UP:mode = NONE;
										   dragged = false;
										   previousTranslateX = translateX;
										   previousTranslateY = translateY;
										   break;

				case MotionEvent.ACTION_POINTER_UP:mode = DRAG;
												   previousTranslateX = translateX;
												   previousTranslateY = translateY;
												   break;      
			}

		detector.onTouchEvent(event);

		//   set to true (meaning the finger has actually moved)
		if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
			invalidate();
			}
		return true;
		}

		private class ScaleListener extends SimpleOnScaleGestureListener {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			invalidate();
			return true;
			}
			}
		@Override protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.save();
			int num = 11;
			canvas.scale(this.scaleFactor, this.scaleFactor, this.detector.getFocusX(), this.detector.getFocusY());
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			int height_factor = 1;
			
			// custom drawing code here
			// remember: y increases from top to bottom
			// x increases from left to right
			
			Paint paint = new Paint();
			paint.setStyle(Style.FILL);

			// make the entire canvas white
			paint.setColor(Color.WHITE);
			canvas.drawPaint(paint);
			// another way to do this is to use:
			// canvas.drawColor(Color.WHITE);

			
			canvas.drawLine(0, height - 40, width, height - 40, paint);
			for(int i = 0; i<24; i+=4){
	        	String time=""; 
	        	if(i<12 && i>0)
	        	{
	        		time = i+" AM";
	        	}
	        	if(i>12){
	        		time = (i-12)+" PM";
	        	}
	        	if(i == 12){
	        		time = "12 PM";
	        	}
	        	paint.setStyle(Style.FILL);
	        	paint.setColor(Color.BLACK);
	        	canvas.drawText(time, width/24*i, height-20,paint);
	        	
	        }
			paint.setStyle(Style.FILL);
        	paint.setColor(Color.BLACK);
			canvas.drawText("12AM", 0, height-20,paint);




			 rects =  new ArrayList<NodeRectangle>();
			 
		       ArrayList<Float>line = new ArrayList<Float>();
		      try{
		       for (int i = 0; i <cluster.size();i++) {
		        	paint.setColor(Color.BLACK);
		        	paint.setStrokeWidth(2);
		        	paint.setStyle(Style.STROKE);
		        	
		          
		          long strtDu = cluster.get(i).getTime();
				   //Log.i("duration "+i,cluster.get(i).getDuration()+"");
		          long endDu = strtDu + (long)(cluster.get(i).getDuration()*1000);
		          float strtP = getXValueForNode(strtDu,width, sTime);
		         float endP = getXValueForNode(endDu,width, sTime) ;
		         /*if(i%2 == 0)
		        	{
		        		
		        		if(i != 0)
							line.add(strtP);
		        		line.add(endP);
		        	}
		         else{
		        	 line.add(strtP);
		        	 if(i < cluster.size()-1)
		        		 line.add(endP);
		         }*/


				  if(i<cluster.size()-1){

					  long endL = cluster.get(i + 1).getTime();
					  float tym = getXValueForNode(endL,width,sTime);

						  line.add(endP);
					  //Log.i("endP"+i,endP+" "+endDu);
						  line.add(tym);


				  }


		         canvas.drawRect(strtP, 20, endP, 50, paint);
		         NodeRectangle nRect = new NodeRectangle(strtP,20,endP,50,(i+1),cluster.get(i).getLatitude(), cluster.get(i).getLongitude());
		         rects.add(nRect);
		         int strX = (int)(strtP+((endP-strtP)/2));
		         
		         canvas.drawText(""+(i+1),strX,40, paint);
		         
		        
		        }
		       
		       for(int i = 0; i < line.size()-1; i+=2){
				   //Log.i("line no:"+i,line.get(i)+"  "+line.get(i+1)+"");
		    	   canvas.drawLine(line.get(i), 35, line.get(i + 1), 35, paint);
		       }
		       for (int i = 0; i <errorTimes.size();i+=2) {
		        	paint.setColor(Color.LTGRAY);
		        	//paint.setAlpha(10);
		        	paint.setStyle(Style.FILL);
		        	
		          
		          long strtDu = errorTimes.get(i);
		          //Log.i("start",""+errorTimes.get(i));
		          long endDu = errorTimes.get(i+1);
		          //Log.i("end",""+errorTimes.get(i+1));
		          float strtP = getXValueForNode(strtDu, width, sTime);
		         float endP = getXValueForNode(endDu, width, sTime) ;
		         canvas.drawRect(strtP, 20, endP, 50, paint);
		         
		        }


				  //ArrayList<Node> resAct = getGpsData(nipunAct, sTime, eTime);

				  //Log.d("size in main resAct", resAct.size()+"");
				  if(nipunAct.size()>0) {
					  for (int i = 0; i < nipunAct.size(); i++) {

						  paint.setStyle(Style.FILL);
						  String strAct = nipunAct.get(i).getnAct().split(":")[0];
						  double confidence = Double.parseDouble(nipunAct.get(i).getnAct().split(":")[1]);
						  //Log.d(strAct + "  " + i, resAct.get(i).getActivity().split(":")[1]);
						  String act = getActivityData(strAct);

						  /*if (confidence > 0.70) {
							  paint.setColor(Color.BLUE);
						  } else {
							  paint.setColor(Color.RED);
						  }*/
						  if (act == "_" || act == "W" || act == "b") {
							  float actx = getXValue(nipunAct.get(i).getTime(), width, sTime);
							  height_factor++;

							  if (act == "_") {
								  act = ".";
							  } else if (act == "W") {
								  act = "-";
							  } else if (act == "b") {
								  act = "`";
							  }
							  canvas.drawText(act, (int) actx, height * 2 / num, paint);
						  } else if (act == "B" || act == "c" || act == "M") {
							  float actx = getXValue(nipunAct.get(i).getTime(), width, sTime);

							  height_factor++;
							  if (act == "c") {
								  act = "_";
								  paint.setColor(Color.RED);
							  } else if (act == "B") {
								  act = "-";
								  paint.setColor(Color.BLUE);
							  } else if (act == "M") {
								  act = "`";
								  paint.setColor(Color.GREEN);
							  }
							  canvas.drawText(act, (int) actx, height * 3 / num, paint);
						  }

					  }
				  }

				  Log.i("googleact before",geoAct.size()+"");


				  /*for (int i = 0; i<geoAct.size();i++) {
					  paint.setColor(Color.BLUE);
					  paint.setStyle(Style.FILL);


					  String act = getActivityData(geoAct.get(i).getgAct().split(":")[0]);
					  Log.i("act",act);
					  if(!act.equals("")) {
						  if(act=="_"){
							  paint.setColor(Color.MAGENTA);
						  }
						  if(act=="W" || act =="F"){
							  act = "*";
							  paint.setColor(Color.BLUE);
						  }
						  else if(act == "b"){
							  act = "|";
							  paint.setColor(Color.RED);
						  }
						  else if(act == "v"){
							  act = "~";
							  paint.setColor(Color.GREEN);
						  }
						  float actx = getXValue(geoAct.get(i).getTime(), width, sTime);

						  canvas.drawText(act, (int) actx, height * 4 / num, paint);
					  }
				  }*/
				  ArrayList<Node> googleAct = getGoogleData(geoAct, sTime, eTime);
				  Log.i("size after",googleAct.size()+"");
					  for (int i = 0; i <googleAct.size();i++) {

						  paint.setColor(Color.BLUE);
						  paint.setStyle(Style.FILL);
						  double confidence = Double.parseDouble(googleAct.get(i).getActivity().split(":")[1]);

						  String act = getActivityData(googleAct.get(i).getActivity().split(":")[0]);
						  if(!act.equals("")) {
							  if(act=="_"){
								  paint.setColor(Color.MAGENTA);
							  }
							  if(act=="W" || act =="F"){
								  act = "*";
								  paint.setColor(Color.BLUE);
							  }
							  else if(act == "b"){
								  act = "|";
								  paint.setColor(Color.RED);
							  }
							  else if(act == "v"){
								  act = "~";
								  paint.setColor(Color.GREEN);
							  }
							  float actx = getXValue(googleAct.get(i).getTime(), width, sTime);

							  canvas.drawText(act, (int) actx, height * 4 / num, paint);
						  }
					  }
				  //}


		       //socio activity
		     /* for (int i = 0; i <data.size();i++) {
		       	paint.setColor(Color.RED);
		       	paint.setStyle(Style.FILL);
		       	float actx = getXValue(data.get(i).getTime(),width, sTime);
		         String act = getSocioData(data.get(i).getSocio_activity());
		         canvas.drawText(act, (int)actx,height*6/num,paint);
		        
		       }*/
		       
		       double maxVal = 0,minVal = 0;
		       for(int i =0; i<socioAct.size(); i++){
		    	   double decibel = socioAct.get(i).getDecibel();
		    	   if(decibel > maxVal){
		    		   maxVal = decibel;
		    	   }
		    	   if(decibel < minVal){
		    		   minVal = decibel;
		    	   }
		       
		       }
		       
		       //Decibel level
		       for(int i =0; i<socioAct.size(); i++){
		    	   double decibel = normalise(socioAct.get(i).getDecibel(),maxVal,minVal);
		    	   float actx = getXValue(socioAct.get(i).getTime(),width,sTime);
		    	   float h = (height*5/num);
		    	   double grad = 50; 
		    	  
		    	   //if(decibel<=50){
		    		   
		    		   grad = decibel * 2.55;
		    		   
		    		   paint.setColor(Color.argb(255,255-(int)grad, (int)grad, 0));
		    	   //}
		    	  // else if (decibel == 50){
		    		//   paint.setColor(Color.argb(255, 255, 255, 0));
		    	   //}
		    	   //else {
		    		 //  grad =  255-decibel * 5.1;
		    		  // paint.setColor(Color.argb(255, (int) grad,255, 0));
		    	   //}
		    	   paint.setStyle(Style.FILL); 
		    	   
		    	
		    	   canvas.drawRect(actx,(float)(h-(decibel/4)) , actx + 5,  h, paint);
		       }
		       
		       // Battery level 
		       for(int i =0; i<socioAct.size(); i++){
		    	   double bat = socioAct.get(i).getBattery();
		    	   double grad = 0; 
		    	   if(bat < 50)
		    	   {
		    		   grad = bat * 5.1;
		    		   paint.setColor(Color.argb(255,255, (int)grad, 0));
		    	   }
		    	   else if(bat == 50){
		    		   paint.setColor(Color.argb(255, 255, 255, 0));
		    	   }
		    	   
		    	   else {
		    		   grad = 255 - bat * 5.1;
		    		   paint.setColor(Color.argb(255, (int) grad,255, 0));
		    	   }
		    	   double decibel = normalise(socioAct.get(i).getBattery(), 100, 0);
		    	   float actx = getXValue(socioAct.get(i).getTime(),width,sTime);
				   height_factor++;
		    	   float h = (height*6/num);
		    	   canvas.drawRect(actx, (float) (h - (decibel / 4)), actx + 5, h, paint);
		    	   
		    	  
		       } 
		       
		       // Location Provider
		      /* for(int i = 0; i<rawClu.size(); i++){
		    	   paint.setColor(Color.GREEN);
			       	paint.setStyle(Style.FILL);
			       	float actx = getXValue(Long.parseLong(rawClu.get(i).getTime()),width, sTime);
			         String net = rawClu.get(i).getProvider();
			         String res;
			         if(net.equals("network"))
			         {
			        	 res = "_";
			         }
			         else{
			        	 res = "G";
			         }
			         canvas.drawText(res, (int)actx,height*7/num,paint);
		       }*/
		       
		       // Speed 
		       double maxS = 0,minS = 0;
		       for(int i =0; i<data.size(); i++){
		    	   double speed = data.get(i).getSpeed();
		    	   if(speed > maxS){
		    		   maxS = speed;
		    	   }
		    	   if(speed < minS){
		    		   minS = speed;
		    	   }
		       
		       }
		       
		       //Speed level
		       for(int i =0; i<data.size(); i++){
		    	   double decibel = normalise(data.get(i).getSpeed(),maxS,minS);
		    	   float actx = getXValue(data.get(i).getTime(),width,sTime);
				   height_factor++;
		    	   float h = (height*7/num);
		    	   if(decibel <5 && decibel >=0){
		    		   paint.setColor(Color.BLUE);
		    		   paint.setStyle(Style.FILL);

		    	   }
		    	   if(decibel>=5 && decibel <10){
		    		   paint.setColor(Color.GREEN);
		    		   paint.setStyle(Style.FILL);
		    		   
		    	   }
		    	   if(decibel>=10 && decibel <15){
		    		   paint.setColor(Color.MAGENTA);
		    		   paint.setStyle(Style.FILL);

		    	   }
		    	   if(decibel>=15&&decibel<20){
		    		   paint.setColor(Color.CYAN);
		    		   paint.setStyle(Style.FILL);
		    		  
		    	   }
		    	   if(decibel>=20){
		    		   paint.setColor(Color.RED);
		    		   paint.setStyle(Style.FILL);
		    		   paint.setAlpha(30);

		    	   }

		    	   canvas.drawRect(actx,(float)(h-(decibel/4)) , actx + 5,  h, paint);
		       }
		       
		    // Light
		       double maxL = 0,minL = 0;
		       for(int i =0; i<socioAct.size(); i++){
		    	   double light = socioAct.get(i).getLight();
		    	   if(light > maxL){
		    		   maxL = light;
		    	   }
		    	   if(light < minL){
		    		   minL = light;
		    	   }
		       
		       }
		       
		       //Light level
		       for(int i =0; i<socioAct.size(); i++){
		    	   double decibel = normalise(socioAct.get(i).getLight(),maxL,minL);
		    	   float actx = getXValue(socioAct.get(i).getTime(),width,sTime);
				   height_factor++;
		    	   float h = (height*8/num);
		    	   double grad = 50; 
		    	   grad = decibel * 2.55;
	    		   
	    		   paint.setColor(Color.argb(255,(int)grad, 0, 255-(int)grad));  
		    	  /* if(decibel<=12.5){
		    		   
		    		   grad = decibel * 10.4;
		    		   paint.setColor(Color.argb(255, 255,0, (int) grad));
		    	   }
		    	  
		    	   else {
		    		   grad = 255-decibel * 10.4;
		    		  
		    		   paint.setColor(Color.argb(255, (int)grad,0, 255));
		    	   }*/
		    	   paint.setStyle(Style.FILL); 
		    	   
		    	
		    	  
		    	   canvas.drawRect(actx,(float)(h-(decibel/4)) , actx + 5,  h, paint);
		       } 
		      /* //Altitude
		       double maxA = 0,minA = 0;
		       for(int i =0; i<data.size(); i++){
		    	   double alti = data.get(i).getAltitude();
		    	   if(alti > maxA){
		    		   maxA = alti;
		    	   }
		    	   if(alti < minA){
		    		   minA = alti;
		    	   }
		       
		       }
		       
		       //Altitude level
		       for(int i =0; i<data.size(); i++){
		    	   double decibel = normalise(data.get(i).getAltitude(),maxA,minA);
		    	   float actx = getXValue(data.get(i).getTime(),width,sTime);
				   height_factor++;
				   //System.out.print(height_factor);
		    	   float h = (height*9/num);
		    	   double grad =0;
		    	   grad = decibel * 2.55;
	    		   
	    		   paint.setColor(Color.argb(255,0, (int)grad, 255-(int)grad));  
		    	  /* if(decibel<=12.5){
		    		   
		    		   grad = decibel * 10.4;
		    		   paint.setColor(Color.argb(255, 255,0, (int) grad));
		    	   }
		    	  
		    	   else {
		    		   grad = 255-decibel * 10.4;
		    		  
		    		   paint.setColor(Color.argb(255, (int)grad,0, 255));
		    	   }*/
		    	  /* paint.setStyle(Style.FILL);
		    	   
		    	
		    	  
		    	   canvas.drawRect(actx,(float)(h-(decibel/4)) , actx + 5,  h, paint);
		       } */
		       if((translateX * 1) < 0) {
		    		translateX = 0;
		    	}

		    	 
		    	else if((translateX * 1) > (scaleFactor - 1) * width) {
		    	translateX = (1 - scaleFactor) * width;
		    	}

		    	if(translateY * 1 < 0) {
		    	translateY = 0;
		    	}

		    	else if((translateY * 1) > (scaleFactor - 1) * height) {
		    		translateY = (1 - scaleFactor) * height;
		    	}

		    	canvas.translate(translateX / scaleFactor, translateY / scaleFactor);
		       canvas.restore();
		       
		      }catch(Exception e){}
		        
		}
	}
	
	private double normalise(double decibel, double max, double min){
		double res;
		res = (decibel-min)*100/(max - min);
		
		return res;
	}
	 private String getActivityData(String act){
		  String res = "";
		 if(act.equals("Unknown")){
			 res = "";}

		 else if(act.equals("Still"))
			  res = "_";
		  else if (act.equals("Foot")){
			  res =  "F";
		  }
		  else if(act.equals("Vehicle")){
			  res = "v";
		  }

		  else if (act.equals("Bus")){
			  res =  "B";
		  }
		  
		  else if (act.equals("Train")){
			  res =  "M";
		  }
		  else if (act.equals("MRT")){
			  res =  "M";
		  }
		  else if (act.equals("Bicycle")){
			  res =  "b";
		  }
		  else if (act.equals("Car")){
			  res =  "c";
		  }
		  else if (act.equals("Walking")){
			  res =  "W";
		  }

		return res;
		
	  }
	  
	  private String getSocioData(String act){
		  String res = "";
		  if(act.equals("speaking"))
		  {
			  res ="S";
		  }
		  else if (act.equals("quite")){
			  res = "_";
		  }
		  else if(act.equals("music")){
			  res="m";
		  }
		  
		  return res;
	  }


	private ArrayList<Node> getGoogleData( List<Node>data,long stime, long etime){
		ArrayList<Node> resAct = new ArrayList<>();



		long temp=0, next=0;
		for(int i = -1; i < data.size(); i++){
			String acty;
			if(i == -1){
				temp = stime;
				acty = "Still:100";
				next = data.get(0).getTime();

			}
			else if(i == data.size()-1){
				next = data.get(data.size()-1).getTime()+300000;
				temp = data.get(i).getTime();
				acty = "Still:100";

			}

			else{
				acty = data.get(i).getActivity();
				temp = data.get(i).getTime();
				if(i<data.size()-1){
					next = data.get(i+1).getTime();
				}
				else{
					next=etime;
				}
			}

			long diff = next-temp;
			long num = (diff/(1000*60))*2;
			if(next>temp && num > 1)
			{
				for(int j = 0; j < num; j++){

					long tym = temp+(2*60*1000*j);
					if(tym >=next)
					{
						break;
					}
					if(!acty.equals("")){
						Node aa = new Node(tym,acty);
						resAct.add(aa);
					}
					Log.i("act",acty);
				}
			}
			else
			{
				if(i >0) {
					if(!data.get(i).getActivity().equals("")) {
						Node aa = new Node(data.get(i).getTime(), data.get(i).getActivity());
						resAct.add(aa);
						Log.i("act", acty);
					}
					//Log.i("act", data.get(i).getActivity());
				}
			}


		}

		/*for(int i=0;i<data.size();i++){
			String acty = data.get(i).getgAct();

				Node aa = new Node(data.get(i).getTime(), acty);
				resAct.add(aa);
				Log.i("act", acty);

		}*/


		return resAct;
	}

	private ArrayList<Node> getGpsData(List<GeoActivity>gpsAct, long stime, long etime){
		ArrayList<Node> resAct = new ArrayList<Node>();

		long temp=0, next=0;
		for(int i = 0; i < gpsAct.size(); i++){

			String acty;
			if(i == -1){
				Log.i("entered",i+"");
				temp = stime;
				acty = "Still:100";
				next = gpsAct.get(0).getTime();

				Log.i(i+"",next+"");
			}
			else if(i == gpsAct.size()-1){
				Log.i("entered_last",i+"");
				next = etime;
				temp = gpsAct.get(i).getTime();
				acty = "Still:100";
				Log.i(i+"",gpsAct.get(i).getgAct());

			}

			else{
				Log.i("entered_middle",i+"");
				acty = gpsAct.get(i).getgAct();
				Log.i("entered_middle",acty);
				temp = gpsAct.get(i).getTime();
				Log.i("middle_temp",temp+"");
				next = gpsAct.get(i+1).getTime();
				Log.i("middle_next",next+"");
			}

			long diff = next-temp;
			long num = (diff/(1000*60))*7;
			if(next>temp && num > 1)
			{

				for(int j = 0; j < num; j++){

					long tym = temp+(7*60*1000*j);
					if(tym >=next)
					{
						break;
					}
					Node aa = new Node(tym,acty);
					resAct.add(aa);
				}
				Log.i(i+"",gpsAct.get(i).getgAct());
			}
			else
			{

				Log.i(i+"",gpsAct.get(i).getgAct());
				Node aa = new Node(gpsAct.get(i).getTime(),gpsAct.get(i).getgAct());
				resAct.add(aa);
			}


		}

		return resAct;
	}



	private float getXValue(long time, int wth, long stime){
			float res;
				long min = (time-stime)/60000;
				res = min *wth/1440;
			
			return res;
		
		}
		private long getXValueForNode(long time, int wth, long stime){
			long res;
				long min = (time-stime)/60000;
				res = min *wth/1440;
			
			return res;
		
		}
}
 
	