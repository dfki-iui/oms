package de.dfki.oms.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A query object modeling an OMS wide search operation. */
public class OMSQuery
{
	private String m_resultType = null;
	private List<OMSQueryCondition> m_conditions = null;
	
	
	/** Constructor.
	 * @param jsonQuery The query as a JSON {@link String}. 
	 */
	public OMSQuery(final String jsonQuery)
	{
		if (jsonQuery == null) throw new IllegalArgumentException("OMS Query string is empty!");
		
		try
		{
			initialize(jsonQuery.getBytes("utf-8"));			
		} 
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	/** Constructor.
	 * @param jsonQuery The query as a JSON {@link byte[]}. 
	 */
	public OMSQuery(final byte[] jsonQuery)
	{
		initialize(jsonQuery);
	}
	
	
	/** Retrieves the result type of this query, for example "omm" for a primary ID as result or "omm.blockcount" for the number of block in an object memory. 
	 * @return Result type as {@link String}. 
	 */
	public final String getResultType() { return m_resultType; }
	
	/** Retrieves the search criteria for this query.
	 * @return Conditions as a {@link List} of {@link OMSQueryCondition}s.
	 */
	public final List<OMSQueryCondition> getConditions() { return m_conditions; }
		
	
	/** Initializes a query object by parsing the query in JSON format.
	 * @param jsonQuery Query as a {@link byte[]} containing a JSON String. 
	 */
	@SuppressWarnings("unchecked")
	private void initialize(final byte[] jsonQuery)
	{		
		if (jsonQuery.length < 1) return;
		
		try
		{
			final Map<String, Object> userData = OMSQueryProcessor.JSON_OBJECT_MAPPER.readValue(jsonQuery, Map.class);
			parse(userData);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/** Sets the internal variables for the query's result type and (by calling {@link #parseWhere}) conditions.
	 * @param userData Entered query as parsed {@link Map}<{@link String}, {@link Object}>. 
	 */
	private void parse(final Map<String, Object> userData)
	{
		if (!userData.containsKey("select")) return;
		m_resultType = (String)userData.get("select");
		
		if (!userData.containsKey("where"))
			parseWhere(new ArrayList<>()); // if now "where"-statement given --> no conditions
		else
			parseWhere(userData.get("where"));
	}
	
	/** Sets the internal variable for the query's conditions.
	 * @param where Conditions as an {@link ArrayList} (else parsing will not succeed). 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parseWhere(final Object where)
	{
		if (!(where instanceof ArrayList)) throw new IllegalArgumentException("where part is not valid!");
		
		ArrayList list = (ArrayList)where;
		
		for(Object entry : list)
		{
			if (entry instanceof Map)
			{
				for(Object mapEntry : ((Map)entry).entrySet())
				{
					OMSQueryCondition cond = OMSQueryCondition.create((Map.Entry<String, Object>)mapEntry);
					if (cond != null)
					{
						if (m_conditions == null) m_conditions = new ArrayList<OMSQueryCondition>();
						m_conditions.add(cond);
					}
				}
			}
		}
	}
	
	/** <p>Converts an {@link OMSQuery} object to a String of the form: </p>
	 * <p>"[OMSQuery]<br>
	 *   -Resulttype: '[type]'<br>
	 *   -Conditions: [list of conditions]"</p>
	 */
	@Override
	public String toString()
	{
		StringBuffer retVal = new StringBuffer();
		retVal.append("[OMSQuery]\n");
		
		retVal.append("  -Resulttype: '"+m_resultType + "'\n");
		
		retVal.append("  -Conditions:\n");
		
		for(OMSQueryCondition cond : m_conditions)
		{
			retVal.append("    * " + cond.toString() + "\n");
		}
		
		return retVal.toString();
	}
}
