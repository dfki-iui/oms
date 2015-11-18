/**
 * 
 */
package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.adom.rest.Tools;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.GenericTypedValue;
import de.dfki.omm.types.TypedValue;


/** <p>A {@link Component} to append text to a block's payload via the REST interface.</p>
 * <p>Represents the "payload/append" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class PayloadAppend extends Component {

	private BlockManager m_blockManager;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the containing block belongs. 
	 * @param blockManager The {@link BlockManager} handling the containing block. 
	 */
	public PayloadAppend(ADOMeRestlet adom, BlockManager blockManager) {
		super(adom);
		m_blockManager = blockManager;
	}

	
	@Override
	public void doGet(Request request, Response response) {
		doPost(request, response);
	}
	
	
	/**
	 * POST request for appending payload.
	 * Appends the payload given inside the POST request to the payload of the block.
	 * @param request
	 * @param response
	 */
	@Override
	public void doPost(Request request, Response response) {
		String block_id = getBlockID(m_blockManager, request, 2);    // .../id/payload/append
																	 // id is 2 positions before append-command

		String current_payload_string = m_blockManager.getPayload().get(block_id);
		if (current_payload_string == null) 
			current_payload_string = "";
		
		String payload_string_to_add  = request.getEntityAsText();
		if (payload_string_to_add == null) 
			payload_string_to_add = "";
		
		String new_payload_string = current_payload_string + payload_string_to_add;
		
		OMMBlock omm_block = getOMMBlock(m_blockManager, request, 2);  // see above
		TypedValue payload = omm_block.getPayload();
		if (payload == null)
			payload = new GenericTypedValue("none", "");  // set "none" as default encoding. alternatives: base64 and uuencode

		payload.setValue(new_payload_string);		// Keep the encoding, set the new payload.
		omm_block.setPayload(payload, Tools.GetEntity(request));

		String message = "\"" + payload_string_to_add + "\"" + " appended to payload of block "+ block_id + ".";
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
	}
	
	
	/** Appends text to an existing payload (or creates a new payload based on the text to add). 
	 * @param blockID ID of the block to whose payload text is added.
	 * @param payloadToAdd
	 */
	public void post(String blockID, String payloadToAdd) {
		
		String current_payload_string = m_blockManager.getPayload().get(blockID);
		String new_payload_string     = current_payload_string + payloadToAdd;
		
		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
		TypedValue payload = omm_block.getPayload();
		if (payload == null)
			payload = new GenericTypedValue("none", null);  // set "none" as default encoding. alternatives: base64 and uuencode
		
		payload.setValue(new_payload_string);		// Keep the encoding, set the new payload.
		omm_block.setPayload(payload, Tools.GetApiEntity());

		return;
	}
}
