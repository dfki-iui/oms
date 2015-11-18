package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.omm.interfaces.OMMHeader;
import de.dfki.omm.tools.OMMXMLConverter;


/** <p>A {@link Component} to handle the retrieval of the object memory's header via the REST interface.</p>
 * <p>Represents the "header" node in the REST tree and handles HTTP requests going there.</p>
 *  
 * @author Jens Haupert
 * @organization DFKI
 */
public class Header extends Component {
	
	private BlockManager   m_blockManager;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} this header describes. 
	 */
	public Header(ADOMeRestlet adom) {
		super(adom);
		m_blockManager = new BlockManager(adom);
	}

	/* (non-Javadoc)
	 * @see de.dfki.adom.Component#doPut(org.restlet.Request, org.restlet.Response)
	 */
	@Override
	public void doGet(Request request, Response response) {

		ADOMeRestlet adom = getADOM();
		OMMHeader header = adom.getOMM().getHeader();
		String message = OMMXMLConverter.toXMLFileString(OMMXMLConverter.generateHeaderDocument(header));
		
		response.setEntity(message, MediaType.APPLICATION_XML);
	}
	
	
	/**
	 * Attach this object and its subcomponents to the given router.
	 * @param router
	 */
	@Override
	public void attach(Router router, String path) {
		super.attach(router, path);
		m_blockManager.attach(router, path + "/header");
	}
	

	/**
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
