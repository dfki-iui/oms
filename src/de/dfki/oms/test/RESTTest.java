package de.dfki.oms.test;

import java.net.HttpURLConnection;
import java.net.URL;

/** Connects to "http://localhost:10082/web/sample/" and tries to retrieve header fields from there, 
 * printing them to the console if successful. */
public class RESTTest
{
	public static void main(String[] args) throws Exception
	{
		URL url = new URL("http://localhost:10082/web/sample/");		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		// List all the response headers from the server.
	    // Note: The first call to getHeaderFieldKey() will implicit send
	    // the HTTP request to the server.
	    for (int i=0; ; i++) 
	    {
	        String headerName = conn.getHeaderFieldKey(i);
	        String headerValue = conn.getHeaderField(i);

	        if (headerName == null && headerValue == null) 
	        {
	            // No more headers
	            break;
	        }
	        if (headerName == null) 
	        {
	        	System.out.println("Version : " +headerValue);
	        }
	        else
	        {
	        	System.out.println(headerName + " : " +headerValue);
	        }
	    }
	}

}
