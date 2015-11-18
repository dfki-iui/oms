package de.dfki.oms.query;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMMultiLangText;
import de.dfki.omm.types.OMMSubjectTag;

/** Search criteria for a query concerning an OMM's attributes (blocks and their metadata). */
public class OMSQueryAttributeCondition extends OMSQueryCondition
{
	private String m_key = null, m_value = null;
	
	@SuppressWarnings("unused")
	private OMSQueryAttributeCondition() {}
	
	/** Constructor. 
	 * @param key The block attribute that is to comply with the condition, for example "title" or "namespace"). 
	 * @param value The value the attribute is meant to have to comply with the condition. 
	 */
	OMSQueryAttributeCondition(String key, String value)
	{
		m_key = key; m_value = value;
	}
	
	/** Retrieves the block attribute that is to comply with the condition.
	 * @return Attribute name as {@link String}. 
	 */
	public String getKey()
	{
		return m_key;		
	}
	
	/** Retrieves the value the attribute is meant to have to comply with the condition.
	 * @return Value as {@link String}. 
	 */
	public String getValue()
	{
		return m_value;
	}
	
	/** <p>Converts an {@link OMSQueryAttributeCondition} object to a String of the form: </p>
	 * <p>"[OMSQueryAttributeCondition] ('[key]' = '[value]')"</p>
	 */
	@Override
	public String toString()
	{
		return "[OMSQueryAttributeCondition] ('"+m_key+"'='"+m_value+"')";
	}


	@Override
	public boolean check(OMM omm, final Date validAtDate)
	{
		if (omm == null) return false;
		
		for(OMMBlock block : omm.getAllBlocks())
		{
			if (check(block)) return true;
		}
		
		return false;
	}
	
	/** Checks whether a specific block complies with the condition. 
	 * @param block The {@link OMMBlock} to check. 
	 * @return True, if the block has an attribute-value-pair compliant to this OMSQueryAttributeCondition. 
	 */
	private boolean check(OMMBlock block)
	{
		switch(m_key)
		{
			case "id":
				return block.getID().equals(m_value);
			case "type":
				return block.getType().toString().equals(m_value);
			case "namespace":
				return block.getNamespace().toString().equals(m_value);
			case "title":
				OMMMultiLangText title = block.getTitle();
				for(Locale lang : title.keySet())
				{
					if (title.get(lang).equals(m_value)) return true;
				}
				break;
			case "description":
				OMMMultiLangText description = block.getDescription();
				for(Locale lang : description.keySet())
				{
					if (description.get(lang).equals(m_value)) return true;
				}
				break;
			case "format":
				return block.getFormat().getMIMEType().equals(m_value);
			case "creator":
				return block.getCreator().getValue().equals(m_value);
			case "contributor":
				List<OMMEntity> contributors = block.getContributors();
				for(OMMEntity contributor : contributors)
				{
					if (contributor.getValue().equals(m_value)) return true;
				}
			case "subject":
				for(OMMSubjectTag tag : block.getSubject())
				{
					if (checkSubject(tag)) return true;
				}
				return false;
		}
		
		return false;
	}
	
	/** Checks whether a subject attribute complies with the condition. 
	 * @param tag The {@link OMMSubjectTag} to check. 
	 * @return True, if the subject attribute contains an entry compliant to this OMSQueryAttributeCondition. 
	 */
	private boolean checkSubject(OMMSubjectTag tag)
	{
		if (tag.getValue().equals(m_value)) return true;
		
		if (tag.getChild() != null) return checkSubject(tag.getChild());
		return false;
	}
}
