package models;

public class NodeRectangle {
	
	double left;
	double top;
	double right;
	double bottom;
	int id;
	double latitude;
	double longitude;
	
	public NodeRectangle(double left,
						 double top,
						 double right,
						 double bottom,
						 int id,
						 double latitude,
						 double longitude){
		this.left = left;
		this.top = top;
		this.right =  right;
		this.bottom = bottom;
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double getLeft(){
		return left;
	}
	public double getTop(){
		return top;
	}
	public double getRight(){
		return right;
	}
	public double getBottom(){
		return bottom;
	}
	public int getId(){
		return id;
	}
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	
	public boolean contains(double x, double y){
		boolean res = false;
		if(x >= left && x < right ){
			if(y >=top && y< bottom)
			{
				res = true;
			}
		}
		return res;
	}

}
