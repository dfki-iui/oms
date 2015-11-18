package de.dfki.adom.rest;

/** Offers data and information that is valid for the whole memory, such as ports and filepaths. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Config {

	private static String  m_host = "localhost";
	public static int PORT = 10082;
	public static int PORT_SSL = 10083;
	
	public static String MemoryPath   = "resources/memories/";
	public static String SampleMemory = "sample";
//	public static String SampleMemory = "luaExecutionExample";
	public static String SampleFile   = "/v0.xml";
	
	public static String RessourcePath = "resources";
	
  public static final String LUA_INDICATION_PREFIX = "lua.";
  public static final String LUA_NAMESPACE = "urn:adome:block:featuremodule:lua";
  public static final String CONFIGURATION_NAMESPACE = "urn:adome:block:configuration";
  public static final String HEARTBEATTASK_NAME = "heartbeattask";
  public static final String TRIGGERTASK_NAME = "triggertask";
  public static final String LUA_EXECUTION_NAME = "luaexecution";
  public static final String STEP_NAME = "interval";
  public static final String LIMIT_NAME = "limit";
  public static final String SANDBOX_PATH = "resources/lua/sandbox.lua";
  public static final long DEFAULT_HEARTBEAT = 500;
	
	/**
	 * Sets the host for the ADOM configuration.
	 * @param host as String
	 */
	public static void setHost(String host) {
		m_host = host;
	}
	
	
	/**
	 * Returns the host URL of the ADOM.
	 * @return The host URL of the ADOM.
	 */
	public static String getHostURL() {
		return "http://" + m_host;
	}

	
	/**
	 * Returns the base URL of an ADOM.
	 * @return The base URL of an ADOM.
	 */
	public static String getBaseURL() {
		String url = m_host;
		
		url = url + ":" + PORT;			
		
		url = url + "/";
		
		return url;
	}
	

	public static void main(String[] args) {		
		System.out.println("ADOM Config");
		System.out.println("  Host: \t" + Config.getHostURL());
		System.out.println("  Port: \t" + Config.PORT);
		System.out.println("  URL:  \t" + Config.getBaseURL());
	}
}
