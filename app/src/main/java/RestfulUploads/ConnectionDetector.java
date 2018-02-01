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
	 

	        public void isNetworkAvailable(final Handler handler, final int timeout) {
	            // ask fo message '0' (not connected) or '1' (connected) on 'handler'
	            // the answer must be send before before within the 'timeout' (in milliseconds)

	            new Thread() {
	                private boolean responded = false;   
	                @Override
	                public void run() { 
	                    // set 'responded' to TRUE if is able to connect with google mobile (responds fast) 
	                    new Thread() {      
	                        @Override
	                        public void run() {
	                            HttpGet requestForTest = new HttpGet("http://202.94.70.41");
	                            try {
	                                new DefaultHttpClient().execute(requestForTest); // can last...
	                                responded = true;
	                                
	                            } 
	                            catch (Exception e) {
									System.out.println(e);
	                            }
	                        } 
	                    }.start();

	                    try {
	                        int waited = 0;
	                        while(!responded && (waited < timeout)) {
	                            sleep(100);
	                            if(!responded ) { 
	                                waited += 100;
	                            }
	                        }
	                    } 
	                    catch(InterruptedException e) {} // do nothing 
	                    finally { 
	                        if (!responded) { handler.sendEmptyMessage(0); } 
	                        else { handler.sendEmptyMessage(1); }
	                    }
	                }
	            }.start();
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


