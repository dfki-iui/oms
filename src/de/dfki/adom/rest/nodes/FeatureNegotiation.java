package de.dfki.adom.rest.nodes;

import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.API;
import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.HeaderControl;
import de.dfki.omm.impl.rest.OMMRestNegotiationData;
import de.dfki.omm.impl.rest.OMMRestNegotiationManagement;
import de.dfki.omm.impl.rest.OMMRestNegotiationStorage;

/** <p>A {@link Component} to handle the feature negotiation of an object memory via the REST interface. It provides information about the memory's version and its storage and management nodes.</p>
 * <p>Represents the "memory name" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class FeatureNegotiation extends Component {

	public static String 		RouteReplacement = null;
	public static String 		RouteSupplamental = null;
	private static final int	REFRESH_DATA_TIMESPAN_MSECS = 1000 * 60; // 1 minute

	protected Storage					m_storage;
	protected Management 				m_management;
	protected int						m_version;
	protected boolean					m_isDistributed, m_isFlushNecessary;
	protected OMMRestNegotiationData	m_negData;
	protected String					m_negDataJSONString;
	protected long					m_lastAccess;
	
	protected boolean 				m_secure;
	
	
	/** Constructor.
	 * 
	 * @param adom The {@link ADOMeRestlet} for which this component offers functionality. 
	 * @param secure True, if memory uses "https".
	 */
	public FeatureNegotiation(ADOMeRestlet adom, boolean secure) {
		super(adom);
		
		m_storage			= adom.getStorage();
		m_management		= adom.getManagement();
		m_version 			= 1;
		m_isDistributed		= false;
		m_isFlushNecessary	= false;		
		m_secure 			= secure;
	}

	

	
	/**
	 * Handles the DELETE request to the feature negotiation (deletion of the whole memory).
	 * @param request The request
	 * @param response The response
	 */
	@Delete
	public void doDelete(Request request, Response response) {  

		String memoryName = getADOM().getMemoryName();

		// don't delete if deletion is disabled
		if (ADOMeRestlet.MemoryAccess.IsDeleteDisabled(memoryName)) {
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "OMM deletion disabled");
		}
		
		else { // delete whole memory
			ADOMeRestlet.MemoryAccess.deleteMemory(memoryName);
			response.setEntity("Memory "+memoryName+" successfully deleted", MediaType.TEXT_PLAIN);
		}
	}
	
	/**
	 * Handles the GET request to the feature negotiation (gets an information string on the memory).
	 * @param request
	 * @param response
	 */
	@Get
	public void doGet(Request request, Response response) {  
	  ADOMeRestlet adom = getADOM();
	  API api = adom.getAPI();
	  boolean secure = false;
	  if (request.getResourceRef().toString().startsWith("https://"))
	  {
		  secure = true;
	  }
	  String message = api.getFeatureNegotiation(secure);
		response.setEntity(message, MediaType.APPLICATION_JSON);
		HeaderControl.AddAccessControlAllowOrigin(response);
		return;
	}
	
	
	
	/**
	 * Delivers the feature negotiation string of an ADOM.
	 * @return the feature negotiation string.
	 */
	public String get() {
		long now = new GregorianCalendar().getTime().getTime();
		
		if (now - m_lastAccess > REFRESH_DATA_TIMESPAN_MSECS)
		{
			m_lastAccess = now;
			updateJSONData();
		}
		
		return m_negDataJSONString;
	}
	
	protected void updateJSONData()
	{
		OMMRestNegotiationStorage storage = OMMRestNegotiationStorage.create(setupPath(m_storage.getPath()), getCapacity(), getFreeSpace(), m_isDistributed, ADOMeRestlet.MemoryAccess.IsDeleteDisabled(getADOM().getMemoryName()));
		OMMRestNegotiationManagement man = OMMRestNegotiationManagement.create(setupPath(m_management.getPath()), m_isFlushNecessary);
		
		m_negData = OMMRestNegotiationData.create(m_version, storage, man);
		m_negDataJSONString = m_negData.toJSONString();
	}
	
	protected String setupPath(String path)
	{
		if (path == null) return null;		
		if (RouteReplacement != null) path = path.replace(Config.getBaseURL(), RouteReplacement);
		if (RouteSupplamental != null) path = path.replace(Config.getBaseURL(), Config.getBaseURL()+RouteSupplamental);
		if (m_secure)
		{
			if (!path.startsWith("https://"))
			{
				path = path.replace("//", "/");
				return "https://"+path;
			}
		}
		else
		{
			if (!path.startsWith("http://"))
			{
				path = path.replace("//", "/");
				return "http://"+path;
			}
		}
		
		return path;
	}
	
	protected long getFreeSpace()
	{
		try
	    {
			Path root = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
	        FileStore store = Files.getFileStore(root);
	        long freeSpace = store.getUsableSpace();
	        return freeSpace;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
		
		return Long.MIN_VALUE;
	}
	
	protected long getCapacity()
	{
		try
	    {
			Path root = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
	        FileStore store = Files.getFileStore(root);
	        long capacity = store.getTotalSpace();
	        return capacity;	        
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
		
		return Long.MIN_VALUE;
	}

}
