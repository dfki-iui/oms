package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.routing.Router;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.impl.OMMImpl;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.tools.OMMXMLConverter;

/** <p>A {@link Component} to handle the addition of OMM blocks to a memory via the REST interface.</p>
 * <p>Represents the "block" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Block extends Component {
	
	protected BlockManager   m_blockManager;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the block belongs. 
	 */
	public Block(ADOMeRestlet adom) {
		super(adom);
		m_blockManager = new BlockManager(adom);
	}

	/* (non-Javadoc)
	 * @see de.dfki.adom.Component#doPut(org.restlet.Request, org.restlet.Response)
	 */
	@Override
	public void doPost(Request request, Response response) {
		
		String entity_text = request.getEntityAsText();   // data content of the PUT request
		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		String message = api.postBlock(entity_text);
		
		// Build response
		if (message != null) {
			response.setEntity(message, MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_CREATED);
		}
		else {
			internalError(response, "Block.doPost: Unable to create new block.");
		}
	}
	
	/** Adds a block to the ADOM. 
	 * @param blockAsXMLSnipped An XML String describing the block. 
	 * @return ID of the added block (or null, if block could not be added). 
	 */
	public String post(String blockAsXMLSnipped) {
		
		OMMImpl omm = getADOM().getOMM();
		blockAsXMLSnipped = blockAsXMLSnipped.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		
		String doc_string =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
				"<omm:omm xmlns:omm=\"http://www.w3.org/2005/Incubator/omm/elements/1.0/\"> \n" +
				blockAsXMLSnipped + "\n" +
				"</omm:omm>";
		Document doc;
		Element root = null;
		try {			
			doc = OMMXMLConverter.getXmlDocumentFromString(OMMXMLConverter.getInputStreamFromText(doc_string));
			root = doc.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (root == null) return null;
		
		Element block_element = null;
		NodeList nl = root.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++)
		{
			Object child = nl.item(i);
			if (!(child instanceof Element)) continue;
			if (block_element != null)
				System.out.print("Block snipped with multiple children.");
			block_element = (Element)child;
		}

		OMMBlock block = null;
		try
		{
			block = OMMXMLConverter.parseBlock(block_element);
			block.setID(OMMFactory.getFreeBlockID(omm));
			omm.addBlockWithoutChanges(block, block.getCreator());
		}
		catch(Exception e){e.printStackTrace();}
    
	    // Build response
	    String message;
	    if (block != null) {
	      message = block.getID();
	    }
	    else {
	      return null;
	    }
		
		// A new block may be created - update the blockManager block-ID-list
		m_blockManager.updateSpecificBlocks();
		return message;
	}
	
	/**
	 * Attach this object and its subcomponents to the given router.
	 * @param router The router to attach to 
	 */
	@Override
	public void attach(Router router, String path) {
		super.attach(router, path);
		
				
		m_blockManager.attach(router, path + "/{id}");
	}
	
	/*
	 * Detach this object and its subcomponents from the given router.
	 * @param router
	 */
	@Override
	public void detach(Router router) {
//		for (SpecificBlock spec_blck : m_specificBlockList)
//			spec_blck.detach(router);
		
		m_blockManager.detach(router);
		
		super.detach(router);
	}
	
//	private void updateRoutes() {
//		m_router.detach(this);
//		
//		m_router.attach(m_path, this);
//		m_router.attach(m_path + "/", this);
//	}
	
	/**
	 * Delivers the block manager object of an ADOM.
	 * @return the block manager object.
	 */
	public BlockManager getBlockManager() {
		return m_blockManager;
	}

}
