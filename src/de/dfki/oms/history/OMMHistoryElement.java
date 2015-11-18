package de.dfki.oms.history;

import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.OMMEntity;

/** A single change to a memory, consisting of an entity who issued the change, a block which has been changed and a description of the operation. */
public class OMMHistoryElement
{
	public OMMEntity OMMEntity;
	public OMMBlock OMMBlock;
	public String Change; 
	
	/** Constructor. 
	 * 
	 * @param entity The {@link OMMEntity} who issued the change.
	 * @param block The {@link OMMBlock} which has been changed. 
	 * @param change A description of the change operation.
	 */
	public OMMHistoryElement(OMMEntity entity, OMMBlock block, String change)
	{
		OMMEntity = entity;
		OMMBlock = block;
		Change = change;
	}
}