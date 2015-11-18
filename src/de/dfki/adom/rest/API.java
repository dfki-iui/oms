/**
 * 
 */
package de.dfki.adom.rest;

import java.util.HashSet;
import java.util.Set;

import de.dfki.adom.rest.nodes.Block;
import de.dfki.adom.rest.nodes.BlockIDs;
import de.dfki.adom.rest.nodes.BlockManager;
import de.dfki.adom.rest.nodes.FeatureNegotiation;
import de.dfki.adom.rest.nodes.MetaInfo;
import de.dfki.adom.rest.nodes.Payload;
import de.dfki.adom.rest.nodes.SpecificBlock;
import de.dfki.adom.rest.nodes.Storage;
import de.dfki.adom.rest.nodes.Toc;
import de.dfki.omm.tools.OMMActionResultType;

/** Provides an API for an {@link ADOMeRestlet} with which its properties and contents can be accessed internally. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class API implements APIObservable {
  
	private final Set<APIObserver> observers = new HashSet<APIObserver>();
	private ADOMeRestlet m_adom;

	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} for which an API is provided. 
	 */
	public API(ADOMeRestlet adom) {
		m_adom = adom;
	}

	
	/** Retrieves the feature negotiation data of an ADOM (for example version and storage information) as a JSON String.
	 * @param secure True, if the secure feature negotiation data is to be retrieved (which is not attached to a router). 
	 * @return Feature negotiation data of an ADOM as a JSON {@link String}
	 */
	public String getFeatureNegotiation(boolean secure) {
		
		if (m_adom == null) return null;
		
		FeatureNegotiation feature_neg = m_adom.getFeatureNegotiation(secure);
		String result = feature_neg.get();
		
		for (APIObserver obs : this.observers) obs.notifyFeatureNegotiation(result);
		return result;
	}

	
	/** Retrieves the table of contents string of an ADOM. 
	 * @return The table of contents as {@link String}. 
	 */
	public String getToC() {
		if (m_adom == null)
			return null;
		
		Storage storage = m_adom.getStorage();
		Toc toc = storage.getToc();
		
		String result = toc.get();
		for (APIObserver obs : this.observers) obs.notifyTableOfContents(result);
		return result;
	}


	/** Retrieves all block IDs of this ADOM. 
	 * @return All block IDs of this ADOM in one {@link String}. 
	 */
	public String getBlockIDs() {
		if (m_adom == null)
			return null;
		
		Storage storage = m_adom.getStorage();
		BlockIDs block_ids = storage.getBlockIDs();
		
		String result = block_ids.get();
		for (APIObserver obs : this.observers) obs.notifyBlockIDs(result);
		return result;
	}


	/** Given a block in form of an XML text, adds the block to the ADOM by posting it to the REST interface. 
	 * @param blockAsXMLSnipped The XML text representing a block.
	 * @return The added block's ID. 
	 */
	public String postBlock(String blockAsXMLSnipped) {
		
		if (m_adom == null) return null;
		
		Storage storage = m_adom.getStorage();
		Block block = storage.getBlock();
		
		String blockID = block.post(blockAsXMLSnipped);
		
		for (APIObserver obs : this.observers) obs.notifyPostBlock(blockID, blockAsXMLSnipped);
		return blockID;
	}
	
	
	/** Deletes a specific block from an ADOM.
	 * 
	 * @param blockID ID of the block to delete. 
	 * @return Result of the deletion as a {@link OMMActionResultType}. 
	 */
	public OMMActionResultType deleteBlock(String blockID) {

		if (m_adom == null) return OMMActionResultType.UnknownError;

		Storage storage = m_adom.getStorage();
		Block block = storage.getBlock();
		BlockManager block_manager = block.getBlockManager();
		SpecificBlock specific_block = block_manager.getSpecificBlock();

		OMMActionResultType result = specific_block.delete(blockID);
		for (APIObserver obs : this.observers) obs.notifyDeleteBlock(result == OMMActionResultType.OK, blockID);
		return result;
	}


	/** Retrieves the meta information of a specific block.
	 * @param blockID ID of the block. 
	 * @return The block's meta information as an XML {@link String}. 
	 */
	public String getMeta(String blockID) {
		
		if (m_adom == null) return null;
		
		MetaInfo meta_info = this.metaInfo();
		
		String result = meta_info.get(blockID);
		for (APIObserver obs : this.observers) obs.notifyMeta(result, blockID);
		return result;
	}
	
	/** Retrieves the meta information of a specific block.
	 * @param blockID ID of the block. 
	 * @return The block's meta information as a JSON {@link String}.
	 */
	public String getMetaAsJson(String blockID) {
		if (m_adom == null)
			return null;
		MetaInfo meta_info = this.metaInfo();
		
		String result = meta_info.getJson(blockID);
		for (APIObserver obs : this.observers) obs.notifyMeta(result, blockID);
		return result;
	}

	
	/** Retrieves the payload of a specific block.
	 * @param blockID ID of the block. 
	 * @return The block's payload as {@link String}.
	 */
	public String getPayload(String blockID) {
		
		if (m_adom == null) return null;
		Payload payload = this.payload();
		
		String result = payload.get(blockID);
		for (APIObserver obs : this.observers) obs.notifyPayload(result, blockID);
		return result;
	}


	/** Adds a payload to the block with the given ID. 
	 * @param blockID ID of the block to which to add the payload. 
	 * @param payload Payload to add. 
	 * @return True, if the action was successful.
	 */
	public boolean postPayload(String blockID, String payload) {
		
		if (m_adom == null) return false;
		Payload pl = this.payload();
		
		boolean result = pl.post(blockID, payload);
		for (APIObserver obs : this.observers) obs.notifyPostPayload(result, blockID, payload);
		return result;
	}


	/** Deletes the payload from the block with the given ID. 
	 * @param blockID ID of the block to which to add the payload. 
	 * @return True, if the action was successful.
	 */
	public boolean deletePayload(String blockID) {
		
		if (m_adom == null) return false;
		Payload payload = this.payload();
		
		boolean result = payload.delete(blockID);
		for (APIObserver obs : this.observers) obs.notifyDeletePayload(result, blockID);
		return result;
	}

	
	/** Retrieves the payload's encoding from the block with the given ID. 
	 * @param blockID ID of the block. 
	 * @return The payload's encoding as {@link String}.
	 */
	public String getPayloadEncoding(String blockID) {
		
		if (m_adom == null) return null;
		Payload payload = this.payload();
		
		String result = payload.get(blockID);
		for (APIObserver obs : this.observers) obs.notifyPayloadEncoding(result, blockID);
		return result;
	}
	
	
	
	
	/*
	 * *********************************************************************************
	 * 
	 *                        P R I V A T E   M E T H O D S
	 *                        
	 * *********************************************************************************
	 */
	
	
	private MetaInfo metaInfo() {
		Storage storage = m_adom.getStorage();
		Block block = storage.getBlock();
		BlockManager block_manager = block.getBlockManager();
		MetaInfo meta_info = block_manager.getMetaInfo();
		
		return meta_info;
	}
	
	private Payload payload() {
		Storage storage = m_adom.getStorage();
		Block block = storage.getBlock();
		BlockManager block_manager = block.getBlockManager();
		Payload payload = block_manager.getPayload();
		
		return payload;
	}

	
  @Override
  public void addObserver(APIObserver obs)
  {
    this.observers.add(obs);
  }

  @Override
  public void removeObserver(APIObserver obs)
  {
    this.observers.remove(obs);
  }
	  
}
