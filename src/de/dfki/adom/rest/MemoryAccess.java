package de.dfki.adom.rest;

import java.util.List;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;

/** Handles access to a memory and its management properties. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public interface MemoryAccess 
{	
	/**
	 * Returns a Set of Strings containing the names of all existing memories.
	 * @return returns the names of all existing memories.
	 */
	public List<String> getMemoryNames();
		
	/**
	 * Returns the OMM memory of the given name if such a OMM exists, null else.
	 * @param name
	 * @return returns the OMM of the given name if such a OMM exists, null else.
	 */
	public OMM getMemory(String name);
		
	/** Checks whether memory deletion is allowed.  
	 * @param memoryName The name of the memory to check.
	 * @return True, if memory cannot be deleted.
	 */
	public boolean IsDeleteDisabled(String memoryName);	
	
	/** Retrieves information about an object memory's owner. 
	 * @param memoryName The name of the memory to check.
	 * @return Owner information in fom of an {@link OMMBlock}. 
	 */
	public OMMBlock getOwnerBlock(String memoryName);

	/** Sets or replaces an object memory's owner.
	 * @param memoryName The name of the memory of which the owner is set. 
	 * @param ommBlock New owner information as an {@link OMMBlock}.
	 */
	public void setOwnerBlock(String memoryName, OMMBlock ommBlock);
	
	/** Deletes a memory from the OMS. 
	 * @param memoryName The name of the memory to delete. 
	 */
	public void deleteMemory(String memoryName);
}
