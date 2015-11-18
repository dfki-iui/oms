package de.dfki.adom.client;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/** Implementation of {@link AdomConnector} that connects to a remote URL as specified in the client configuration. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class RemoteConnector implements AdomConnector {
	
	private ClientConfig m_clientConfig;

	/** Constructor.
	 * @param config The {@link ClientConfig} to use for the connection. 
	 */
	public RemoteConnector(ClientConfig config) {
		m_clientConfig = config;
	}

	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#getBlockIDs()
	 */
	public String getBlockIDs() {
		ClientResource resource = new ClientResource(m_clientConfig.getBlockIDsURL());
		
		String text = null;
		try {
			Representation rep = resource.get();
			text = rep.getText();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#writePayload(java.lang.String)
	 */
	public void writePayload(String text) {
		ClientResource resource = new ClientResource(m_clientConfig.getReadingsURL());
		try {
			Representation rep = resource.post(text);
			rep.write(System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#appendPayload(java.lang.String)
	 */
	public void appendPayload(String text) {
		ClientResource resource = new ClientResource(m_clientConfig.getReadingsURL());
		try {
			Representation rep = resource.post(text);
			rep.write(System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.client.AdomConnector#createBlock(java.lang.String)
	 */
	public void createBlock(String blockID) {
		String file_name = m_clientConfig.getSnippedMap().get(blockID);
		String snipped = Tools.Read(m_clientConfig.getSnippedPath(), file_name);
		
		ClientResource resource = new ClientResource(m_clientConfig.getBlockURL());		
		try {
			Representation rep = resource.post(snipped);
			rep.write(System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
