package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMMBlock;

/**<p>A {@link Component} to handle memory management via the REST interface.</p>
 * <p>Represents the "[memory name]/mgmt" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Management extends Component {
	
	protected Owner m_owner;

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} being managed by this object. 
	 */
	public Management(ADOMeRestlet adom) {
		super(adom);
		m_owner = new Owner(adom);
	}

	@Override
	public void doGet(Request request, Response response) {  
		String message = "Return the complete memory as OMM-XML-data (or 404 Not Found).";  		
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}
		
	@Override
	public void doPost(Request request, Response response) {
		String message =  "Create a complete memory containing the posted OMM-XML-data (or 409 Conflict).";
		response.setEntity(message, MediaType.TEXT_PLAIN);
		
		return;
	}
	
	@Override
	public void doDelete(Request request, Response response) {
		String message =  "Delete the complete memory (or 404 Not Found).";
		response.setEntity(message, MediaType.TEXT_PLAIN);
		
		return;
	}

	@Override
	public void attach(Router router, String path) {
		super.attach(router, path);
		m_owner.attach(router, path + "/owner");
	}
	
	@Override
	public void detach(Router router) {
		m_owner.detach(router);
		super.detach(router);
	}
	
	
	
	/** <p>A {@link Component} to handle owner information via the REST interface.</p>
	 * <p>Represents the "owner" node in the REST tree and handles HTTP requests going there.</p>
	 */
	protected class Owner extends Component 
	{			
		/** Constructor.
		 * @param adom The {@link ADOMeRestlet} with the owner represented in this object. 
		 */
		public Owner(ADOMeRestlet adom) { super(adom); }

		@Override
		public void doGet(Request request, Response response) 
		{
			OMMBlock ownerBlock = ADOMeRestlet.MemoryAccess.getOwnerBlock(getADOM().getMemoryName());
			if (ownerBlock == null) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return;
			}
			String[] ownerInfo = ownerBlock.getPayloadAsString().split(OMMFactory.OWNER_SEPARATOR);
			
			response.setEntity(ownerInfo[0], MediaType.TEXT_PLAIN); // shows only the name
			HeaderControl.AddAccessControlAllowOrigin(response);
			return;
		}		
		
		@Override
		public void doPut(Request request, Response response) 
		{
			String entity_text = request.getEntityAsText();			
			System.out.println("OWNER: "+entity_text);
			String[] ownerInfo = entity_text.split(OMMFactory.OWNER_SEPARATOR);
			if (ownerInfo.length != 2) {
				response.setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
				return;
			}

			OMMBlock newOwnerBlock = OMMFactory.createOMMOwnerBlock(ADOMeRestlet.MemoryAccess.getMemory(getADOM().getMemoryName()).getHeader(), entity_text);
			System.out.println("changed");
			ADOMeRestlet.MemoryAccess.setOwnerBlock(getADOM().getMemoryName(), newOwnerBlock);
			System.out.println("owner = "+ ADOMeRestlet.MemoryAccess.getOwnerBlock(getADOM().getMemoryName()));
			response.setStatus(Status.SUCCESS_OK);
			
//			Document doc = OMMXMLConverter.generateCompleteBlock(newOwnerBlock, true); // OMMXMLConverter.getXmlDocumentFromString(OMMXMLConverter.getInputStreamFromText(entity_text));
//			Element root = doc.getDocumentElement();
//			if (root.getChildNodes().getLength() == 2)
//			{				
//				OMMBlock oldOwnerBlock = OMMXMLConverter.parseBlock((Element)root.getChildNodes().item(0));
//				OMMBlock newOwnerBlock = OMMXMLConverter.parseBlock((Element)root.getChildNodes().item(1));				
//				OMMBlock currentOwnerBlock = ADOMeRestlet.MemoryAccess.getOwnerBlock(getADOM().getMemoryName());
//				
//				System.out.println("CUR = "+currentOwnerBlock.getPayloadAsString());
//				System.out.println("OLD = "+oldOwnerBlock.getPayloadAsString());
//				System.out.println("NEW = "+newOwnerBlock.getPayloadAsString());				
//				
//				if (oldOwnerBlock.getPayloadAsString().equals(currentOwnerBlock.getPayloadAsString())) // todo: check for access
//				{
//					System.out.println("changed");
//					ADOMeRestlet.MemoryAccess.setOwnerBlock(getADOM().getMemoryName(), newOwnerBlock);
//					System.out.println("owner = "+ ADOMeRestlet.MemoryAccess.getOwnerBlock(getADOM().getMemoryName()));
//					response.setStatus(Status.SUCCESS_OK);
//				}
//				else
//				{
//					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//				}
//			}
//			else
//			{
//				response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			}
			
			HeaderControl.AddAccessControlAllowOrigin(response);
			return;
		}		
	}

}