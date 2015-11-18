package de.dfki.oms.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dfki.omm.impl.OMMBlockImpl;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.tools.OMMXMLConverter;
import de.dfki.omm.types.OMMEntity;
import de.dfki.oms.webapp.OMSOMMHandler;

/** Handles a memory's versioning and persistence. */
public class OMMConfig
{
	protected int m_currentVersion = 0;
	protected String m_memoryName = null;
	protected SortedMap<Integer, OMMHistoryElement> m_history = null; 
	protected OMMBlock m_ownerBlock = null;
	protected boolean m_deleteDisabled = false;
	
	/** Constructor. 
	 * 
	 * @param memoryName Name of the object memory to configure. 
	 * @param init True, if memory should be saved initially (otherwise it is only loaded). 
	 * @throws IllegalArgumentException If the memory does not exist.
	 */
	public OMMConfig(final String memoryName, boolean init) throws IllegalArgumentException
	{
		this.m_memoryName = memoryName;
		try 
		{
			if(init){
				saveData();
				loadData();
			}else{
				loadData();
			}
		}
		catch(FileNotFoundException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	

	/** Retrieves the memory's current version. 
	 * @return Version number as an <code>int</code>. 
	 */
	public int getCurrentVersion()
	{
		return m_currentVersion;
	}
	
	/** Sets the current version to a new value and issues a save operation.
	 * @param version The new version number. 
	 */
	public void setCurrentVersion(int version)
	{
		m_currentVersion = version;
		saveData();
	}
	
	/** Checks whether the memory can be deleted.
	 * @return True, if memory deletion is disabled. 
	 */
	public boolean isDeleteDisabled()
	{
		return m_deleteDisabled;
	}
	
	/** Retrieves the owner information of the object memory in block form.
	 * @return Owner information as {@link OMMBLock}. 
	 */
	public OMMBlock getOwnerBlock()
	{
		if (m_ownerBlock == null) return null;
		return OMMXMLConverter.parseBlock(OMMXMLConverter.generateCompleteBlock(m_ownerBlock, true).getDocumentElement());
	}	
	
	/** Sets the owner information of the object memory and issues a save operation.
	 * @param block New owner information as {@link OMMBLock}. 
	 */
	public void setOwnerBlock(OMMBlock block)
	{
		m_ownerBlock = block;
		saveData();
	}	

	/** Retrieves the memory's history element for a given version. 
	 * @param version The version as an <code>int</code>.
	 * @return The {@link OMMHistoryElement} belonging to that version (or <code>null</code> if none can be found). 
	 */
	public OMMHistoryElement getHistoryElement(int version)
	{
		if (m_history.containsKey(version)) return m_history.get(version);
		
		return null;
	}
	
	/** Retrieves all history elements of the object memory. 
	 * @return A {@link SortedMap}<{@link Integer}, {@link OMMHistoryElement}> mapping version number to respective history element. 
	 */
	public SortedMap<Integer, OMMHistoryElement> getHistoryElementsHM()
	{
		return m_history;
	}
	
	/** Retrieves the memory's history element for the current version. 
	 * @return The current {@link OMMHistoryElement}. 
	 */
	public OMMHistoryElement getMostRecentHistoryElement()
	{
		return getHistoryElement(m_currentVersion);
	}
	
	/** Adds a history element for a memory version. 
	 * @param version The version for which to add the history.
	 * @param element The {@link OMMHistoryElement} for that version. 
	 */
	public void addHistoryElement(int version, OMMHistoryElement element)
	{
		m_history.put(version, element);
		saveData();
	}
	
	/** Deletes the history elements for all versions of the object memory and issues a save operation. */
	public void clearHistory()
	{
		for(int i = m_history.lastKey(); i > m_currentVersion; i--)
		{
			m_history.remove(i);
		}
		saveData();
	}
	
	/** Deletes the memory and its whole folder. */
	public void deleteMemory () {

		final String currentDir = OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + m_memoryName;
		File memoryFolder = new File(currentDir);

		// recursively delete memory directory and all files in it
		if (!m_deleteDisabled) {
			deleteData(memoryFolder);
		}
	}
	

	/** Loads an OMM using its "info.xml" file. 
	 * @throws FileNotFoundException If "info.xml" could not be found. 
	 */
	protected void loadData() throws FileNotFoundException
	{
		final String currentDir = OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + m_memoryName;
		final String currentFileStr = currentDir + OMSOMMHandler.FILE_SEPARATOR + "info.xml";
		final File currentFile = new File(currentFileStr);
		
		if (!currentFile.exists())
		{
			throw new FileNotFoundException("Memory '"+m_memoryName+"' not found!");
		}
		
		try
		{			
			Document doc = OMMXMLConverter.getXmlDocumentFromString(new FileInputStream(currentFile));
			Element root = doc.getDocumentElement();
			if (!root.getNodeName().equals(OMMXMLConverter.OMM_NAMESPACE_PREFIX+":oms")) throw new IllegalArgumentException("info.xml file is not valid");
			
			NodeList nl = root.getChildNodes();
			for(int i = 0; i < nl.getLength(); i++)
			{
				Object child = nl.item(i); 
				if (child instanceof Element)	
				{
					Element cElement = (Element)child;
					switch(cElement.getNodeName())
					{
						case "currentVersion":
							m_currentVersion = Integer.parseInt(cElement.getTextContent());
							break;
						case "history":
							loadHistory(cElement);
							break;
						case "memoryProperties":
							loadMemoryProperties(cElement);
							break;
						case "ownership":
							loadOwnership(cElement);
							break;						
					}
				}
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	/** Loads the memory's owner information from the "ownership" XML Element representing an owner block. 
	 * @param root Root node of the block {@link Element}. 
	 */
	protected void loadOwnership(Element root)
	{
		NodeList nl = root.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++)
		{
			Object child = nl.item(i); 
			if (child instanceof Element)	
			{
				m_ownerBlock = (OMMBlockImpl)OMMXMLConverter.parseBlock((Element)child);
				return;
			}
		}		
	}
	
	/** Loads the memory's history from the "history" XML Element representing history information. 
	 * @param root Root node of the {@link Element}. 
	 */
	protected void loadHistory(Element root)
	{
		m_history = new TreeMap<>();
		NodeList nl = root.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++)
		{
			Object child = nl.item(i); 
			if (child instanceof Element)	
			{
				Element cElement = (Element)child;
				if (cElement.getNodeName().equals("historyEntry"))
				{
					int id = Integer.parseInt(cElement.getAttribute("version"));
					OMMEntity entity = loadEntity(OMMXMLConverter.findChild(cElement, "entity"));
					String change = OMMXMLConverter.findChild(cElement, "change").getTextContent();
					
					OMMHistoryElement ohe = new OMMHistoryElement(entity, null, change);
					m_history.put(id, ohe);
				}
			}
		}
	}

	/** Loads a memory's properties (deletion policy) from the "memoryProperties" XML Element. 
	 * @param root Root node of the entry {@link Element}. 
	 */
	protected void loadMemoryProperties(Element root)
	{
		NodeList nl = root.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++)
		{
			Object child = nl.item(i); 		
			if (child instanceof Element)	
			{
				Element cElement = (Element)child;
				switch (cElement.getNodeName())
				{
					case "deleteDisabled":
						m_deleteDisabled = true;
						break;
				}
			}
		}
	}
	
	/** Retrieves an OMM contributor entity from an "entity" XML Element.
	 * @param root Root node of the {@link Element}. 
	 * @return The parsed {@link OMMEntity}. 
	 */
	protected OMMEntity loadEntity(Element root)
	{
		Element contributor = OMMXMLConverter.findChild(root, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":contributor");
		Element date = OMMXMLConverter.findChild(root, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":date");
		
		String type = contributor.getAttribute(OMMXMLConverter.OMM_NAMESPACE_PREFIX+":type");
		String value = contributor.getTextContent();
		
		OMMEntity entity = new OMMEntity(type, value, date.getTextContent());
		
		return entity;
	}
	
	/** Saves all current memory data to the OMM's "info.xml" file. */
	protected void saveData()
	{
		final String currentDir = OMSOMMHandler.MEMORY_PATH + OMSOMMHandler.FILE_SEPARATOR + m_memoryName;
		final String currentFileStr = currentDir + OMSOMMHandler.FILE_SEPARATOR + "info.xml";
		final File currentFile = new File(currentFileStr);
		
		Document doc = OMMXMLConverter.createNewXmlDocument();
		
		Element root = OMMXMLConverter.createXmlElementAndAppend(doc, "oms", OMMXMLConverter.OMM_NAMESPACE_PREFIX, OMMXMLConverter.OMM_NAMESPACE_URI);
		
		Element cV = OMMXMLConverter.createXmlElement(doc, "currentVersion");
		cV.setTextContent(m_currentVersion+"");
		
		root.appendChild(cV);

		Element hist = OMMXMLConverter.createXmlElement(doc, "history");
		if(m_history == null){
			m_history= new TreeMap<>();
		}
		for(Map.Entry<Integer, OMMHistoryElement> he : m_history.entrySet())
		{
			Element heXml = OMMXMLConverter.createXmlElement(doc, "historyEntry");
			heXml.setAttribute("version", he.getKey().toString());
			
			Element ent = OMMXMLConverter.createXmlElement(doc, "entity");
			Element cont = OMMXMLConverter.createXmlElement(doc, "contributor", OMMXMLConverter.OMM_NAMESPACE_PREFIX, OMMXMLConverter.OMM_NAMESPACE_URI);
			if (he.getValue().OMMEntity == null)
			{
				System.err.println("MISSING ENTITY");
				continue;
			}
			cont.setAttributeNS(OMMXMLConverter.OMM_NAMESPACE_URI, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":type", he.getValue().OMMEntity.getType());
			cont.setTextContent(he.getValue().OMMEntity.getValue());
			ent.appendChild(cont);
			
			Element date = OMMXMLConverter.createXmlElement(doc, "date", OMMXMLConverter.OMM_NAMESPACE_PREFIX, OMMXMLConverter.OMM_NAMESPACE_URI);
			date.setAttributeNS(OMMXMLConverter.OMM_NAMESPACE_URI, OMMXMLConverter.OMM_NAMESPACE_PREFIX+":encoding", "ISO8601");
			date.setTextContent(he.getValue().OMMEntity.getDateAsISO8601());
			ent.appendChild(date);
			
			heXml.appendChild(ent);
			
			Element change = OMMXMLConverter.createXmlElement(doc, "change");
			change.setTextContent(he.getValue().Change);
			heXml.appendChild(change);
			
			hist.appendChild(heXml);
		}
		
		root.appendChild(hist);
		
		if (m_ownerBlock != null)
		{
			Element ownership = OMMXMLConverter.createXmlElement(doc, "ownership");
			ownership.appendChild(doc.importNode(OMMXMLConverter.generateCompleteBlock(m_ownerBlock, true).getDocumentElement(),true));
			root.appendChild(ownership);
		}
		
		// no access control
		Element accessControl = OMMXMLConverter.createXmlElement(doc, "accessControl");
		accessControl.appendChild(OMMXMLConverter.createXmlElement(doc, "noRestriction"));	  
		root.appendChild(accessControl);
		
		Element memoryProperties = OMMXMLConverter.createXmlElement(doc, "memoryProperties");
		if (m_deleteDisabled) 
			accessControl.appendChild(OMMXMLConverter.createXmlElement(doc, "deleteDisabled"));
		root.appendChild(memoryProperties);
		
		try 
		{
			FileWriter fw = new FileWriter(currentFile);
			fw.write(OMMXMLConverter.toXMLFileString(doc));
			fw.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	
	
	/** Deletes the whole memory recursively. 
	 * @param data The folder to delete. 
	 */
	protected void deleteData(File data) {
		
//		System.out.println("~~~~ now deleting: "+data.getName());
		
		File[] contents = data.listFiles();
	    if (contents != null) {
	        for (File f : contents) deleteData(f);
	    }
	    
	    data.delete();
	}
	
}
