package dev.sutd.hdb;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;

public class MapActivity extends Activity {
	static final String MapfileName = Environment.getExternalStorageDirectory() +"/GPSTest/map.html";
	
		private WebView webView;

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.map_activity);

			webView = (WebView) findViewById(R.id.webView1);
			webView.getSettings().setJavaScriptEnabled(true);
		 
			webView.loadUrl("file://"+MapfileName);
		
	 
		}

		
	
}		
	 
