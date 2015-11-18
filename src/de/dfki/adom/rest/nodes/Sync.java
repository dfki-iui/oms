/**
 * 
 */
package de.dfki.adom.rest.nodes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.HeaderControl;

/** <p>A {@link Component} to handle the sync properties of a memory via the REST interface.</p>
 * <p>Represents the "sync" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Sync extends Component { // TODO Class is used in the REST tree (<memoryName>/sync) but doesn't do anything useful

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the sync node belongs. 
	 */
	public Sync(ADOMeRestlet adom) {
		super(adom);
	}

	@Override
	public void doGet(Request request, Response response) {
		String message = "GET Sync"; // and this is all it ever does
		HeaderControl.AddAccessControlAllowOrigin(response);
		response.setEntity(message, MediaType.TEXT_PLAIN);
		return;
	}
}
