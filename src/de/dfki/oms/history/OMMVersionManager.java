package de.dfki.oms.history;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.codehaus.jackson.map.util.LRUMap;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.omm.events.OMMEvent;
import de.dfki.omm.events.OMMEventListener;
import de.dfki.omm.events.OMMEventType;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.impl.OMMImpl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMSourceType;
import de.dfki.oms.adom.ADOMContainer;
import de.dfki.oms.webapp.OMSOMMHandler;
import de.dfki.oms.webapp.Tools;

/** Manages versioning and history of an object memory. */
public class OMMVersionManager implements OMMEventListener
{
	protected static int MAX_SIZE = 8;
	/** A map containing all version managers for an OMS */
	protected static LRUMap<String, OMMVersionManager> MAP = new LRUMap<>(0, MAX_SIZE);
	
	protected final String m_memoryName;
	protected OMM m_currentVersion = null;
	protected OMMConfig m_config = null;
	protected OMMVersionManagerHistory m_historyState;
	
	/** Constructor. 
	 * 
	 * @param memoryName Name of the object memory. 
	 * @param init True, if memory should be saved initially (otherwise it is only loaded). 
	 * @param historyState A {@link OMMVersionManagerHistory} determining whether or not a version history is kept. 
	 */
	protected OMMVersionManager(String memoryName, boolean init, OMMVersionManagerHistory historyState)
	{
		m_historyState = historyState;
		m_memoryName = memoryName;
		m_config = new OMMConfig(memoryName, init);
		updateCurrentVersion();
	}
	
	/** Retrieves an existing or creates a new version manager for a given memory. 
	 * @param memoryName Name of the object memory. 
	 * @param historyState A {@link OMMVersionManagerHistory} determining whether or not a version history is kept. 
	 * @return The {@link OMMVersionManager} for the given memory. 
	 */
	public static synchronized OMMVersionManager create(String memoryName, OMMVersionManagerHistory historyState)
	{
		if (MAP.containsKey(memoryName))
		{
			return MAP.get(memoryName);
		}
		
		OMMVersionManager vm = new OMMVersionManager(memoryName, false, historyState);
		if (MAP.size() < MAX_SIZE) MAP.put(memoryName, vm);
		
		return vm;		
	}
	
	/** Uses an OMM event to initialize a new version manager. 
	 * @param event The {@link OMMEvent} to be the first update to the version manager.
	 * @param memoryName Name of the object memory. 
	 * @param historyState A {@link OMMVersionManagerHistory} determining whether or not a version history is kept. 
	 * @return The {@link OMMVersionManager} for the given memory. If there already is a manager, it is retrieved without changes. 
	 */
	public static synchronized OMMVersionManager createWithNewMemoryInfo(OMMEvent event, String memoryName, OMMVersionManagerHistory historyState){
		
		if (MAP.containsKey(memoryName))
		{
			return MAP.get(memoryName);
		}
		
		OMMVersionManager vm = new OMMVersionManager(memoryName, true, historyState);
		vm.updateOMM(event);
		if (MAP.size() < MAX_SIZE) MAP.put(memoryName, vm);

		return vm;	
	}
	
	/** Deletes the version manager for a given memory. 
	 * @param memoryName Name of the object memory. 
	 */
	public static void deleteMemoryVersionManager(String memoryName) {
		if (MAP.containsKey(memoryName))
		{
			MAP.remove(memoryName);
		}
	}
	
	/** Retrieves a list of all available memories in the memory folder. 
	 * @return A {@link List}<{@link String}> containing the memory names. 
	 */
	public static synchronized List<String> getAvailableMemories()
	{
		List<String> retVal = new LinkedList<>();
		
		for(File dir : new File(OMSOMMHandler.MEMORY_PATH).listFiles())
		{
			if (dir != null && dir.isDirectory()) 
			{
				//File f = new File(OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + dir);
				if (dir.listFiles().length > 1) retVal.add(dir.getName());
			} 
		}
		
		return retVal;
	}
	
	/** Hands a new memory to this version manager. 
	 * @param newXMLContent The new OMM as an XML representation {@link String}. 
	 * @param entity The {@link OMMEntity} responsible for the update. 
	 */
	public synchronized void setNewOMM(final String newXMLContent, final OMMEntity entity)
	{
		OMM omm = OMMFactory.loadOMMFromXmlFileString(newXMLContent, null, getCurrentVersionFile(), OMMSourceType.LocalFile);
		m_currentVersion = omm;
		updateOMM(new OMMEvent(omm, null, entity, OMMEventType.MEMORY_SYNC));
	}
	
 	/** Retrieves the name of this version manager's OMM.
 	 * @return The memory name. 
 	 */
 	public synchronized String getMemoryName()
	{
		return m_memoryName;
	}
	
	/** Retrieves the memory in its current version. 
	 * @return The {@link OMM}. 
	 */
	public synchronized OMM getCurrentVersion()
	{
		if (m_currentVersion == null)
		{
			updateCurrentVersion();
		}
		
		return m_currentVersion;
	}

	/** Retrieves the date of the last change to the memory.
	 * @return Creation {@link Date} of the current memory version. 
	 */
	public synchronized Date getCurrentVersionDate()
	{
		return m_config.getHistoryElement(m_config.getCurrentVersion()).OMMEntity.getDate();
	}
	
	/** Retrieves the most recent memory XML file.  
	 * @return Memory {@link File} in the current version. 
	 */
	public synchronized File getCurrentVersionFile()
	{
		final String currentDir = OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + m_memoryName;
		final String currentFileStr = currentDir + OMSOMMHandler.FILE_SEPARATOR + "v" + m_config.getCurrentVersion() + ".xml";
		final File currentFile = new File(currentFileStr);
		System.out.println("CurrentFile: "+currentFile.getAbsolutePath());
		return currentFile;
	}
	
