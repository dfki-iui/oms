package de.dfki.adom.rest.nodes;

import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;

/** <p>A {@link Component} to output all block IDs of a memory via the REST interface. </p>
 * <p>Represents the "block_ids" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class BlockIDs extends Component {

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the block information node belongs. 
	 */
	public BlockIDs(ADOMeRestlet adom) {
		super(adom);
	}
	
//	/* (non-Javadoc)
//	 * @see de.dfki.adom.Component#doGet(org.restlet.Request, org.restlet.Response)
//	 */
//	@SuppressWarnings("deprecation")
//	@Override
//	public void doGet(Request request, Response response) {
//
//		String message;
//		ADOM adom = getADOM();
//		OMM omm = adom.getOMM();
//		
//		if (omm == null) {
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			message = "Internal Error in BlockIDs.doGet: No OMM of name \"" +  adom.getMemoryName() + "\" found.";
//			response.setEntity(message, MediaType.TEXT_PLAIN);
//			return;
//		}
//		
//		List<String> block_ids = omm.getAllBlockIDs();
//		
//		if ((block_ids == null) || (block_ids.size() == 0)) {
//			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
//			message = "No Block IDs found for OMM \"" +  adom.getMemoryName() + "\".";
//			response.setEntity(message, MediaType.TEXT_PLAIN);
//			return;
//		}
//		
//		
//		JSONObject result = new JSONObject();
//		try {
//			
//			JSONArray id_arr = new JSONArray();
//			for (String block_id : block_ids) {
//				block_id = URLDecoder.decode(block_id);
//				id_arr.put(block_id);
//			}			
//			result.put("IDs", id_arr);
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			message = "Internal Error converting Block ID Information to JSON.";
//			response.setEntity(message, MediaType.TEXT_PLAIN);
//			return;
//		}
//		
////		System.out.println(result);
//		message = result.toString();
//		response.setEntity(message, MediaType.TEXT_PLAIN);
//		
//		return;
//	}
	
	/**
	 * Delivers the block IDs string of an ADOM.
	 * @param request The Request
	 * @param response The Response
	 */
	@Override
	public void doGet(Request request, Response response) {
	  ADOMeRestlet adom = getADOM();
	  API api = adom.getAPI();
	  String message = api.getBlockIDs();
		
		if (message == null) {
			notFoundError(response, "Toc.doGet()");
			return;
		}
		
		HeaderControl.AddAccessControlAllowOrigin(response);
		
		response.setEntity(message, MediaType.TEXT_PLAIN);
		return;
	}	
	
	/**
	 * Delivers the block IDs string of an ADOM.
	 * @return the block IDs string.
	 */
	public String get() { 
		String message;
		ADOMeRestlet adom = getADOM();
		OMM omm = adom.getOMM();
		
		if (omm == null)
			return null;
		try
		{
			URLEncoder.encode("sample", "UTF-8");
		}
		catch(Exception e){ e.printStackTrace(); return null; }
		
		List<String> block_ids = omm.getAllBlockIDs();
		
		if ((block_ids == null) || (block_ids.size() == 0))
			return null;		
		
		JSONObject result = new JSONObject();
		try {
			
			JSONArray id_arr = new JSONArray();
			for (String block_id : block_ids) 
			{
				OMMBlock block = omm.getBlock(block_id);
				// supress ADOMe config block
				if (block != null && block.getNamespace() != null && block.getNamespace().toString().startsWith(Config.CONFIGURATION_NAMESPACE)) continue;
				block_id = URLEncoder.encode(block_id, "UTF-8");
				id_arr.put(block_id);
			}			
			result.put("IDs", id_arr);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		message = result.toString();
		return message;
	}
}
