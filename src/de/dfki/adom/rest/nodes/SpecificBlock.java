package de.dfki.adom.rest.nodes;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.Tools;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.tools.OMMActionResultType;
import de.dfki.omm.types.OMMEntity;

/** <p>A {@link Component} to handle the output and deletion of specific OMM blocks via the REST interface.</p>
 * <p>Represents the "[block id]" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class SpecificBlock extends Component {

	private BlockManager m_blockManager;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the block belongs. 
	 * @param blockManager The {@link BlockManager} managing the block. 
	 */
	public SpecificBlock(ADOMeRestlet adom, BlockManager blockManager) {
		super(adom);
		m_blockManager = blockManager;
	}


	/**
	 * SpecificBlock.doGet() is not defined by the spec.
	 */
	@Override
	public void doGet(Request request, Response response) {
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 0);

		if (block_id == null) {
			notFoundError(response, "SpecificBlock when searching for block " + block_id);		
			return;
		}
		notImplementedError(response, "SpecificBlock: GET request not implemented for block " + block_id);

		return;
	}

	
	
	@Override
	public void doDelete(Request request, Response response) {

		if (ADOMeRestlet.MemoryAccess.IsDeleteDisabled(m_blockManager.getADOM().getMemoryName()))
		{
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		}
		
		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		String block_id = Tools.GetBlockId(request, blockIdList, 0);
		
		if (block_id == null) {
			notFoundError(response, "SpecificBlock when searching for block " + block_id);		
			return;
		}
		
		// --- Perform Delete ---
		ADOMeRestlet adom = getADOM();
		API api = adom.getAPI();
		OMMActionResultType result = api.deleteBlock(block_id);
		
		switch (result) {
			case Forbidden :
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				break;
			case OK : 
				// do nothing
				// better yet, delete block from blockIdList
				blockIdList.remove(block_id);
				break;
			default :
				notFoundError(response, "SpecificBlock deleting block " + block_id);
				break;
		}
		
		return;
	}
	
	

	/**
	 * Deletes the block with the given blockID. Returns true if the delete operation was successful, false else.
	 * @param blockID
	 * @return true if delete of block with given blockID successful, false else.
	 */
	public OMMActionResultType delete(String blockID) {

		// --- Get Block ID ---
		List<String> blockIdList = m_blockManager.getBlockIdList();
		if ((blockID == null) || (!blockIdList.contains(blockID)))
			return OMMActionResultType.BlockNotExistent;

		// --- Perform Delete ---
		OMM omm = getADOM().getOMM();
		OMMEntity entity = Tools.GetApiEntity();

		return omm.removeBlock(blockID, entity);

	}
}
