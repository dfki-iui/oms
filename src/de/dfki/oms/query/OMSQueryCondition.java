package de.dfki.oms.query;

import java.util.Date;
import java.util.Map;

import de.dfki.omm.interfaces.OMM;

/** Search criteria for a query, determining properties of the OMMs to be found. */
public abstract class OMSQueryCondition
{
	/** 
	 * Static method to create a new query condition object from a parsed map entry. 
	 * If the condition applies to the OMM id, an {@link OMSQueryIDContition} will be created, 
	 * otherwise an {@link OMSQueryAttributeCondition}.  
	 * 
	 * @param condition The search condition as a map {@link Entry}<{@link String}, {@link Object}>. 
	 * @return An {@link OMSQueryCondition} if its creation was successful, otherwise null. 
	 */
	@SuppressWarnings("unchecked")
	public static OMSQueryCondition create(final Map.Entry<String, Object> condition)
	{
		if (condition.getKey() == "omm.id")
		{
//			return new OMSQueryIDContition((Map)condition.getValue());
			return new OMSQueryIDContition((Map<String, Object>)condition.getValue());
		}
		else if (condition.getKey().startsWith("omm.block."))
		{
			return new OMSQueryAttributeCondition(condition.getKey().replace("omm.block.", ""), (String)condition.getValue());
		}
		
		return null;
	}
	 
	/** Checks whether an OMM complies with the condition. 
	 * 
	 * @param omm The {@link OMM} to check.
	 * @param validAtDate {@link Date} of the last change to the OMM. 
	 * @return
	 */
	public abstract boolean check(final OMM omm, final Date validAtDate); 
}
