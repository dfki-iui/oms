package de.dfki.oms.adom;

import java.util.List;

import de.dfki.adom.rest.MemoryAccess;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.history.OMMVersionManager;
import de.dfki.oms.history.OMMVersionManagerHistory;

/** Implementation of {@link MemoryAccess}, allowing access to an OMS's memories and their properties. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class OmsAdomBinding implements MemoryAccess  
{
	protected OMMVersionManagerHistory m_history = OMMVersionManagerHistory.Enabled;
	
	/** Constructor.
	 * @param history A {@link OMMVersionManagerHistory} object determining whether the OMS should keep a history. 
	 */
	public OmsAdomBinding(OMMVersionManagerHistory history) {
		m_history = history;
	}

	/* (non-Javadoc)
	 * @see de.dfki.adom.MemoryAccess#getMemoryNames()
	 */
	public List<String> getMemoryNames() {	
		List<String> memory_names = OMMVersionManager.getAvailableMemories();
		
		return memory_names;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.MemoryAccess#getMemory(java.lang.String)
	 */
	public OMM getMemory(String name) {
		OMMVersionManager vm = OMMVersionManager.create(name, m_history);
		if (vm == null)
			return null;
		
		OMM omm = vm.getCurrentVersion();
		
		return omm;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.rest.MemoryAccess#IsDeleteDisabled(java.lang.String)
	 */
	public boolean IsDeleteDisabled(String memoryName)
	{
		OMMVersionManager vm = OMMVersionManager.create(memoryName, m_history);
		if (vm == null) return false;
		
		return vm.getConfig().isDeleteDisabled();
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.rest.MemoryAccess#getOwnerBlock(java.lang.String)
	 */
	public OMMBlock getOwnerBlock(String memoryName)
	{
		OMMVersionManager vm = OMMVersionManager.create(memoryName, m_history);
		if (vm == null) return null;
		
		return vm.getConfig().getOwnerBlock();
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.rest.MemoryAccess#setOwnerBlock(java.lang.String, de.dfki.omm.interfaces.OMMBlock)
	 */
	public void setOwnerBlock(String memoryName, OMMBlock ownerBlock)
	{
		OMMVersionManager vm = OMMVersionManager.create(memoryName, m_history);
		if (vm == null) return;
		
		vm.getConfig().setOwnerBlock(ownerBlock);
	}

	/* (non-Javadoc)
	 * @see de.dfki.adom.rest.MemoryAccess#deleteMemory(java.lang.String)
	 */
	public void deleteMemory(String memoryName) 
	{
		OMMVersionManager vm = OMMVersionManager.create(memoryName, m_history);
		if (vm == null) return;
		
//		vm.deleteMemory(memoryName);
		OMMVersionManager.deleteMemoryVersionManager(memoryName);
		
		vm.getConfig().deleteMemory();
	}
}