	/** Retrieves the time span between the current time and the last change to the memory. 
	 * @return Time span as human readable {@link String}. 
	 */
	public synchronized String getLastChangeTimeSpan()
	{
		long today = new Date().getTime();
		long lastChange = new Date().getTime();
		if (getLastChangeEntity() != null) lastChange = getLastChangeEntity().getDate().getTime();
		
		return Tools.getFriendlyTime(today - lastChange) + " ago";
	}
	
	/** Retrieves the last OMM entity to change the memory. 
	 * @return The {@link OMMEntity}. 
	 */
	public synchronized OMMEntity getLastChangeEntity()
	{
		OMMHistoryElement he = m_config.getMostRecentHistoryElement();
		if (he == null) return null;
		return he.OMMEntity;
	}
	
	/** Retrieves a list of all history elements for the memory. 
	 * @return A {@link Collection}<{@link OMMHistoryElement}> containing all available history elements. 
	 */
	public synchronized Collection<OMMHistoryElement> getHistoryList()
	{
		return m_config.getHistoryElementsHM().values();
	}
	
	/** Retrieves the complete history of the memory. 
	 * @return A {@link SortedMap}<{@link String}, {@link OMMHistoryElement}> containing the version numbers and respective history elements. 
	 */
	public synchronized SortedMap<Integer, OMMHistoryElement> getHistory()
	{
		return m_config.getHistoryElementsHM();
	}
	
	/** Deletes all versions of the memory, starting from a given number. 
	 * @param versionToWithdraw Version number from which on to delete memories.
	 */
	public synchronized void withdrawNewerVersion(int versionToWithdraw)
	{
		if (versionToWithdraw > m_config.getCurrentVersion()) return;
		
		releaseCurrentVersion();
		
		for(int i = m_config.getCurrentVersion(); i >= versionToWithdraw; i--)
		{
			final String currentDir = OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + m_memoryName;
			final String currentFileStr = currentDir + OMSOMMHandler.FILE_SEPARATOR + "v" + i + ".xml";
			final File currentFile = new File(currentFileStr);

			if (currentFile.exists()) 
			{
				currentFile.delete();
			}	
		}
		
		m_config.setCurrentVersion(versionToWithdraw-1);
		m_config.clearHistory();
		updateCurrentVersion();
		
		ADOMeRestlet adome = ADOMContainer.getAdom(m_memoryName);
		if (adome == null) throw new IllegalStateException("ADOM is null");
		
		adome.setOMM(m_currentVersion);
	}
	
	@Override
	public synchronized void eventOccured(OMMEvent event)
	{
		updateOMM(event);
	}
		
	/** Retrieves a map of all OMMs and their respective date of last change. 
	 * @return {@link Map} of all {@link OMM}s and their current version {@link Date}s. 
	 */
	public synchronized static Map<OMM, Date> getOMMDateMap()
	{
		Map<OMM, Date> retVal = new HashMap<OMM, Date>();
		
		final String currentDir = OMSOMMHandler.MEMORY_PATH;
		File f = new File(currentDir);
		
		File[] files = f.listFiles(new FileFilter() { public boolean accept(File file) { return file.isDirectory(); } });
		
		for(File ommPath : files)
		{
			String f2 = ommPath.toString().replace(OMSOMMHandler.MEMORY_PATH+File.separator, "");
			
			OMMVersionManager vm = OMMVersionManager.create(f2, OMMVersionManagerHistory.Disabled);
			
			retVal.put(vm.getCurrentVersion(), vm.getCurrentVersionDate());
		}
		
		return retVal;
	}
		
	/** Retrieves the configuration object for the memory. 
	 * @return The {@link OMMConfig} connected to the memory. 
	 */
	public synchronized OMMConfig getConfig()
	{
		return m_config;
	}
	
	/** Reloads the current version of the memory. */
	protected synchronized void updateCurrentVersion()
	{
		releaseCurrentVersion();
		
		final File currentFile = getCurrentVersionFile();
		if (!currentFile.exists()) 
		{
			System.err.println("File not found! ("+currentFile+")");
			return;
		}		
		
		m_currentVersion = OMMFactory.loadOMMFromXmlFile(currentFile);		 
		((OMMImpl)m_currentVersion).addEventListener(this);
		
		// memory content changed -> update adome
	}
	
	/** Unloads the current version of the memory. */
	protected synchronized void releaseCurrentVersion()
	{
		if (m_currentVersion != null)
		{
			((OMMImpl)m_currentVersion).removeEventListener(this);
			m_currentVersion = null;
		}
	}

	/** Updates the memory's history and saves the new version after an event occured. 
	 * @param event The {@link OMMEvent} that caused a change to the memory. 
	 */
	protected synchronized void updateOMM(OMMEvent event)
	{
		if (m_historyState == OMMVersionManagerHistory.Enabled)
		{
			m_config.setCurrentVersion(m_config.getCurrentVersion() + 1);
		}
		
		OMMFactory.saveOMMToXmlFile(m_currentVersion, getCurrentVersionFile(), false);
		
		if (m_historyState == OMMVersionManagerHistory.Enabled)
		{
			// update history
			OMMHistoryElement h = new OMMHistoryElement(event.entity, event.block, event.type.toString());
			m_config.addHistoryElement(m_config.getCurrentVersion(), h);
		}		
	}
}


