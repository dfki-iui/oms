package de.dfki.adom.rest.nodes;

import java.util.Date;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.adom.rest.Tools;
import de.dfki.omm.impl.OMMBlockImpl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.tools.OMMXMLConverter;
import de.dfki.omm.types.GenericTypedValue;
import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMEntityCollection;
import de.dfki.omm.types.TypedValue;

/** <p>A {@link Component} to handle interaction with a block's payload via the REST interface.</p>
 * <p>Represents the "payload" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Payload extends Component {

	private BlockManager     m_blockManager;
	private PayloadAppend    m_payloadAppend;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the containing block belongs. 
	 * @param blockManager The {@link BlockManager} handling the containing block. 
	 */
	public Payload(ADOMeRestlet adom, BlockManager blockManager) {
		super(adom);
		m_blockManager = blockManager;		
		m_payloadAppend   = new PayloadAppend(adom, blockManager);
	}

	
	
//	@Override
//	public void doGet(Request request, Response response) {
////		String message = "GET Payload of block ";	
////		response.setEntity(message, MediaType.TEXT_PLAIN);
//		
//		doGet(request, response);
//		
//		return;
//	}
	
	@Override
	public void doPut(Request request, Response response) {
		
		doPost(request, response);
	}
		
		
	/**
	 * GET request for payload.
	 * Returns payload and payload encoding as a JSON-string in the format {"payload":"xxxx", "type":"yyyy"}
	 * in the HTTP-response.
	 * @param request
	 * @param response
	 */
	@Override
	public void doGet(Request request, Response response) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 1);

		if (block_id == null) {
			notFoundError(response, "Payload.doGet() when searching for block " + block_id);		
			return;
		}
		
		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		String message = api.getPayload(block_id);
		OMMBlock block = adom.getOMM().getBlock(block_id);
		
		MediaType mimeType = MediaType.TEXT_PLAIN; 

		if (block.getFormat() != null && block.getFormat().getMIMEType() != null)
		{
			String mimeTypeString = block.getFormat().getMIMEType().trim().toUpperCase();
			try 
			{
				mimeType = MediaType.valueOf(mimeTypeString);
	        } 
			catch(IllegalArgumentException ex) {}			
		}
		
		if (message == null)
		{
			notFoundError(response, "Payload.doGet(). No payload found");
			return;
		}
		else
		{
		  response.setEntity(message, mimeType);
		}
		
		HeaderControl.AddAccessControlAllowOrigin(response);
	}
	
	/**
	 * POST request for payload.
	 * Writes the payload given inside the POST request into the payload of the block.
	 * @param request
	 * @param response
	 */
	@Override
	public void doPost(Request request, Response response) {
		// Get request content
		String entity_text = request.getEntityAsText();
		
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 1);

		if (block_id == null) {
			notFoundError(response, "Payload.doPost() when searching for block " + block_id);		
			return;
		}
		
		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		boolean result = api.postPayload(block_id, entity_text);
		
		if (result)
		{
			// update creator/contributor
			OMMBlockImpl block = (OMMBlockImpl)adom.getOMM().getBlock(block_id);
			OMMEntityCollection contrList = (OMMEntityCollection)block.getContributors();
			if (contrList != null && contrList.size() > 0)
			{
				OMMEntityCollection contrList2 = new OMMEntityCollection();
				int counter = 0;
				for(OMMEntity contr : contrList)
				{
					if (counter == contrList.size() - 1)
					{
						contr = new OMMEntity(contr.getType(), contr.getValue(), ISO8601.getISO8601String(new Date()));
					}
					contrList2.add(contr);
					counter++;
				}

				block.setContributors(contrList2);
			}
			else // no contributor -> creator 
			{
				OMMEntity creator = block.getCreator();
				block.setCreator(new OMMEntity(creator.getType(), creator.getValue(), ISO8601.getISO8601String(new Date())));
			}
			
  		String message = "Payload of block " + block_id + " added / changed";
  		response.setEntity(message, MediaType.TEXT_PLAIN);
		}
		else
		{
			notFoundError(response, "Payload.doPost() when searching for OMM block " + block_id);		
		}
	}
	

	
	/**
	 * DELETE request for payload.
	 * Deletes the payload of the block.
	 * @param request
	 * @param response
	 */
	@Override
	public void doDelete(Request request, Response response) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 1);

		if (block_id == null) {
			notFoundError(response, "Payload.doDelete() when searching for block " + block_id);		
			return;
		}

		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		boolean result = api.deletePayload(block_id);
		
		if (result)
		{
  		String message = "Payload of block " + block_id + " removed";
  		response.setEntity(message, MediaType.TEXT_PLAIN);
		}
		else
		{
			notFoundError(response, "Payload.doDelete(). No payload found");
		}
	}
	
