package de.dfki.adom.rest.nodes;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.adom.rest.Tools;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;

/** A blueprint offering basic functionality for nodes of the REST interface, like handling HTTP commands and issuing errors.
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Component extends Restlet {

	private String m_path = null;
	private ADOMeRestlet m_adom   = null;
	
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} for which this component offers functionality. 
	 */
	public Component(ADOMeRestlet adom) {
		super();
		m_adom = adom;
	}

	
	/** Retrieves the {@link ADOMeRestlet} for which this component offers functionality. 
	 * @return The object memory as {@link ADOMeRestlet}. 
	 */
	public ADOMeRestlet getADOM() {
		return m_adom;
	}

	
	/* (non-Javadoc)
	 * @see org.restlet.Restlet#handle(org.restlet.Request, org.restlet.Response)
	 */
	@Override  
	public void handle(Request request, Response response) {  
		super.handle(request, response);  // Mandatory!!!

		Method method = request.getMethod();

		// Dispatch the request methods
		if (method.equals(Method.GET))
			doGet(request, response);
		else if (method.equals(Method.PUT))
			doPut(request, response);
		else if (method.equals(Method.POST))
			doPost(request, response);
		else if (method.equals(Method.DELETE))
			doDelete(request, response);
		else {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		
		return;
	}

	
	/**
	 * Handles the GET requests. Default: doMethodNotAllowed().
	 * If you want to implement: @Override
	 * @param request The request
	 * @param response The response
	 */
	public void doGet(Request request, Response response) {
		doMethodNotAllowed(request, response);		
		return;
	}
	
	
	/**
	 * Handles the POST requests. Default: doMethodNotAllowed().
	 * If you want to implement: @Override
	 * @param request The request
	 * @param response The response
	 */
	public void doPost(Request request, Response response) {  
		doMethodNotAllowed(request, response);
		return;
	}

	
	/**
	 * Handles the PUT requests. Default: doMethodNotAllowed().
	 * If you want to implement: @Override
	 * @param request The request
	 * @param response The response
	 */
	public void doPut(Request request, Response response) {  
		doMethodNotAllowed(request, response);
		return;
	}

	
	/**
	 * Handles the DELETE requests. Default: doMethodNotAllowed().
	 * If you want to implement: @Override
	 * @param request The request
	 * @param response The response
	 */
	public void doDelete(Request request, Response response) {  
		doMethodNotAllowed(request, response);
		
		return;
	}

	
	/**
	 * Handles request types that are not allowed for the component.
	 * @param request The request
	 * @param response The response
	 */
	public void doMethodNotAllowed(Request request, Response response) {
		Method method = request.getMethod();
		String error_str = this.getClass().getName() + ": " + method.getName() + " request type not allowed.";
		System.err.println(error_str);
		response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, error_str);
		
		return;
	}

	
	/**
	 * Delivers the block ID string of the block associated with the Component.
	 * @param blockManager The handling BlockManager
	 * @param request The request
	 * @param positionFromEnd Position of the block identifier in the REST request, counted from the end of the URL.
	 * @return Component's block ID string.
	 */
	public String getBlockID(BlockManager blockManager, Request request, int positionFromEnd) {
		List<String> blockIdList = blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, positionFromEnd);
		
		return block_id;
	}

	
	/**
	 * Delivers the OMMBlock-object of the block associated with the Component.
	 * @param blockManager The handling BlockManager
	 * @param request The request
	 * @param positionFromEnd Position of the block identifier in the REST request, counted from the end of the URL.
	 * @return Component's OMMBlock-object.
	 */
	public OMMBlock getOMMBlock(BlockManager blockManager, Request request, int positionFromEnd) {
		List<String> blockIdList = blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, positionFromEnd);

		if (block_id == null)	
			return null;

		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(block_id);

		return omm_block;
	}

	
	/**
	 * Delivers the OMMBlock-object of the block associated with the Component.
	 * @param blockManager The handling BlockManager
	 * @param blockID as String
	 * @return Component's OMMBlock-object.
	 */
	public OMMBlock getOMMBlock(BlockManager blockManager, String blockID) {
		// --- Get Block ID ---
		List<String> blockIdList = blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return null;

		// --- Get payload ---
		OMM omm = this.getADOM().getOMM();
		OMMBlock omm_block = omm.getBlock(blockID);
		
		return omm_block;
	}
	
	
	/**
	 * Attach this Component to the given router at the given path plus at the given path plus a trailing "/".
	 * @param router The router to attach to
	 * @param path as String
	 */
	public void attach(Router router, String path)
	{
		m_path = path;
		
		router.attach(path, this);//, ACLSecurity.getAuthenticator(getContext(), this, verifier));
		router.attach(path + "/", this);//, ACLSecurity.getAuthenticator(getContext(), this, verifier));
	}
	
	
	/**
	 * Detach this Component from the given router.
	 * @param router The router to attach to
	 */
	public void detach(Router router) {	
		router.detach(this);
	}
	

	/**
	 * Returns the path of the component. Set by the attach()-Method.
	 * @return The path of the component. Set by the attach()-Method.
	 */
	public String getPath() {
		return Config.getBaseURL() + m_path;
	}
	
	
	/** Changes a response into an "internal error" (500) with respective status code and message. 
	 * @param response The response to change into an error message.
	 * @param reason The reason for or explanation of the error. 
	 */
	protected void internalError(Response response, String reason) {
		response.setStatus(Status.SERVER_ERROR_INTERNAL);
		String message = "Internal error in " + reason;
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}
	
	
	/** Changes a response into a "not found" error (404) with respective status code and message. 
	 * @param response The response to change into an error message.
	 * @param where The resource which could not be found. 
	 */
	protected void notFoundError(Response response, String where) {
		response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		String message = "Client error \"not found\" in " + where;
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}
	
	
	/** Changes a response into a "not implemented" error (501) with respective status code and message. 
	 * @param response The response to change into an error message.
	 * @param where The resource which was called with a not implemented method.
	 */
	protected void notImplementedError(Response response, String where) {
		response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		String message = "Not implemented error in " + where;
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}
	
	
	/** Changes a response into a "bad request" error (400) with respective status code and message.
	 * @param response The response to change into an error message.
	 * @param where The resource which was called with malformed syntax.
	 */
	protected void badRequest(Response response, String where) {
		response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		String message = "Bad request " + where;
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}

}
