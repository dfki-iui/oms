/**
 * 
 */
package de.dfki.adom.rest.nodes;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.adom.rest.Tools;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.tools.OMMXMLConverter;

/** <p>A {@link Component} to handle the output of meta information about a block via the REST interface (in default format: XML).</p>
 * <p>Represents the "meta" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class MetaInfo extends Component {

	protected BlockManager    	m_blockManager;
	protected MetaInfoJSON		m_metaInfoJson;
	protected MetaInfoXML			m_metaInfoXml;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the meta information belongs. 
	 * @param blockManager The {@link BlockManager} of the block for which this meta information is valid. 
	 */
	public MetaInfo(ADOMeRestlet adom, BlockManager blockManager) {
		super(adom);
		m_blockManager = blockManager;		
		m_metaInfoJson = new MetaInfoJSON(adom);
		m_metaInfoXml = new MetaInfoXML(adom);
	}

	@Override
	public void attach(Router router, String path) {
		super.attach(router, path);
		m_metaInfoJson.attach(router, path + "/json");
		m_metaInfoXml.attach(router, path + "/xml");
	}
	
	/**
	 * GET request for the meta information.
	 * Returns meta information as OMM-XML-snippet in the HTTP-response.
	 * @param request
	 * @param response
	 */
	@Override
	public void doGet(Request request, Response response) {

		// if request accepts format JSON, return that (default: XML)
		boolean requestForJson = false;
		if (request.getHeaders().getValues("Accept").toLowerCase().contains("json")) requestForJson = true;

		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 1);
		if (block_id == null) {
			notFoundError(response, "MetaInfo.doGet(), searching for block " + block_id);		
			return;
		}
		
		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		
		String message = null;
		if (requestForJson) message = api.getMetaAsJson(block_id);
		else message = api.getMeta(block_id);
		if (message == null)
		{
			notFoundError(response, "MetaInfo.doGet(). No payload found");
			return;
		}
		
		HeaderControl.AddAccessControlAllowOrigin(response);
		if (requestForJson) response.setEntity(message, MediaType.APPLICATION_JSON);
		else response.setEntity(message, MediaType.APPLICATION_XML);
	}
	
	
	
	/**
	 * Gets the meta information of the block with the given blockID.
	 * @param blockID
	 * @return the meta information of the block identified by blockID, as an XML String.
	 */
	public String get(String blockID) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return null;

		// --- Get meta info ---
		ADOMeRestlet adom = this.getADOM();
		OMM omm = adom.getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
				
		String message = OMMXMLConverter.toXMLFileString(OMMXMLConverter.generateCompleteBlock(omm_block, false));

		return message;
	}
	
	/**
	 * Gets the meta information of the block with the given blockID.
	 * @param blockID
	 * @return the meta information of the block identified by blockID, as a JSON String.
	 */
	public String getJson(String blockID) {

		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return null;

		// --- Get meta info ---
		ADOMeRestlet adom = this.getADOM();
		OMM omm = adom.getOMM();
		OMMBlock omm_block = omm.getBlock(blockID); 
		
		return omm_block.getJsonRepresentation();
	}

	@Override
	public void detach(Router router) {
		m_metaInfoJson.detach(router);
		super.detach(router);
	}
	
	
	
	/** <p>A {@link Component} to handle the output of meta information about a block via the REST interface (in JSON format).</p>
	 * <p>Represents the "meta/json" node in the REST tree and handles HTTP requests going there.</p>
	 * 
	 * @author xekl01
	 */
	protected class MetaInfoJSON extends Component 
	{			
		/** Constructor.
		 * @param adom The {@link ADOMeRestlet} to which the meta information belongs. 
		 */
		public MetaInfoJSON(ADOMeRestlet adom) { super(adom); }

		@Override
		public void doGet(Request request, Response response) 
		{
			// --- Get Block ID ---
			List<String> blockIdList = m_blockManager.getBlockIdList();
			String block_id = Tools.GetBlockId(request, blockIdList, 2);
			if (block_id == null) {
				notFoundError(response, "MetaInfo.doGet(), block was not found.");		
				return;
			}
			
			ADOMeRestlet adom = getADOM();
			API api = adom.getAPI();
			
			String message = api.getMetaAsJson(block_id); 
			if (message == null)
			{
				notFoundError(response, "MetaInfo.doGet(). No payload found");
				return;
			}
			
			HeaderControl.AddAccessControlAllowOrigin(response);
			response.setEntity(message, MediaType.APPLICATION_JSON);
		}		
		
	}
	

	/** <p>A {@link Component} to handle the output of meta information about a block via the REST interface (in XML format explicitly).</p>
	 * <p>Represents the "meta/xml" node in the REST tree and handles HTTP requests going there.</p>
	 * 
	 * @author xekl01
	 */
	protected class MetaInfoXML extends Component 
	{			
		/** Constructor.
		 * @param adom The {@link ADOMeRestlet} to which the meta information belongs. 
		 */
		public MetaInfoXML(ADOMeRestlet adom) { super(adom); }

		@Override
		public void doGet(Request request, Response response) 
		{
			// --- Get Block ID ---
			List<String> blockIdList = m_blockManager.getBlockIdList();
			String block_id = Tools.GetBlockId(request, blockIdList, 2);
			if (block_id == null) {
				notFoundError(response, "MetaInfo.doGet(), block was not found.");		
				return;
			}
			
			ADOMeRestlet adom = getADOM();
			API api = adom.getAPI();
			
			String message = api.getMeta(block_id); 
			if (message == null)
			{
				notFoundError(response, "MetaInfo.doGet(). No payload found");
				return;
			}
			
			HeaderControl.AddAccessControlAllowOrigin(response);
			response.setEntity(message, MediaType.APPLICATION_XML);
		}		
		
	}
	
}
