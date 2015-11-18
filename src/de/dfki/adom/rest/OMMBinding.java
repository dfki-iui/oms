package de.dfki.adom.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;

/** Rudimentary memory access, allowing the retrieval of memories and memory names.  
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class OMMBinding implements MemoryAccess {

	private String m_memoryName = null;
	
	public OMMBinding() {
	}

	/** Constructor.
	 * @param memoryName Name of the memory which is bound to this access manager. 
	 */
	public OMMBinding(String memoryName) {
		m_memoryName = memoryName;
	}

	
	/* (non-Javadoc)
	 * @see de.dfki.adom.MemoryAccess#getMemoryNames()
	 */
	public List<String> getMemoryNames() {	
		
		ArrayList<String> memory_names = new ArrayList<String>();
		
		if (m_memoryName != null) memory_names.add(m_memoryName);
		else memory_names.add(Config.SampleMemory);
		
		return memory_names;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.adom.MemoryAccess#getMemory(java.lang.String)
	 */
	public OMM getMemory(String memoryName) {
		
		File file = new File(Config.MemoryPath + memoryName + Config.SampleFile);
		OMM omm = OMMFactory.loadOMMFromXmlFile(file);
		
		return omm;
	}
	
	public boolean IsDeleteDisabled(String memoryName) {
		return false;
	}

	@Override
	public OMMBlock getOwnerBlock(String memoryName) {
		return null;
	}

	@Override
	public void setOwnerBlock(String memoryName, OMMBlock ommBlock) {
	}

	@Override
	public void deleteMemory(String memoryName) {
	}

}
