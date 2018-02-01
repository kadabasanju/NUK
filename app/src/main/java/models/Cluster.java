package models;

public class Cluster {
		private double latitude;
		private double longitude;
		private long time;
		private double accuracy;
		private long duration;
	    private String activity;

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public Cluster(double lat, double longi, long tim,long dura, double accu){
			this.latitude = lat;
			this.longitude = longi;
			this.time = tim;
			this.accuracy = accu;
			this.duration = dura;

		}
		
		public double getLatitude()
		{
			return latitude;
		}
		
		public double getLongitude(){
			return longitude;
		}
		
		public long getTime(){
			
			return time;
		}
		

		
		public double getAccuracy(){
			return accuracy;
			
		}

		public double getDuration(){
			return duration;

		}

		public void setLatitude(double lat){
			this.latitude = lat;
		}

		public void setLongitude(double longi){
			this.longitude = longi;
		}

		public void setTime(long time){
			this.time= time;

		}


	public void setDuration(long dura){
		this.duration= dura;

	}




	public void setAccuracy(double accu){
			this.accuracy = accu;
		}
	
}
