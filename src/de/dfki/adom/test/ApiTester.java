package de.dfki.adom.test;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.omm.tools.OMMActionResultType;

/** Provides a test suite for the REST API. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
@Deprecated
public class ApiTester {

	ADOMeRestlet m_adom;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which to connect for testing. 
	 */
	public ApiTester(ADOMeRestlet adom) {
		m_adom = adom;
	}

	
	/** <p>A test suite. Tries to retrieve data from the API, save data to the API and finally delete it.  </p>
	 * <p>The results are saved as "test_result.xml". </p>
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public void test() throws JSONException, IOException {
		API api = m_adom.getAPI();
		String snipped;
		
		System.out.println("--- Test API.getFeatureNegotiation() ---");
		String features = api.getFeatureNegotiation(false);
		System.out.println(features);
		System.out.println();
		
		System.out.println("--- Test API.getToC() ---");
		String toc = api.getToC();
		System.out.println(toc);
		System.out.println();
		
		System.out.println("--- Test API.getBlockIDs() ---");
		String block_ids = api.getBlockIDs();
		JSONObject block_ids_obj = new JSONObject(block_ids);
		JSONArray id_arr = (JSONArray) block_ids_obj.get("IDs");
		for (int i=0; i<id_arr.length(); i++)
			System.out.print(id_arr.getString(i) + " ");
		System.out.println();
		System.out.println();
		
		System.out.println("--- Test API.postBlock() ---");
		snipped = Snipped.block();
		String block_id = api.postBlock(snipped);
		System.out.println(block_id);
		System.out.println();
		
		System.out.println("--- Test API.deleteBlock() ---");
		OMMActionResultType deleted = api.deleteBlock("2");
		System.out.println(deleted);
		System.out.println();
		
		System.out.println("--- Test API.getMeta() ---");
		String meta = api.getMeta(block_id);
		System.out.println(meta);
		System.out.println();
				
		System.out.println("--- Test API.getPayload() ---");
		String payload = api.getPayload(block_id);
		System.out.println(payload);
		System.out.println();

		System.out.println("--- Test API.deletePayload() ---");
		boolean pl_deleted = api.deletePayload(block_id);
		System.out.println(pl_deleted);
		System.out.println();
		
		System.out.println("--- Test API.postPayload() ---");
		snipped = Snipped.payload();
		boolean pl_written = api.postPayload(block_id, snipped);
		System.out.println(pl_written);
		System.out.println();
		
		
		// Save memory
		m_adom.save("test_result.xml");
		
		return;
	}
	
}
