package de.dfki.oms.tools;

import java.util.HashMap;

/** Caches key-value-pairs of Strings and Objects. */
public class UploadCache { // TODO This class is not used anywhere
	
	private static HashMap<String, Object> UPLOADCACHE = new HashMap<String, Object>();
	
	/** Adds another entry to the cache. 
	 * 
	 * @param key Key of the new entry.
	 * @param value Value of the new entry.
	 */
	public static void PushCache(String key, Object value){
		UPLOADCACHE.put(key, value);
	}
	
	/** Retrieves an entry from the cache. 
	 * 
	 * @param key Key to look up in the cache. 
	 * @return Value to which the specified key is mapped, or <code>null</code> if none is found. 
	 */
	public static Object PopCache(String key){
		return UPLOADCACHE.get(key);
	}
}
