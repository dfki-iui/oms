/**
 * 
 */
package de.dfki.adom.rest.nodes;

import java.util.Collection;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMToCEntry;
import de.dfki.omm.tools.OMMXMLConverter;

/** <p>A {@link Component} to provide a memory's Table of Contents via the REST interface.</p>
 * <p>Represents the "toc" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Toc extends Component {

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the Table of Contents belongs. 
	 */
	public Toc(ADOMeRestlet adom) {
		super(adom);
	}

//	@Override
//	public void doGet(Request request, Response response) {
//
//		String message;
//		ADOM adom = getADOM();
//		OMM omm = adom.getOMM();
//		
//		if (omm == null) {
//			response.setStatus(Status.SERVER_ERROR_INTERNAL);
//			message = "Internal Error in Toc.doGet: No OMM of name \"" +  adom.getMemoryName() + "\" found.";
//			response.setEntity(message, MediaType.TEXT_PLAIN);
//			return;
//		}
//		
//		Collection<OMMToCEntry> omm_tocs = omm.getTableOfContents();
//		
//		if ((omm_tocs == null) || (omm_tocs.size() == 0)) {
//			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
//			message = "No Tables of Content found for OMM \"" +  adom.getMemoryName() + "\".";
//			response.setEntity(message, MediaType.TEXT_PLAIN);
//			return;
//		}
//		
//		message = OMM_IO.toXMLFileString(omm_tocs);
//		response.setEntity(message, MediaType.APPLICATION_XML);
//		
//		return;
//	}
	
	/**
	 * Handles the GET request to the Table of Contents.
	 * @param request
	 * @param response
	 */
	@Override
	public void doGet(Request request, Response response) {
	  ADOMeRestlet adom = getADOM();
	  API api = adom.getAPI();
	  String message = api.getToC();
		
		if (message == null) {
			notFoundError(response, "Toc.doGet()");
			return;
		}
		
		HeaderControl.AddAccessControlAllowOrigin(response);
		
		response.setEntity(message, MediaType.APPLICATION_XML);
		return;
	}	

	
	/**
	 * Delivers the table of contents string of an ADOM.
	 * @return the table of contents string.
	 */
	public String get() {
		String message;
		ADOMeRestlet adom = getADOM();
		OMM omm = adom.getOMM();
		
		if (omm == null)
			return null;
		
		Collection<OMMToCEntry> omm_tocs = omm.getTableOfContents();
		
		if ((omm_tocs == null) || (omm_tocs.size() == 0))
			return null;
		
		message = OMMXMLConverter.toXMLFileString(omm_tocs);
		return message;
	}
}
