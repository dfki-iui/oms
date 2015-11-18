package de.dfki.adom.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Request;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.OMMEntity;

/** Helpful methods to retrieve often needed information or perform frequent tasks. */
public class Tools {

	/** Retrieves an OMM entity from a HTTP request. 
	 * @param request The request from which to retrieve an entity. 
	 * @return An {@link OMMEntity} constructed from the request's IP address and the current time. 
	 */
	public static OMMEntity GetEntity(Request request) {
		String remoteAddr = request.getClientInfo().getAddress();
		String type = "ipv4";
		if (InIPv6Format(remoteAddr)) type = "ipv6";
		OMMEntity entity = new OMMEntity(type, remoteAddr, ISO8601.getISO8601String(new Date()));

		return entity;
	}
	
	
	/** Retrieves an OMM entity representing the Application. 
	 * @return An {@link OMMEntity} with type "api", value "Application" and the current time as date. 
	 */
	public static OMMEntity GetApiEntity() {
		String remoteAddr = "Application";
		String type = "api";
		OMMEntity entity = new OMMEntity(type, remoteAddr, ISO8601.getISO8601String(new Date()));

		return entity;
	}
	
	
	
	/** Checks whether an address is in IPv4 format. 
	 * @param addressString Address to check.
	 * @return True, if address is in IPv4 format (has four parts, separated by "."s). 
	 */
	public static boolean InIPv4Format(String addressString) {		
		Pattern p = Pattern.compile("[.]");
		String[] parts = p.split(addressString);
		
		if (parts.length == 4) return true;
		return false;
	}
	
	
	
	/** Checks whether an address is in IPv6 format. 
	 * @param addressString Address to check.
	 * @return True, if address is in IPv6 format (has eight parts, separated by ":"s). 
	 */
	public static boolean InIPv6Format(String addressString) {		
		Pattern p = Pattern.compile("[:]");
		String[] parts = p.split(addressString);
		
		if (parts.length == 8) return true;
		return false;
	}
	
	
	
	private static Pattern s_pathPatternOne = Pattern.compile("^(.*)/(.*)/(.*)/(.*)$");			// without trailing "/"
	private static Pattern s_pathPatternTwo = Pattern.compile("^(.*)/(.*)/(.*)/(.*)/$");		// with trailing "/"
	
	/**
	 * Extracts the block ID coded in the URL (given by the request) and looks if this ID is
	 * found in the given blockIdList. The URL is parsed end-to-begin and the relevant part
	 * is given by the positionFromEnd-parameter.
	 * Example: http://localhost:10082/rest/sample/st/block/4/ with positionFromEnd == 0
	 * gives "4" as ID, postitionFromEnd == 2 would give "st".
	 * 
	 * The method returns the aforementioned ID if it is contained in the given blockIdList,
	 * null else.
	 * @param request
	 * @param blockIdList
	 * @param positionFromEnd
	 * @return
	 */
	public static String GetBlockId(Request request, List<String> blockIdList, int positionFromEnd) {

		String path = request.getResourceRef().getPath();
		
		String result = null;
		Matcher matcher;
		
		if (path.endsWith("/"))
			matcher = s_pathPatternTwo.matcher(path);
		else
			matcher = s_pathPatternOne.matcher(path);
		
		if (matcher.matches()) {
			int num_groups = matcher.groupCount();
			String block_id = matcher.group(num_groups - positionFromEnd);
			if (blockIdList.contains(block_id))
				result = block_id;
		}
		
		return result;
	}
	
	
	/**
	 * Extracts the n-th component of the URL given by the request. The URL is parsed
	 * end-to-begin and the relevant part is given by the positionFromEnd-parameter.
	 * Example: http://localhost:10082/rest/sample/st/block/4/ with positionFromEnd == 0
	 * gives "4" as ID, postitionFromEnd == 2 would give "st".
	 * 
	 * The method returns the positionFromEnd-th part of the URL (counted from the end)
	 * if it exists, null else.
	 * @param request
	 * @param positionFromEnd
	 * @return
	 */
	public static String GetPathComponent(Request request, int positionFromEnd) {

		String path = request.getResourceRef().getPath();
		
		String result = null;
		Matcher matcher;
		
		if (path.endsWith("/"))
			matcher = s_pathPatternTwo.matcher(path);
		else
			matcher = s_pathPatternOne.matcher(path);
		
		if (matcher.matches()) {
			int num_groups = matcher.groupCount();
			result = matcher.group(num_groups - positionFromEnd);
		}
		
		return result;
	}
	
	
	
	/** Converts an XML snippet to a full XML document String. 
	 * @param snipped The snippet to extend to a valid OMM document.
	 * @return Complete OMM document in XML format including the snippet. 
	 */
	public String XmlSnipped2OMMDocString(String snipped) {
		String doc_str = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
				"<omm:omm xmlns:omm=\"http://www.w3.org/2005/Incubator/omm/elements/1.0/\"> \n" +
				snipped + "\n" +
				"</omm:omm>";
		return doc_str;
	}
	
	
	/** Parses a string containing XML and returns a DocumentFragment containing the nodes of the parsed XML.
	 * @param doc 
	 * @param fragment
	 * @return
	 */
	public static DocumentFragment ParseXml(Document doc, String fragment) { // TODO Method is not used anywhere
		// Wrap the fragment in an arbitrary element
		fragment = "<fragment>"+fragment+"</fragment>";
		try {
			// Create a DOM builder and parse the fragment
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document d = factory.newDocumentBuilder().parse(
					new InputSource(new StringReader(fragment)));

			// Import the nodes of the new document into doc so that they
			// will be compatible with doc
			Node node = doc.importNode(d.getDocumentElement(), true);

			// Create the document fragment node to hold the new nodes
			DocumentFragment docfrag = doc.createDocumentFragment();

			// Move the nodes into the fragment
			while (node.hasChildNodes()) {
				docfrag.appendChild(node.removeChild(node.getFirstChild()));
			}

			// Return the fragment
			return docfrag;
		} catch (SAXException e) {
			// A parsing error occurred; the xml input is not valid
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	
	/** Reads a file and returns its contents as String. 
	 * @param path Path to the file, without its name.
	 * @param filename The file's name.
	 * @return The file's content.
	 */
	public static String Read(String path, String filename) {
		File file = new File(path + filename);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		
		try {           
			reader = new BufferedReader(new FileReader(file));     
			String text = null;     
			while ((text = reader.readLine()) != null) {         
				contents.append(text).append(System.getProperty("line.separator")); 
			}       
		} catch (FileNotFoundException e) {         
			e.printStackTrace();      
		} catch (IOException e) {   
			e.printStackTrace();    
		} finally {          
			try {             
				if (reader != null) {   
					reader.close();     
				}          
			} catch (IOException e) {  
				e.printStackTrace();     
			}     
		}
		
		return contents.toString();
	}
  
}
