package de.dfki.oms.test;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import de.dfki.omm.impl.rest.OMMRestImpl;
import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.OMMEntity;

/** Tests deleting all blocks of a known memory via {@link OMMRestImpl}. */
@Deprecated
public class QueryTester 
{
	/** Starts the test by calling {@link #deleteAllBlocks} with preset parameters.
	 * @param args Argumens (unused).
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		// retrieve all memories
		//String memories = getOMSMemories("localhost", 10082);
		//System.out.println(memories); //<-- JSON string
		
		// delete all blocks
		deleteAllBlocks("localhost", 10082, "schunk_roboterarm");
	}

	/**
	 * Deletes all blocks in the given memory.
	 * 
	 * @param omsServer Host address of the OMS. 
	 * @param omsPort Port of the OMS.
	 * @param memoryName Name of the memory. 
	 * @throws Exception
	 */
	public static void deleteAllBlocks(String omsServer, int omsPort, String memoryName) throws Exception
	{
		final String request = "http://"+omsServer+":"+omsPort+"/rest/"+memoryName;
		final OMMEntity entity = new OMMEntity("internal", "", ISO8601.getISO8601String(new Date()));
		
		OMMRestImpl omm = new OMMRestImpl(request);
		for(String blockID : omm.getAllBlockIDs())
		{
			omm.removeBlock(blockID, entity);
		}
	}
	
	/** Gets all known memories on the OMS by sending a query to the query interface without any condition. 
	 * 
	 * @param omsServer Host address of the OMS. 
	 * @param omsPort Port of the OMS.
	 * @return List of all memories on the OMS. 
	 * @throws Exception
	 */
	public static String getOMSMemories(String omsServer, int omsPort) throws Exception
	{
		final String request = "http://"+omsServer+":"+omsPort+"/query/";
		
		final String query = "{ \"select\" : \"omm\" }"; // query without any condition
			
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("Content-Type", "application/json"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", "" + Integer.toString(query.getBytes().length));
		connection.setUseCaches (false);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		
		wr.writeBytes(query);
		wr.flush();
		wr.close();		
		
		int status = connection.getResponseCode();
		
		if (status == 200)
		{
			InputStream is = connection.getInputStream();
			String myString = IOUtils.toString(is, "UTF-8");
			connection.disconnect();
			return myString;			
		}
		else 
			System.out.println("HTTP Error Code: "+status);
		connection.disconnect();
		return null;
	}
}
