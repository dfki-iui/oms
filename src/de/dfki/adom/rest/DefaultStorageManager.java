package de.dfki.adom.rest;

import java.io.File;

import de.dfki.omm.events.OMMEvent;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMM;

/** Default version of a {@link StorageManager} saving the storage after every change.
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class DefaultStorageManager implements StorageManager { // TODO This class is not used anywhere

	ADOMeRestlet m_adom = null;
	static Integer index = 0;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} for which this manager handles the storage. 
	 */
	public DefaultStorageManager(ADOMeRestlet adom) {
		m_adom = adom;
	}

	
	@Override
	public void eventOccured(OMMEvent event) {
		System.out.println("DefaultStorageManager: Save memory!");
		this.save("stand_alone_" + index + ".xml");
		index ++;
	}


	@Override
	public String storagePolicy() {
		String policy = "Save memory after every change.";		
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
