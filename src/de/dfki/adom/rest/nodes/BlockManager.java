package de.dfki.adom.rest.nodes;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.interfaces.OMM;
 
/** <p>A {@link Component} to handle the retrieval of OMM blocks and block information via the REST interface.</p>
 *   
 * @author Christian Hauck
 * @organization DFKI
 */
public class BlockManager extends Component {

	protected List<String>	m_blockIdList;
	
	protected SpecificBlock	m_specificBlock;
	protected MetaInfo		m_metaInfo;
	protected Payload			m_payload;

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} for which this manager handles the blocks. 
	 */
	public BlockManager(ADOMeRestlet adom) {
		super(adom);
		
		m_blockIdList		= new ArrayList<String>();
		
		m_specificBlock     = new SpecificBlock(adom, this);
		m_metaInfo			= new MetaInfo(adom, this);
		m_payload			= new Payload(adom, this);
		
//		for (String blockID : adom.getOMM().getAllBlockIDs()){
//		  this.m_blockIdList.add(blockID);
//		}
		
		this.updateSpecificBlocks();
	}

	@Override
	public void doGet(Request request, Response response) {
		String message = "BlockManager received GET request.";
		response.setEntity(message, MediaType.TEXT_PLAIN);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}

	/**
	 * Delivers the block id list object of the ADOM storage.
	 * @return block id list object.
	 */
	public List<String> getBlockIdList() {
		return m_blockIdList;
	}

	/**
	 * Delivers the SpecificBlock object of the ADOM storage.
	 * @return SpecificBlock object.
	 */
	public SpecificBlock getSpecificBlock() {
		return m_specificBlock;
	}

	/**
	 * Delivers the MetaInfo object of the ADOM storage.
	 * @return SpecificBlock object.
	 */
	public MetaInfo getMetaInfo() {
		return m_metaInfo;
	}

	/**
	 * Delivers the Payload object of the ADOM storage.
	 * @return Payload object.
	 */
	public Payload getPayload() {
		return m_payload;
	}

	/**
	 * Updates the specific blocks list of the ADOm BlockManager.
	 */
	public void updateSpecificBlocks() {
		ADOMeRestlet adom = getADOM();
		OMM omm   = adom.getOMM();
		
		m_blockIdList = omm.getAllBlockIDs();
		
		return;
	}

	@Override
	public void attach(Router router, String path) {
		
//		super.attach(router, path);   // do not handle path - let it do SpecificBlock
		updateSpecificBlocks();
		
		m_specificBlock.attach(router, path);
		m_metaInfo.attach(router, path + "/meta");
		m_payload.attach(router, path + "/payload");
	}
	
	/*
	 * Detach this object and its subcomponents from the given router.
	 * @param router
	 */
	@Override
	public void detach(Router router) {
		m_specificBlock.detach(router);
		m_metaInfo.detach(router);
		m_payload.detach(router);
//		super.detach(router);
	}
}
