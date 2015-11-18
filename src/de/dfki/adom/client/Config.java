package de.dfki.adom.client;

import java.util.HashMap;

/**
 * @author Christian Hauck
 * @organization DFKI
 */
public interface Config { // TODO This class is not used anywhere

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
}
