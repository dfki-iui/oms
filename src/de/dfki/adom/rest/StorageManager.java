/**
 * 
 */
package de.dfki.adom.rest;

import de.dfki.omm.events.OMMEventListener;

/** Manager for the storage of an ADOM, responsible for saving changes following a given save policy. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public interface StorageManager extends OMMEventListener {

	/**
	 * Returns a String describing the storage policy of the
	 * specific Class implementing StorageManager.
	 * @return String describing the storage policy.
	 */
	public String storagePolicy();
	
	/**
	 * Saves the memory to a file with the given fileName.
	 * @param fileName as String
	 */
	public void save(String fileName);
	
}
