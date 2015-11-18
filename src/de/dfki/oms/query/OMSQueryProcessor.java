package de.dfki.oms.query;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import de.dfki.omm.interfaces.OMM;

/** 
 */
public class OMSQueryProcessor
{
	public static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

	/** Processes a query and provides the result as a JSON String.
	 *  
	 * @param query The {@link OMSQuery} to process.
	 * @param ommList A {@link Map}<{@link OMM}, {@link Date}> containing all OMMs to test the query against.
	 * @return Query results as a JSON {@link String}. 
	 */
	public static String processAsJson(final OMSQuery query, final Map<OMM, Date> ommList)
	{
		return generateResult(processAsPojo(query, ommList));
	}
	
	/** Processes a query and provides the result as a Plain Old Java Object.
	 *  
	 * @param query The {@link OMSQuery} to process.
	 * @param ommList A {@link Map}<{@link OMM}, {@link Date}> containing all OMMs to test the query against.
	 * @return Query results as a {@link List} of {@link OMM}s. 
	 */
	public static List<OMM> processAsPojo(final OMSQuery query, final Map<OMM, Date> ommList)
	{
		List<OMM> successList = new LinkedList<>(); 
				
		for(OMM omm : ommList.keySet())
		{
			if (processInternal(query, omm, ommList.get(omm))) successList.add(omm);
		}
		
		return successList;
	}
	
	/** Processes a query and provides the result as a JSON String.
	 *  
	 * @param query The {@link OMSQuery} to process.
	 * @param ommList A {@link Map}<{@link OMM}, {@link Date}> containing all OMMs to test the query against.
	 * @param validAtDate Validity {@link Date} of the memory. 
	 * @return Query results as a JSON {@link String}. 
	 */
	public static String processAsJson(final OMSQuery query, final OMM omm, final Date validAtDate)
	{
		return generateResult(processAsPojo(query, omm, validAtDate));
	}
	
	/** Processes a query and provides the result as a Plain Old Java Object.
	 *  
	 * @param query The {@link OMSQuery} to process.
	 * @param ommList A {@link Map}<{@link OMM}, {@link Date}> containing all OMMs to test the query against.
	 * @param validAtDate Validity {@link Date} of the memory. 
	 * @return Query results as a {@link List} of {@link OMM}s. 
	 */
	public static List<OMM> processAsPojo(final OMSQuery query, final OMM omm, final Date validAtDate)
	{
		if (omm == null) throw new IllegalArgumentException("Given OMM is null!");
		
		if (processInternal(query, omm, validAtDate))
		{
			List<OMM> list = new LinkedList<OMM>();
			list.add(omm);
			return list;
		}			
		
		return null;
	}
	
	/** Configures the JSON object mapper's use of indentation. 
	 * @param value True, if indentation is to be used, otherwise false. 
	 */
	public static void setIndentationEnabled(boolean value)
	{
		JSON_OBJECT_MAPPER.configure(SerializationConfig.Feature.INDENT_OUTPUT, value);
	}
	
	
	/** Processes the query on a given OMM.
	 * 
	 * @param query The {@link OMSQuery} to process.
	 * @param omm The {@link OMM} to test the query against.
	 * @param validAtDate Validity {@link Date} of the memory. 
	 * @return True, if the memory complies with the query's conditions. 
	 */
	private static boolean processInternal(final OMSQuery query, final OMM omm, final Date validAtDate)
	{
		boolean success = true;
		
		if (query.getConditions() == null) return true;
		
		for(OMSQueryCondition cond : query.getConditions())
		{
			if (cond != null) success = success && cond.check(omm, validAtDate);
		}
		
		return success;
	}
	
	/** Converts a query's result (list of memory IDs) into a JSON String.  
	 * 
	 * @param ommList {@link List} of {@link OMM} that have been found to comply the query's conditions. 
	 * @return Result list as JSON {@link String}. 
	 */
	private static String generateResult(final List<OMM> ommList)
	{
		if (ommList == null || ommList.size() < 1) return null;
		
		Map<String, Object> userData = new HashMap<String, Object>();
		List<String> resultList = new LinkedList<String>();
		
		for(OMM omm : ommList)
		{
			resultList.add(omm.getHeader().getPrimaryID().toString());
		}
		
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		userData.put("result", resultList);
		
		try
		{			
			JSON_OBJECT_MAPPER.writeValue(boas, userData);
			return boas.toString("utf-8");
		}
		catch(Exception e){ e.printStackTrace();}
		
		return null;
	}
}
