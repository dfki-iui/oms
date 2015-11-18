package de.dfki.adom.rest;

import org.restlet.Restlet;
import org.restlet.routing.Router;

import de.dfki.adom.rest.nodes.FeatureNegotiation;
import de.dfki.adom.rest.nodes.Management;
import de.dfki.adom.rest.nodes.Storage;
import de.dfki.adom.rest.nodes.Sync;
import de.dfki.omm.events.OMMEvent;
import de.dfki.omm.events.OMMEventListener;
import de.dfki.omm.impl.OMMImpl;
import de.dfki.omm.interfaces.OMM;

/**
 * Restlet to represent a single OMM in the REST interface. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class ADOMeRestlet extends Restlet implements OMMEventListener {
	
	// --- class members ---
	public static MemoryAccess MemoryAccess = null;
	
	// --- object members ---
	protected StorageManager m_storageManager = null;
	protected OMMImpl m_omm = null;
	
	protected API m_api;
	protected String m_memoryName;
	protected String m_url;
	
	protected FeatureNegotiation m_featureNegotiation, m_featureNegotiationSecure;
	protected Storage m_storage;
	protected Management m_management;
	
	protected Sync m_sync;

	/**
	 * Constructor for an ADOM with the given name using the given memory access.
	 * 
	 * @param memoryName The OMM's name
	 * @param memoryAccess Memory Access Mode
	 * @param heartBeatRate The heart beat rate
	 */
	public ADOMeRestlet(String memoryName, MemoryAccess memoryAccess, long heartBeatRate) {

		m_memoryName = memoryName;
		m_url = "/" + m_memoryName;
		
		if (ADOMeRestlet.MemoryAccess == null)
			ADOMeRestlet.MemoryAccess = memoryAccess;

		init(heartBeatRate);
	}
	
	/**
	 * Constructor for an ADOM with the given name using the given memory access.
	 * 
	 * @param memoryName The OMM's name
	 * @param memoryAccess Memory Access Mode
	 */
	public ADOMeRestlet(String memoryName, MemoryAccess memoryAccess) {
		this(memoryName, memoryAccess, Config.DEFAULT_HEARTBEAT);
	}
	
	/**
	 * Initializes all relevant fields of the ADOMe Restlet.
	 *  
	 * @param heartBeatRate
	 */
	protected void init(long heartBeatRate) {

		m_omm = (OMMImpl) this.getOMM();
		m_api = new API(this);
		// this.m_api.addObserver(new LoggingAPIObserver(this.m_memoryName +
		// ".log.json"));
		
		m_storage = new Storage(this);
		m_management = new Management(this);
		m_sync = new Sync(this);
		m_featureNegotiation = new FeatureNegotiation(this, false);
		m_featureNegotiationSecure = new FeatureNegotiation(this, true);
	
		m_omm.addEventListener(this);
		
		// Initialize stand-alone ADPG on Raspberry Pi
		// rpiInit();
	}
	
	// private void rpiInit() {
	// RPiClient rpi_client;
	// if (m_memoryName.equals("rolle") || m_memoryName.equals("band")) {
	// rpi_client = new RPiClient();
	// rpi_client.initialize();
	//
	// Timer timer = new Timer();
	//
	// HeartBeat heart_beat = new HeartBeat(timer);
	// heart_beat.addClient(rpi_client);
	//
	// timer.schedule(heart_beat, 1000, 1000);
	// } else
	// return;
	// }
	
	/**
	 * Initialize Stand-Alone-Mode: Handle Memory-Storage without an OMS.
	 * 
	 * @param storageManager StorageManager to use (or null for Stand-Alone-Mode)
	 */
	public void initStandAloneMode(StorageManager storageManager) {
		if (storageManager == null) {
			System.out.println("ADOM initialized for Stand-Alone-Mode without storage management");
			return;
		}
		
		m_storageManager = storageManager;
		m_omm.addEventListener(storageManager);
		System.out.println("ADOM Stand-Alone-Mode initialized in default storage policy: " + m_storageManager.storagePolicy());
	}
		
	/** 
	 * Saves the memory contents to a file.
	 * 
	 * @param fileName Name of the output file.  
	 * @return True, if contents have been saved. False, if there is nothing to save. 
	 */
	public boolean save(String fileName) {
		if (m_storageManager != null) {
			m_storageManager.save(fileName);
			return true;
		}
		
		return false;
	}
		
	/**
	 * Attach this object and its subcomponents to the given router.
	 * 
	 * @param router The router to attach to
	 */
	public void attach(Router router) {
		m_featureNegotiation.attach(router, m_url);
		m_storage.attach(router, m_url + "/st");
		m_management.attach(router, m_url + "/mgmt");
		m_sync.attach(router, m_url + "/sync");
	}
	
	/**
	 * Returns the API object of the ADOM.
	 * 
	 * @return The API object of the ADOM.
	 */
	public API getAPI() {
		return m_api;
	}
	
	/**
	 * Returns the OMM associated with this ADOM.
	 * 
	 * @return The OMM associated with this ADOM.
	 */
	public OMMImpl getOMM() {
		
		if (m_omm == null) {
			String mem_name = this.getMemoryName();
			MemoryAccess mem_access = ADOMeRestlet.MemoryAccess;
			m_omm = (OMMImpl) mem_access.getMemory(mem_name);
		}
		
		return m_omm;
	}
	
	/**
	 * Sets the OMM associated with this ADOM.
	 * 
	 * @param omm The OMM associated with this ADOM.
	 */
	public void setOMM(OMM omm) {
		m_omm = (OMMImpl) omm;
	}
	
	/**
	 * Resets the OMM associated with this ADOM to null.
	 */
	public void resetOMM() {
		m_omm = null;
	}
	
	/**
	 * Returns the memory name of the ADOM.
	 * 
	 * @return The memory name of the ADOM.
	 */
	public String getMemoryName() {
		return m_memoryName;
	}
	
	// /**
	// * Returns the memory access to to physical storage lying under the ADOM.
	// * @return The memory access to to physical storage lying under the ADOM.
	// */
	// private MemoryAccess getMemoryAccess() {
	// return ADOM.MemoryAccess;
	// }
	
	/**
	 * Returns the Storage component of the ADOM.
	 * 
	 * @return The Storage component of the ADOM.
	 */
	public Storage getStorage() {
		return m_storage;
	}
	
	/**
	 * Returns the Management component of the ADOM.
	 * 
	 * @return The Management component of the ADOM.
	 */
	public Management getManagement() {
		return m_management;
	}
	
	/**
	 * Returns the Sync component of the ADOM.
	 * 
	 * @return The Sync component of the ADOM.
	 */
	public Sync getSync() {
		return m_sync;
	}
	
	/**
	 * Returns the FeatureNegotiation component of the ADOM.
	 * 
	 * @param secure True for secure mode
	 * @return The FeatureNegotiation component of the ADOM.
	 */
	public FeatureNegotiation getFeatureNegotiation(boolean secure) {
		if (secure)
			return m_featureNegotiationSecure;
		else
			return m_featureNegotiation;
	}

	@Override
	public void eventOccured(OMMEvent event) { }	
}