//	 * Returns payload and payload type as a JSON-string in the format {"payload":"xxxx", "type":"yyyy"}
	/**
	 * GET request for payload.
	 * Returns the payload as a String block in the HTTP-response.
	 * @param blockID
	 */
	public String get(String blockID) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return null;
		
		// --- Get payload ---
		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
		if (omm_block == null)
			return null;
		
		if (omm_block.getFormat() != null && omm_block.getFormat().getMIMEType().equals("application/xml"))
		{
			Element element = omm_block.getPayloadElement();
			if (element != null)
			{
				if (element.getNodeName().equals(OMMXMLConverter.OMM_NAMESPACE_PREFIX+":payload") && element.getChildNodes().getLength() > 0)
				{
					NodeList nl = element.getChildNodes();
					for(int i = 0; i < nl.getLength(); i++)
					{
						Node node = nl.item(i);
						if (node instanceof Element) 
							return OMMXMLConverter.toXMLFileString(node);
					}					
				}					
				else
					return OMMXMLConverter.toXMLFileString(element);
			}
		}
		
		TypedValue value = omm_block.getPayload();
		if (value != null)
		{
			return (String)value.getValue();
		}
		
		Element element = omm_block.getPayloadElement();
		if (element != null)
		{
			return OMMXMLConverter.toXMLFileString(element);
		}
		
		return null;
		
		//		String type = value.getType();
				
		
		// --- Build result ---
//		JSONObject result = new JSONObject();
//		try {
//			result.put("type", type);
//			result.put("payload", payload);
//						
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return null;
//		}
//			
//		String message = result.toString();
//		return message;
		
		//return payload;
	}
	

	/**
	 * POST request for payload.
	 * Writes the payload given inside the POST request into the payload of the block.
	 * @param blockID
	 * @param payload
	 */
	public boolean post(String blockID, String payload) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return false;
		
		// --- Perform Post (Write new content to payload) ---
		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
		if (omm_block == null)
			return false;
		
		OMMEntity entity = Tools.GetApiEntity();
		TypedValue pl_val = omm_block.getPayload();
		if (pl_val == null)
			pl_val = new GenericTypedValue("none", null); // set "none" as default encoding. alternatives: base64 and uuencode
		pl_val.setValue(payload);		// Keep the encoding, set the new payload.
		omm_block.setPayload(pl_val, entity);
		
		return true;
	}
	
	
	/**
	 * Deletes the payload of the block with the given blockID. Returns true if the delete operation
	 * was successful, false else.
	 * @param blockID
	 * @return true if delete of payload with given blockID successful, false else.
	 */
	public boolean delete(String blockID) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return false;
		
		// --- Perform Delete ---
		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
		if (omm_block == null)
			return false;
		
		TypedValue value = omm_block.getPayload();
		if (value == null)
			return false;
		
		OMMEntity entity = Tools.GetApiEntity();
		omm_block.removePayload(entity);
		return true;
	}
	

	@Override
	public void attach(Router router, String path) {
		super.attach(router, path);
		m_payloadAppend.attach(router, path + "/append");
	}
	
	
	@Override
	public void detach(Router router) {
		m_payloadAppend.detach(router);
		super.detach(router);
	}

}
