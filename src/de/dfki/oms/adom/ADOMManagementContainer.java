package de.dfki.oms.adom;

import java.io.File;
import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Router;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.interfaces.OMMHeader;
import de.dfki.omm.tools.OMMXMLConverter;
import de.dfki.omm.types.OMMEntity;
import de.dfki.oms.history.OMMVersionManager;
import de.dfki.oms.webapp.OMSOMMHandler;

/** Handles the "[host]/mgmt" node and its subnodes in the REST interface. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class ADOMManagementContainer extends Application {
	
	@Override
	public Restlet createInboundRoot() { 
		Router router = new Router(getContext()); 
		
		ADOMCloneMemoryComponent cloneMemory = new ADOMCloneMemoryComponent();
		router.attach("/cloneMemory", cloneMemory);
		
		ADOMCreateMemoryComponent createMemory = new ADOMCreateMemoryComponent();
		router.attach("/createMemory", createMemory);
		
		ADOMGetMemoryList memoryList = new ADOMGetMemoryList();
		router.attach("/memoryList", memoryList);

		return router;
	}
	
	/**
	 * Restlet for the node [host]/mgmt/memoryList where a GET request is answered with an array of Strings, 
	 * containing the names of all memories on the OMS (as a JSON object).
	 */
	public class ADOMGetMemoryList extends Restlet
	{
		@Override  
		public void handle(Request request, Response response) {  
			super.handle(request, response);
			
			HeaderControl.AddAccessControlAllowOrigin(response);
			
			Method method = request.getMethod();
			
			// Dispatch the request methods
			if (method.equals(Method.GET))
			{
				JsonNodeFactory jnf = JsonNodeFactory.instance;
				ArrayNode root = jnf.arrayNode();
				
				for(String memory : OMMVersionManager.getAvailableMemories())
				{
					root.add(jnf.textNode(memory));
				}
				
				ObjectMapper om = new ObjectMapper();			
				try
				{
					String message = om.writeValueAsString(root);
					response.setEntity(message, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				}
				catch(Exception e) { e.printStackTrace(); response.setStatus(Status.SERVER_ERROR_INTERNAL);}
			}
		}
	}
	
	/**
	 * <p>Restlet for the node [host]/mgmt/createMemory to which an XML string describing a memory can be POSTed
	 * in order to create that memory as a fresh and empty OMM on the OMS.</p>
	 * <p>The XML String must contain a memory header and an owner block.</p>
	 */
	public class ADOMCreateMemoryComponent extends Restlet
	{
		@Override  
		public void handle(Request request, Response response) {  
			
			super.handle(request, response); 
			HeaderControl.AddAccessControlAllowOrigin(response);
			
			Method method = request.getMethod();
			
			// Dispatch the request methods
			if (method.equals(Method.POST))
			{
				String entity_text = request.getEntityAsText(); 
				Document doc = OMMXMLConverter.getXmlDocumentFromString(OMMXMLConverter.getInputStreamFromText(entity_text));
				Element root = doc.getDocumentElement();
				
				OMMHeader header = OMMXMLConverter.parseHeader(OMMXMLConverter.findChild(root, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":header"));
				OMMBlock ownerBlock = OMMXMLConverter.parseBlock(OMMXMLConverter.findChild(root, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":block"));				
				
				String memoryURL = header.getPrimaryID().getValue().toString();
				String memoryName = OMMXMLConverter.getMemoryName(memoryURL);	
				OMMEntity entity = OMMEntity.getDummyEntity();
				
				if (OMSOMMHandler.createNewMemory(memoryName, new File(OMSOMMHandler.getMemoryPath(memoryName)), entity, memoryURL, header, ownerBlock)) 
					response.setStatus(Status.SUCCESS_CREATED);
				else
					response.setStatus(Status.CLIENT_ERROR_CONFLICT);
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			}
			
			return;
		} 
	}
	
	/**
	 * Restlet for the node [host]/mgmt/cloneMemory to which an XML string containing a memory can be POSTed
	 * in order to clone that memory with all blocks and data as a new memory on the OMS.
	 * 
	 * @author xekl01
	 */
	public class ADOMCloneMemoryComponent extends Restlet
	{
		@Override  
		public void handle(Request request, Response response) {  
			super.handle(request, response); 

			HeaderControl.AddAccessControlAllowOrigin(response);
			
			Method method = request.getMethod();
			
			// Dispatch the request methods
			if (method.equals(Method.POST)) {

				// convert string input to XML tree
				String entity_text = request.getEntityAsText(); 
				
				Document doc = OMMXMLConverter.getXmlDocumentFromString(OMMXMLConverter.getInputStreamFromText(entity_text));
				Element root = doc.getDocumentElement();
				
				// get header, owner, and blocks
				OMMHeader header = null;
				OMMBlock ownerBlock = null;
				ArrayList<OMMBlock> blocks = new ArrayList<OMMBlock>();
				NodeList nodes = root.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node instanceof Element) {
						Element element = (Element) node;
						switch (element.getNodeName()) {
							case (OMMXMLConverter.OMM_NAMESPACE_PREFIX+":header"):
								header = OMMXMLConverter.parseHeader(element);
								break;
							case (OMMXMLConverter.OMM_NAMESPACE_PREFIX+":block"):
								if (element.getAttributes().item(0).getTextContent().equals(OMMFactory.OWNER_BLOCK_ID))
									ownerBlock = OMMXMLConverter.parseBlock(element);
								else {
									blocks.add(OMMXMLConverter.parseBlock(element));
								}
								break;
							default:
								break;
						}
					}
				}

				// setup remaining OMM data
				OMMEntity entity = OMMEntity.getDummyEntity();
				if (ownerBlock != null) entity = ownerBlock.getCreator();
				String memoryURL = header.getPrimaryID().getValue().toString();
				String memoryName = OMMXMLConverter.getMemoryName(memoryURL);

				// create cloned memory
				if (OMSOMMHandler.cloneMemory(memoryName, new File(OMSOMMHandler.getMemoryPath(memoryName)), entity, memoryURL, header, ownerBlock, blocks)) 
					response.setStatus(Status.SUCCESS_CREATED);
				else
					response.setStatus(Status.CLIENT_ERROR_CONFLICT);
			}
			else response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);

			return;
		} 
	}
	
}
