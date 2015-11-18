package de.dfki.oms.query;

import java.util.Date;
import java.util.Map;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.interfaces.OMMIdentifierBlock;
import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.TypedValue;

/** Search criteria for a query concerning an OMM's ID block. */
public class OMSQueryIDContition extends OMSQueryCondition
{
	String m_type = null, m_id = null, m_validAt = null, m_validBefore = null, m_validAfter = null;
	
	@SuppressWarnings("unused")
	private OMSQueryIDContition() {}
	
	/** Constructor. 
	 * @param obj A {@link Map}<{@link String}, {@link Object}> containing the structured condition. 
	 */
	OMSQueryIDContition(Map<String, Object> obj)
	{
		m_type = (String)obj.get("type");
		m_id = (String)obj.get("id");
		
		if (obj.containsKey("valid_at")) m_validAt = (String)obj.get("valid_at");
		if (obj.containsKey("valid_before")) m_validBefore = (String)obj.get("valid_before");
		if (obj.containsKey("valid_after")) m_validAfter = (String)obj.get("valid_after");		
	}
	
	/** <p>Converts an {@link OMSQueryIDContition} object to a String of the form: </p>
	 * <p>"[OMSQueryAttributeCondition] ('[id]' instanceof '[type]'; validAt='[date]'; validBefore='[date]'; validAfter='[date]')"</p>
	 */
	@Override
	public String toString()
	{
		StringBuffer retVal = new StringBuffer();
		retVal.append("[OMSQueryIDContition] ('"+m_id+"' instanceof '"+m_type+"'");
		
		if (m_validAt != null) retVal.append("; validAt='"+m_validAt+"'");
		if (m_validBefore != null) retVal.append("; validBefore='"+m_validBefore+"'");
		if (m_validAfter != null) retVal.append("; validAfter='"+m_validAfter+"'");
		
		retVal.append(")");
		return retVal.toString();
	}

	@Override
	public boolean check(final OMM omm, final Date validAtDate)
	{		
		if (validAtDate != null)
		{
			if (m_validAt != null && !ISO8601.parseDate(m_validAt).getTime().equals(validAtDate)) return false; 
			if (m_validBefore != null && ISO8601.parseDate(m_validBefore).getTimeInMillis() <= validAtDate.getTime()) return false; 
			if (m_validAfter != null && ISO8601.parseDate(m_validAfter).getTimeInMillis() >= validAtDate.getTime()) return false; 
		}
		else
		{
			if (m_validAt != null || m_validAfter != null || m_validBefore != null) return false;
		}
		
		for(OMMBlock block : omm.getAllBlocks())
		{
			if (block instanceof OMMIdentifierBlock)
			{
				if (check((OMMIdentifierBlock)block)) return true;
			}
		}
		return false;
	}
	
	/** Checks whether the OMM's identifier block complies with the condition. 
	 * @param block The {@link OMMIdentifierBlock} to check.
	 * @return True, if type and ID of the block comply with the condition. 
	 */
	private boolean check(OMMIdentifierBlock block)
	{
		for(TypedValue tv : block.getIdentifier())
		{
			if (tv.getType().equals(m_type) && tv.getValue().equals(m_id))
			{
				return true;
			}
		}
		
		return false;
	}
}
