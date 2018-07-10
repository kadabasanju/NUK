package RestfulUploads;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;


public class ConnectionDetector {	     
	    private Context _context;
	   
	     
	    public ConnectionDetector(Context context){
	        this._context = context;
	    }

	     private static final String TAG_SUCCESS = "success";

	     //JSONParser jsonParser = new JSONParser();

	     public Boolean executeTask() throws InterruptedException, ExecutionException{
	    	 CreateNewActivity task = new CreateNewActivity();
	    	 task.execute();
	    	 boolean res;
	    	 boolean[] result = task.get();
	    	 res = result[0];
	    	 return res;
	          
	     }
	     /**
	      * Background Async Task to Create new product
	      * */
	     class CreateNewActivity extends AsyncTask<String,String, boolean[]> {

	         /**
	          * Before starting background thread Show Progress Dialog
	          * */
	         @Override
	         protected void onPreExecute() {
	             super.onPreExecute();
	            
	         }

	         
	         /**
	          * After completing background task Dismiss the progress dialog
	          * **/
	         protected void onPostExecute(boolean[] result) {
	             // dismiss the dialog once done
	        	 super.onPostExecute(result);
	             
	         }

			@Override
			protected boolean[] doInBackground(String... params) {
				// TODO Auto-generated method stub
			
				 boolean available = true; 
			    try {    
			    	InetAddress addr = InetAddress.getByName("google.com");
			    	int port = 443;
			    	SocketAddress sockaddr = new InetSocketAddress(addr, port);

			    	Socket s = new Socket();
			    	s.connect(sockaddr,3000);
			    	
			    	
			        if (s.isConnected())
			        { s.close();    
			        }               
			        } 
			    catch (UnknownHostException e) 
			        { // unknown host 
			        available = false;
			      
			        } 
			    catch (IOException e) { // io exception, service probably not running 
			        available = false;
			      //  s = null;
			        } 
			    catch (NullPointerException e) {
			        available = false;
			        //s=null;
			    }

			    boolean[] con = new boolean[]{available};
			    return con;   


	     }
	     
	    }
}


