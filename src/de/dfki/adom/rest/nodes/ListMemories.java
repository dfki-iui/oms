package de.dfki.adom.rest.nodes;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

import de.dfki.adom.rest.HeaderControl;

/** <p>A {@link Component} to handle the addition of OMM blocks to a memory via the REST interface.</p>
 * <p>Represents the "block" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class ListMemories extends Component {

	List<String> m_memoryNames;
	String       m_type;
	
	/** Constructor. 
	 * @param memoryNames List of all memory names.
	 * @param type The return type of the memory name list, can be "html" or "json". 
	 */
	public ListMemories(List<String> memoryNames, String type) {
		super(null);
		m_memoryNames = memoryNames;
		m_type = type;
		
		if (!m_type.equals("html") && !m_type.equals("json"))
			m_type = "html";
	}

	
	/* (non-Javadoc)
	 * @see de.dfki.adom.Component#doGet(org.restlet.Request, org.restlet.Response)
	 */
	@Override
	public void doGet(Request request, Response response) {
		String message = null;
		
		if (m_type.equals("html")) {
			String part1 = 
					"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \n" +
					"   \"http://www.w3.org/TR/html4/loose.dtd\"> \n" +
					"<html> \n" +
					"<head> \n" +
					"<title>List of the Available OMM Memories</title> \n" +
					"</head> \n" +
					"<body> \n" +
					"<h1>List of the Available OMM Memories</h1> \n" +
					"<p> \n";

			String part2 = "";
			for (String name : m_memoryNames) {
				part2 += "  <a href=\"" + name + "\">" + name + "</a> \n";
			}

			String part3 =
					"</p> \n" +
					"</body> \n" +
					"</html>";

			message = part1 + part2 + part3;
			response.setEntity(message, MediaType.TEXT_HTML);
		} 
		
		else 	
		if (m_type.equals("json")) {
			message = "";
			String part1 = "[";
			
			String part2 = "";
			for (String name : m_memoryNames) {
				if (!name.equals(m_memoryNames.get(0)))  // insert comma if more than 1 element
					part2 += ",";
				part2 += "\"" + name + "\"";
			}

			String part3 = "]";
			message = part1 + part2 + part3;
			response.setEntity(message, MediaType.TEXT_PLAIN);
		} 
		
		else {
			message = "Error: Something went wrong in de.dfki.adom.ListMemories!";
			response.setEntity(message, MediaType.TEXT_PLAIN);
		}
		HeaderControl.AddAccessControlAllowOrigin(response);
		
		return;
	}
}
