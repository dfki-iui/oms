package de.dfki.oms.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** A small test client that connects to the OMS query interface, sends a query and displays the result. */
public class OMSQueryClient
{
	
	/** Sends a query to the server after loading it from a file. 
	 * 
	 * @param args Arguments (unused). 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		File f = new File("C:\\Users\\jeha01\\Desktop\\sample.json");
		URL url = new URL("http://localhost:10082/query/");
		String result = query(f, url);
		System.out.println("Result: "+result);
	}
	
	/** Converts a String path into a file object and queries its contents. 
	 * 
	 * @param queryFile Filepath as String. 
	 * @param omsURL {@link URL} of the OMS. 
	 * @return Query result as String. 
	 * @throws IOException
	 */
	public static String query(String queryFile, URL omsURL) throws IOException
	{
		File f = new File(queryFile);
		return query(f, omsURL);
	}
	
	/** Opens and buffers a file in order to query its contents.
	 * 
	 * @param queryFile {@link File} containing the query.
	 * @param omsURL {@link URL} of the OMS. 
	 * @return Query result as String. 
	 * @throws IOException
	 */
	public static String query(File queryFile, URL omsURL) throws IOException
	{
		// read file to buffer
		BufferedReader in = new BufferedReader(new FileReader(queryFile));
		String read = null;
		String buffer = "";
		while((read = in.readLine()) != null)
		{
			buffer += read;
		}
		in.close();

		return query(buffer.getBytes(), omsURL);
	}
	
	/** Sends a query to the OMS via HTTP.
	 * 
	 * @param queryFileArray The JSON query as a {@link byte[]}.
	 * @param omsURL {@link URL} of the OMS. 
	 * @return Query result as String. 
	 * @throws IOException
	 */
	public static String query(byte[] queryFileArray, URL omsURL) throws IOException
	{				
		// open url connection and POST file to OMS
		HttpURLConnection httpCon = (HttpURLConnection) omsURL.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/json");
		httpCon.setRequestProperty("Content-Length", "" + Integer.toString(queryFileArray.length));
		httpCon.getOutputStream().write(queryFileArray);
		httpCon.getOutputStream().close();
		
		// response code != 200 --> error
		if (httpCon.getResponseCode() != 200)
		{
			System.out.println("Responsecode: "+httpCon.getResponseCode() + " " + httpCon.getResponseMessage());
			return null;
		}
		
		// ok -> read response
		BufferedInputStream isr = new BufferedInputStream(httpCon.getInputStream());
		byte[] contents = new byte[1024];
        
        int bytesRead=0;
        StringBuilder result = new StringBuilder();
       
        while( (bytesRead = isr.read(contents)) != -1)
        {               
        	result.append(new String(contents, 0, bytesRead));
        }
        isr.close();

		return result.toString();
	}
}
