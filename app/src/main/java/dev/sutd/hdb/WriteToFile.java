package dev.sutd.hdb;


import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WriteToFile {
	private static final String GPSfileName = Environment.getExternalStorageDirectory() +"/GPSTest/Location.kml";
	private static final String GPSfiledir = Environment.getExternalStorageDirectory() +"/GPSTest";
	private static final String ActivityfileName = Environment.getExternalStorageDirectory() +"/GPSTest/Activity.txt";
	private static final String Activityfiledir = Environment.getExternalStorageDirectory() +"/GPSTest";
	private static final String MapfileName = Environment.getExternalStorageDirectory() +"/GPSTest/map.html";
	private static final String Mapfiledir = Environment.getExternalStorageDirectory() +"/GPSTest";
	static final String workMapfileName = Environment.getExternalStorageDirectory() +"/FbFiles/work_map.html";
	static final String travelMapfileName = Environment.getExternalStorageDirectory() +"/FbFiles/travel_map.html";
	private static final String Fbfiledir = Environment.getExternalStorageDirectory() +"/FbFiles";
	public Context context;
	
	public File fout = new File(GPSfileName);
	public File fAct = new File(ActivityfileName);
	public File fMap = new File(MapfileName);
	public File fWMap = new File(workMapfileName);
	public File fWTMap= new File(travelMapfileName);
	//write data to sd card file
	public void writeGPSFile(String write_str) throws IOException{   
	 try{   
		 File dirfile = new File(GPSfiledir);
		 if(!dirfile.exists())
		 {
			 dirfile.mkdir();
		 }

		 //write_str = write_str.concat(begin);
		 //Log.i("text", "writing to file");
	       byte [] bytes = write_str.getBytes();   
	       RandomAccessFile raf = new RandomAccessFile(fout,"rw"); 
	       //raf.seek(fout.length());
	       raf.write(bytes);  
	       //Log.i("text","finished writing");
	       //((Closeable) fout).close();   
	       //UploadToServer us = new UploadToServer();
			//us.uploadToServer();
	       raf.close();
	     }  
	  
	      catch(Exception e){   
	        e.printStackTrace();   
	       }   
	   }

	
	public void writeActivityFile(String write_str) throws IOException{   
		 try{   
			 File dirfile = new File(Activityfiledir);
			 if(!dirfile.exists())
			 {
				 dirfile.mkdir();
			 }

			 //write_str = write_str.concat(begin);
			 //Log.i("text", "writing to file");
		       byte [] bytes = write_str.getBytes();   
		       RandomAccessFile raf = new RandomAccessFile(fAct,"rw"); 
		       //raf.seek(fout.length());
		       raf.write(bytes);  
		       
		       //((Closeable) fout).close();   
		       //UploadToServer us = new UploadToServer();
				//us.uploadToServer();
		       raf.close();
		     }  
		  
		      catch(Exception e){   
		        e.printStackTrace();   
		       }   
		   }


	public void deleteFile(){
		
		fout.delete();
	}
	
public void deleteFbFile(){
		
		fWMap.delete();
	}
public void deleteActFile(){
		
		fAct.delete();
	}
public void deleteMapFile(){
	
	fMap.delete();
}
public void writeMapFile(String write_str) throws IOException{   
	 try{   
		 File dirfile = new File(Mapfiledir);
		 if(!dirfile.exists())
		 {
			 dirfile.mkdir();
		 }

		 //write_str = write_str.concat(begin);
		 //Log.i("text", "writing to file");
	       byte [] bytes = write_str.getBytes();   
	       RandomAccessFile raf = new RandomAccessFile(fMap,"rw"); 
	       //raf.seek(fout.length());
	       raf.write(bytes);  
	       
	       //((Closeable) fout).close();   
	       //UploadToServer us = new UploadToServer();
			//us.uploadToServer();
	       raf.close();
	     }  
	  
	      catch(Exception e){   
	        e.printStackTrace();   
	       }   
	   }

private void writeFbMapFile(String str, int sel) throws IOException{
	
	 try{   
		 File dirfile = new File(Fbfiledir);
		 if(!dirfile.exists())
		 {
			 dirfile.mkdir();
		 }

		 //write_str = write_str.concat(begin);
		 //Log.i("text", "writing to file");
	       byte [] bytes = str.getBytes(); 
	   
	       if(sel==2){
	       RandomAccessFile raf = new RandomAccessFile(fWMap,"rw"); 
	       raf.write(bytes);  
	       raf.close();
	      
	     }  
	       else if (sel ==1){
	    	   RandomAccessFile raf = new RandomAccessFile(fWTMap,"rw"); 
		       raf.write(bytes);  
		       raf.close();
	       }
	 }
	      catch(Exception e){   
	        e.printStackTrace();   
	       }   
}

public void buildFile(String pos, int height, int width) throws IOException{
	
	
	String fileInput = " <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n" +
			"<head>\n" +
			"<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />\n" +
			"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n" +
			"<title>Elderly Location Tracking</title>\n" +
			"<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false\">\n" +
			"</script>\n" +
			"<style type=\"text/css\"> html, body ,#map_canvas { height: 100%;  margin: 0px; padding: 0px; }\n" +
			"</style><script type=\"text/javascript\"> var side_bar_html = \"\";\n" +
			"var gmarkers = [];\n" +
			"var map = null;\n" +
			"function initialize()\n" +
			" {var myOptions = {\n" +
			"zoom: 13,\n" +
			"center: new google.maps.LatLng(1.30000,103.80000),\n" +
			"mapTypeControl: true,\n" +
			"mapTypeControlOptions: {style: google.maps.MapTypeControlStyle.DROPDOWN_MENU},\n" +
			"navigationControl: true,\n" +
			"mapTypeId: google.maps.MapTypeId.ROADMAP}\n" +
			"map = new google.maps.Map(document.getElementById(\"map_canvas\"),myOptions);\n" +
			"google.maps.event.addListener(map, 'click', function() \n" +
			"{infowindow.close();});\n" + pos + 
			"\nvar bounds = new google.maps.LatLngBounds();\n"+
			"for (var i = 0; i < gmarkers.length; i++)\n"+
			"{bounds.extend(gmarkers[i].getPosition());}\n"+
			"map.fitBounds(bounds);"+
			"\ndocument.getElementById(\"side_bar\").innerHTML = side_bar_html;}\n" +
			"var infowindow = new google.maps.InfoWindow(" +
			"{size: new google.maps.Size(150,50)});\n" +
			"function myclick(i)\n" +
			" {google.maps.event.trigger(gmarkers[i], \"click\");}\n" +
			"function createMarker(latlng, name, html)\n" +
			" {var contentString = html;\n" +
			"var marker = new google.maps.Marker({\n" +
			"position: latlng,\n" +
			"map: map,\n" +
			"zIndex: Math.round(latlng.lat()*-100000)<<5});\n" +
			"google.maps.event.addListener(marker, 'click', function()\n" +
			" {infowindow.setContent(contentString);\n" +
			"infowindow.open(map,marker);});\n" +
			"gmarkers.push(marker);\n" +
			"side_bar_html += '<a href=\"javascript:myclick(' + (gmarkers.length-1) + ')\">' + name + '<\\/a><br>';\n" +
			"}\n" +
			"</script>\n" +
			"</head>\n" +
			"<body style=\"margin:0px; padding:0px;\" onload=\"initialize()\"><table border=\"1\">\n" +
			"<tr>\n" +
			"<td>\n" +
			"<div id=\"map_canvas\" style=\"width: 550px; height: 450px\">\n" +
			"</div>\n" +
			"</td>\n" +
			"<td valign=\"top\" style=\"width:100px; color: #4444ff;\">\n" +
			"<div id=\"side_bar\">\n" +
			"</div>\n" +
			"</td>\n" +
			"</tr>\n" +
			"</table>\n" +
			"<noscript>\n" +
			"<p>\n" +
			"<b>JavaScript must be enabled in order for you to use Google Maps.</b>However, it seems JavaScript is either disabled or not supported by your browser." +
			"To view Google Maps, enable JavaScript by changing your browser options, and then try again.</p>\n" +
			" </noscript>\n" +
			"<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\">\n" +
			"</script>\n" +
			" <script type=\"text/javascript\">_uacct = \"UA-162157-1\";urchinTracker();\n" +
			"</script>\n" +
			"</body>\n" +
			"</html>";
	writeMapFile(fileInput);
	//new google.maps.LatLng(1.30000,103.80000)
}

public void buildRound(String pos, StringBuilder path) throws IOException
{String fileInput = "<!DOCTYPE html>\n" +  
"<html>\n"+
  "<head>\n" +
   " <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n"+
    "<meta charset=\"utf-8\">\n"+
    "<title>Circles</title>\n"+
    "<style>\n"+
      "html, body, #map-canvas {\n"+
        "height: 100%;\n"+
        "margin: 0px;\n"+
        "padding: 0px }\n"+
    "</style>\n"+
    "<script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDoa89Swijm5d2QqcUsxHQZfC7c-pYGH-g\"></script>\n"+
    "<script>\n"+

"var citymap = {};\n"+
pos +


"var cityCircle;\n"+
"var line;\n"+
"function initialize() {\n"+
  // Create the map.
  "var mapOptions = {\n"+
    "zoom: 4,\n"+
    "center: new google.maps.LatLng(1.3000,103.8000),\n"+
    "mapTypeId: google.maps.MapTypeId.TERRAIN\n"+
  "};\n"+

  "var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);\n"+

"var bounds = new google.maps.LatLngBounds();\n"+
"for (var city in citymap)\n"+
"{bounds.extend(citymap[city].center);}\n"+
"map.fitBounds(bounds);\n"+
  // Construct the circle for each value in citymap.
  // Note: We scale the area of the circle based on the population.
  "for (var city in citymap) {\n"+
  	"var populationOptions;\n" +
  	"if(citymap[city].population<=15000){\n"+
    "populationOptions = {\n"+
      "strokeColor: '#FF0000',\n"+
      "strokeOpacity: 0.8,\n"+
      "strokeWeight: 2,\n"+
      "fillColor: '#FF0000',\n"+
      "fillOpacity: 0.35,\n"+
      "map: map,\n"+
      "center: citymap[city].center,\n"+
      "radius: Math.sqrt(citymap[city].population) * 4\n"+
    "};}\n"+
      "else {\n"+
      "populationOptions = {\n"+
      "strokeColor: '#0000FF',\n"+
      "strokeOpacity: 0.8,\n"+
      "strokeWeight: 2,\n"+
      "fillColor: '#0000FF',\n"+
      "fillOpacity: 0.35,\n"+
      "map: map,\n"+
      "center: citymap[city].center,\n"+
      "radius: Math.sqrt(citymap[city].population) * 4\n"+
    "};}\n"+
    // Add the circle for this city to the map.
    "cityCircle = new google.maps.Circle(populationOptions);\n"+
  "}\n"+

  path+"\n"+
//"];\n"+
// " var polyline = new google.maps.Polyline({\n"+
//      "path: polylineCoordinates,\n"+
//      "strokeColor: '#00FF00',\n"+
//      "strokeOpacity: 1.0,\n"+
//      "strokeWeight: 2,\n"+
//      "editable: true\n"+
//  "});\n"+
//   "polyline.setMap(map);\n

"}\n"+


"google.maps.event.addDomListener(window, 'load', initialize);\n"+

    "</script>\n"+
  "</head>\n"+
  "<body>\n"+
    "<div id=\"map-canvas\"></div>\n"+
  "</body>\n"+
"</html>\n";
	
	
	writeMapFile(fileInput);
}


public void buildCircle(String pos) throws IOException{
	String fileInput = "<!DOCTYPE html>\n" +  
			"<html>\n"+
			  "<head>\n" +
			   " <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n"+
			    "<meta charset=\"utf-8\">\n"+
			    "<title>Circles</title>\n"+
			    "<style>\n"+
			      "html, body, #map-canvas {\n"+
			        "height: 100%;\n"+
			        "margin: 0px;\n"+
			        "padding: 0px }\n"+
			    "</style>\n"+
			    "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\"></script>\n"+
			    "<script>\n"+

			"var citymap = {};\n"+
			pos +


			"var cityCircle;\n"+
			"var line;\n"+
			"function initialize() {\n"+
			  // Create the map.
			  "var mapOptions = {\n"+
			    "zoom: 4,\n"+
			    "center: new google.maps.LatLng(1.3000,103.8000),\n"+
			    "mapTypeId: google.maps.MapTypeId.TERRAIN\n"+
			  "};\n"+

			  "var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);\n"+

			"var bounds = new google.maps.LatLngBounds();\n"+
			"for (var city in citymap)\n"+
			"{bounds.extend(citymap[city].center);}\n"+
			"map.fitBounds(bounds);\n"+
			  // Construct the circle for each value in citymap.
			  // Note: We scale the area of the circle based on the population.
			  "for (var city in citymap) {\n"+
			  	"var populationOptions;\n" +
			  	"if(citymap[city].population<=55000){\n"+
			    "populationOptions = {\n"+
			      "strokeColor: '#FF0000',\n"+
			      "strokeOpacity: 0.8,\n"+
			      "strokeWeight: 2,\n"+
			      "fillColor: '#FF0000',\n"+
			      "fillOpacity: 0.35,\n"+
			      "map: map,\n"+
			      "center: citymap[city].center,\n"+
			      "radius: Math.sqrt(citymap[city].population) * 2\n"+
			    "};}\n"+
			      "else {\n"+
			      "populationOptions = {\n"+
			      "strokeColor: '#0000FF',\n"+
			      "strokeOpacity: 0.8,\n"+
			      "strokeWeight: 2,\n"+
			      "fillColor: '#0000FF',\n"+
			      "fillOpacity: 0.35,\n"+
			      "map: map,\n"+
			      "center: citymap[city].center,\n"+
			      "radius: Math.sqrt(citymap[city].population) * 2\n"+
			    "};}\n"+
			    // Add the circle for this city to the map.
			    "cityCircle = new google.maps.Circle(populationOptions);\n"+
			  "}\n"+

			  //path+"\n"+
			//"];\n"+
			// " var polyline = new google.maps.Polyline({\n"+
//			      "path: polylineCoordinates,\n"+
//			      "strokeColor: '#00FF00',\n"+
//			      "strokeOpacity: 1.0,\n"+
//			      "strokeWeight: 2,\n"+
//			      "editable: true\n"+
			//  "});\n"+
			//   "polyline.setMap(map);\n

			"}\n"+


			"google.maps.event.addDomListener(window, 'load', initialize);\n"+

			    "</script>\n"+
			  "</head>\n"+
			  "<body>\n"+
			    "<div id=\"map-canvas\"></div>\n"+
			  "</body>\n"+
			"</html>\n";
				
				
			
				
				
	writeMapFile(fileInput);
}


public void buildWrkCircle(String pos) throws IOException{
	String fileInput = "<!DOCTYPE html>\n" +  
			"<html>\n"+
			  "<head>\n" +
			   " <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n"+
			    "<meta charset=\"utf-8\">\n"+
			    "<title>Circles</title>\n"+
			    "<style>\n"+
			      "html, body, #map-canvas {\n"+
			        "height: 100%;\n"+
			        "margin: 0px;\n"+
			        "padding: 0px }\n"+
			    "</style>\n"+
			    "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\"></script>\n"+
			    "<script>\n"+

			"var citymap = {};\n"+
			pos +


			"var cityCircle;\n"+
			"var line;\n"+
			"function initialize() {\n"+
			  // Create the map.
			  "var mapOptions = {\n"+
			    "zoom: 4,\n"+
			    "center: new google.maps.LatLng(1.3000,103.8000),\n"+
			    "mapTypeId: google.maps.MapTypeId.TERRAIN\n"+
			  "};\n"+

			  "var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);\n"+

			"var bounds = new google.maps.LatLngBounds();\n"+
			"for (var city in citymap)\n"+
			"{bounds.extend(citymap[city].center);}\n"+
			"map.fitBounds(bounds);\n"+
			  // Construct the circle for each value in citymap.
			  // Note: We scale the area of the circle based on the population.
			  "for (var city in citymap) {\n"+
			  	"var populationOptions;\n" +
			  	"if(citymap[city].population<=55000){\n"+
			    "populationOptions = {\n"+
			      "strokeColor: '#FF0000',\n"+
			      "strokeOpacity: 0.8,\n"+
			      "strokeWeight: 2,\n"+
			      "fillColor: '#FF0000',\n"+
			      "fillOpacity: 0.35,\n"+
			      "map: map,\n"+
			      "center: citymap[city].center,\n"+
			      "radius: Math.sqrt(citymap[city].population) * 2\n"+
			    "};}\n"+
			      "else {\n"+
			      "populationOptions = {\n"+
			      "strokeColor: '#0000FF',\n"+
			      "strokeOpacity: 0.8,\n"+
			      "strokeWeight: 2,\n"+
			      "fillColor: '#0000FF',\n"+
			      "fillOpacity: 0.35,\n"+
			      "map: map,\n"+
			      "center: citymap[city].center,\n"+
			      "radius: Math.sqrt(citymap[city].population) * 2\n"+
			    "};}\n"+
			    // Add the circle for this city to the map.
			    "cityCircle = new google.maps.Circle(populationOptions);\n"+
			  "}\n"+

			  //path+"\n"+
			//"];\n"+
			// " var polyline = new google.maps.Polyline({\n"+
//			      "path: polylineCoordinates,\n"+
//			      "strokeColor: '#00FF00',\n"+
//			      "strokeOpacity: 1.0,\n"+
//			      "strokeWeight: 2,\n"+
//			      "editable: true\n"+
			//  "});\n"+
			//   "polyline.setMap(map);\n

			"}\n"+


			"google.maps.event.addDomListener(window, 'load', initialize);\n"+

			    "</script>\n"+
			  "</head>\n"+
			  "<body>\n"+
			    "<div id=\"map-canvas\"></div>\n"+
			  "</body>\n"+
			"</html>\n";
				
				
	writeFbMapFile(fileInput,2);
}

public void buildFbRound(String pos, StringBuilder path) throws IOException
{String fileInput = "<!DOCTYPE html>\n" +  
"<html>\n"+
  "<head>\n" +
   " <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n"+
    "<meta charset=\"utf-8\">\n"+
    "<title>Circles</title>\n"+
    "<style>\n"+
      "html, body, #map-canvas {\n"+
        "height: 100%;\n"+
        "margin: 0px;\n"+
        "padding: 0px }\n"+
    "</style>\n"+
    "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\"></script>\n"+
    "<script>\n"+

"var citymap = {};\n"+
pos +


"var cityCircle;\n"+
"var line;\n"+
"function initialize() {\n"+
  // Create the map.
  "var mapOptions = {\n"+
    "zoom: 4,\n"+
    "center: new google.maps.LatLng(1.3000,103.8000),\n"+
    "mapTypeId: google.maps.MapTypeId.TERRAIN\n"+
  "};\n"+

  "var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);\n"+

"var bounds = new google.maps.LatLngBounds();\n"+
"for (var city in citymap)\n"+
"{bounds.extend(citymap[city].center);}\n"+
"map.fitBounds(bounds);\n"+
  // Construct the circle for each value in citymap.
  // Note: We scale the area of the circle based on the population.
  "for (var city in citymap) {\n"+
  	"var populationOptions;\n" +
  	"if(citymap[city].population<=15000){\n"+
    "populationOptions = {\n"+
      "strokeColor: '#FF0000',\n"+
      "strokeOpacity: 0.8,\n"+
      "strokeWeight: 2,\n"+
      "fillColor: '#FF0000',\n"+
      "fillOpacity: 0.35,\n"+
      "map: map,\n"+
      "center: citymap[city].center,\n"+
      "radius: Math.sqrt(citymap[city].population) * 4\n"+
    "};}\n"+
      "else {\n"+
      "populationOptions = {\n"+
      "strokeColor: '#0000FF',\n"+
      "strokeOpacity: 0.8,\n"+
      "strokeWeight: 2,\n"+
      "fillColor: '#0000FF',\n"+
      "fillOpacity: 0.35,\n"+
      "map: map,\n"+
      "center: citymap[city].center,\n"+
      "radius: Math.sqrt(citymap[city].population) * 4\n"+
    "};}\n"+
    // Add the circle for this city to the map.
    "cityCircle = new google.maps.Circle(populationOptions);\n"+
  "}\n"+

  path+"\n"+
//"];\n"+
// " var polyline = new google.maps.Polyline({\n"+
//      "path: polylineCoordinates,\n"+
//      "strokeColor: '#00FF00',\n"+
//      "strokeOpacity: 1.0,\n"+
//      "strokeWeight: 2,\n"+
//      "editable: true\n"+
//  "});\n"+
//   "polyline.setMap(map);\n

"}\n"+


"google.maps.event.addDomListener(window, 'load', initialize);\n"+

    "</script>\n"+
  "</head>\n"+
  "<body>\n"+
    "<div id=\"map-canvas\"></div>\n"+
  "</body>\n"+
"</html>\n";
	
	
	writeFbMapFile(fileInput,1);
}


}




