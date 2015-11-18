/**
 * 
 */
package de.dfki.adom.rest;

import java.io.File;

import de.dfki.omm.events.OMMEvent;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMM;

/** An empty version of a {@link StorageManager} saving only when {@link #save} is explicitly called. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class EmptyStorageManager implements StorageManager {

	ADOMeRestlet m_adom = null;

	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} for which this manager handles the storage. 
	 */
	public EmptyStorageManager(ADOMeRestlet adom) {
		m_adom = adom;
	}

	
	@Override
	public void eventOccured(OMMEvent event) {
		// Do explicitly NOTHING !
	}

	
	@Override
	public String storagePolicy() {
		String policy = "Do NOT save after every change, only when " +
				"save() is explicitely called.";		
		return policy;
	}


	@Override
	public void save(String fileName) {
		
		String file_name = fileName;
		if (file_name == null) file_name = "default_storage.xml";
		
		File file = new File(Config.MemoryPath + m_adom.getMemoryName() + "/" + file_name);
		OMM omm = m_adom.getOMM();
		OMMFactory.saveOMMToXmlFile(omm, file, false);
	}
}
