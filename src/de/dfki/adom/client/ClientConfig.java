package de.dfki.adom.client;

import java.util.HashMap;

/** Configuration for a client, determining paths, filenames and connectivity. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public interface ClientConfig { 

	public String getConfigBlockID();
	
	public String getReadingsBlockID();
	
	public String getSnippedPath();
	
	public String getConfigSnippedFileName();
	
	public String getReadingsSnippedFileName();
	
	public HashMap<String, String> getSnippedMap();
	
	
	public String getADOMHostURL();
	
	public Integer getADOMPort();

	public String getBlockURL();
	
	public String getBlockIDsURL();
	
	public String getConfigURL();
	
	public String getReadingsURL();
	
	public String getMemoryName();
}
