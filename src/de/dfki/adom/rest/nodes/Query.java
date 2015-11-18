package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.HeaderControl;

/**
 * @author Christian Hauck
 * @organization DFKI
 */
public class Query extends Component { // TODO This class is not used anywhere


	public Query(ADOMeRestlet adom) {
		super(adom);
	}

	/**
	 * Handles the GET requests.
	 * @param request The request
	 * @param response The response
	 */
	@Override 
	public void doGet(Request request, Response response) {  
		String message = "Handle queries to the storage.";  
		HeaderControl.AddAccessControlAllowOrigin(response);
		response.setEntity(message, MediaType.TEXT_PLAIN);
		return;
	}
}
