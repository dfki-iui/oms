package de.dfki.adom.client;

/** Connects to an object memory to issue specific tasks. */
public interface AdomConnector {

	/**
	 * Returns the existing Block-IDs of an ADOM.
	 * @return returns the existing Block-IDs of an ADOM.
	 */
	public String getBlockIDs();
	
	/**
	 * (Over-)writes the payload of an ADOM-block with the given String payload. 
	 * @param payload as String.
	 */
	public void writePayload(String payload);
	
	/**
	 * Appends the given String payload to the payload of an ADOM-block. 
	 * @param payload as String.
	 */
	public void appendPayload(String payload);
	
	/**
	 * Creates a new ADOM-block. 
	 * @param blockID as String.
	 */
	public void createBlock(String blockID);
}
