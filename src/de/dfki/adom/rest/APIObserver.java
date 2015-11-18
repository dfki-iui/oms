package de.dfki.adom.rest;


/** Watches over an API and is notified about any changes to the represented object memory. */
public interface APIObserver { // TODO Interface is not implemented anywhere
  /**
   * Notifies this observer about a getFeatureNegotiation-request including its response.
   * 
   * @param response as String
   */
  public void notifyFeatureNegotiation(String response);
  
  /**
   * Notifies this observer about a getToC-request including its response.
   * 
   * @param response as String
   */
  public void notifyTableOfContents(String response);
  
  /**
   * Notifies this observer about a getBlockIDs-request including its response.
   * 
   * @param response as String
   */
  public void notifyBlockIDs(String response);
  
  /**
   * Notifies this observer about a postBlock-request including its response and the posted block's
   * data.
   * 
   * @param response as String
   * @param blockAsXMLSnipped as String
   */
  public void notifyPostBlock(String response, String blockAsXMLSnipped);
  
  /**
   * Notifies this observer about a deleteBlock-request including its response and the block's ID.
   * 
   * @param response as String
   * @param blockID as String
   */
  public void notifyDeleteBlock(boolean response, String blockID);
  
  /**
   * Notifies this observer about a getMeta-request including its response and the block's ID.
   * 
   * @param response as String
   * @param blockID as String
   */
  public void notifyMeta(String response, String blockID);
  
  /**
   * Notifies this observer about a getMetaAttribute-request including its response, the block's ID
   * and the MetaAttribute.
   * 
   * @param response as String
   * @param blockID as String
   * @param metaAttribute as an object of type MetaAttribute
   */
  public void notifyMetaAttribute(String response, String blockID, MetaAttribute metaAttribute);
  
  /**
   * Notifies this observer about a postMetaAttribute-request including its response, the block's ID,
   * the MetaAttribute and the posted data.
   * 
   * @param response as String
   * @param blockID as String
   * @param metaAttribute as an object of type MetaAttribute
   * @param attribXMLSnipped as String
   */
  public void notifyPostMetaAttribute(boolean response, String blockID, MetaAttribute metaAttribute, String attribXMLSnipped);
  
  /**
   * Notifies this observer about a getPayload-request including its response and the block's ID.
   * 
   * @param response as String
   * @param blockID as String
   */
  public void notifyPayload(String response, String blockID);
  
  /**
   * Notifies this observer about a postPayload-request including its response, the block's ID and
   * the posted payload.
   * 
   * @param response as String
   * @param blockID as String
   * @param payload as String
   */
  public void notifyPostPayload(boolean response, String blockID, String payload);
  
  /**
   * Notifies this observer about a deletePayload-request including its response and the block's ID.
   * 
   * @param response True if there is one
   * @param blockID as String
   */
  public void notifyDeletePayload(boolean response, String blockID);
  
  /**
   * Notifies this observer about a getPayloadEncoding-request including its response and the
   * block's ID.
   * 
   * @param response as String
   * @param blockID as String
   */
  public void notifyPayloadEncoding(String response, String blockID);
}
