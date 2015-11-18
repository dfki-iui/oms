package de.dfki.adom.client;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;

/** Implementation of {@link AdomConnector} that connects to a local {@link ADOMeRestlet}. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class LocalConnector implements AdomConnector {

	private ADOMeRestlet m_adom;
	private ClientConfig m_clientConfig;

	public LocalConnector() {
	}
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which to connect. 
	 * @param config The {@link ClientConfig} to use for the connection. 
	 */
	public LocalConnector(ADOMeRestlet adom, ClientConfig config) {
		m_adom = adom;
		m_clientConfig = config;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#getBlockIDs()
	 */
	public String getBlockIDs() {
		System.err.println("de.dfki.adom.client.getBlockIDs() not yet implemented");
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#writePayload(java.lang.String)
	 */
	public void writePayload(String text) {
		API api = m_adom.getAPI();
		String blockID = m_clientConfig.getReadingsBlockID();
		api.postPayload(blockID, text);
		System.out.println("client.LocalConnector wrote " + text + " to local block " + blockID);
		return;
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#appendPayload(java.lang.String)
	 */
	public void appendPayload(String text) {
		System.err.println("de.dfki.adom.client.appendPayload() not yet implemented");
		return;
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#createBlock(java.lang.String)
	 */
	public void createBlock(String blockID) {
		System.err.println("de.dfki.adom.client.createBlock() not yet implemented");
		return;
	}
}
