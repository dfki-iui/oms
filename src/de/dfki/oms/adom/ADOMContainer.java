package de.dfki.oms.adom;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.MemoryAccess;
import de.dfki.adom.rest.Start;
import de.dfki.adom.rest.nodes.FeatureNegotiation;
import de.dfki.adom.rest.nodes.ListMemories;
import de.dfki.adom.security.ACLAuthentication;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.history.OMMVersionManagerHistory;

/** Organizes and provides access to the {@link ADOMeRestlet}s representing the OMS's object memories. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class ADOMContainer extends Application {

	protected ArrayList<Restlet> m_restletList = null;
	protected static Map<String, ADOMeRestlet> m_adoms = new HashMap<String, ADOMeRestlet>();
	protected static Router myRouter;
	
	protected static HashSet<String> m_additionalServerNames = new HashSet<>();
	
	/** Constructor.
	 * Initializes the Restlet list. 
	 */
	public ADOMContainer() {
		m_restletList = new ArrayList<Restlet>();
	}
	

	/** Adds an additional server name to the server names list. */
	public static void AddAdditionalServerName(String serverName)
	{
		if (serverName != null && !m_additionalServerNames.contains(serverName)) m_additionalServerNames.add(serverName);
	}
	
	/** Sets up the {@link ADOMeRestlet}s representing all known object memories (should be called at server startup). 
	 * @param history A {@link OMMVersionManagerHistory} object determining whether the memories should maintain a history. 
	 */
	public static void initializeADOMs(OMMVersionManagerHistory history){
				
		System.out.println("Setting up ADOM instances.");
		
		MemoryAccess mem_access = new OmsAdomBinding(history);
		List<String> mem_names = mem_access.getMemoryNames();
		
		for (String mem_name : mem_names){
			System.out.println("Creating adom for memory: "+mem_name);
			ADOMeRestlet adom = new ADOMeRestlet(mem_name, mem_access);
			m_adoms.put(mem_name, adom);
		}
	}
	
	/** Retrieves the ADOMe Restlet representing a given object memory.
	 * @param mem_name The requested memory's name.
	 * @return The memory as an {@link ADOMeRestlet}. 
	 */
	public static ADOMeRestlet getAdom(String mem_name){
		return m_adoms.get(mem_name);
	}
	
	/** Adds a new object memory to the known memories and creates a REST interface subtree for it. 
	 * 
	 * @param mem_name Name of the new memory.
	 * @param history  A {@link OMMVersionManagerHistory} object determining whether a history for the memory should be maintained. 
	 * @param ownerBlock Owner information about the new memory as an {@link OMMBlock}. 
	 * @return True, if the memory has been added successfully. 
	 */
	public static boolean registerNewMemory(String mem_name, OMMVersionManagerHistory history, OMMBlock ownerBlock)
	{
	  if (m_adoms.containsKey(mem_name)) return false;
		
	  MemoryAccess mem_access = new OmsAdomBinding(history);
	  mem_access.setOwnerBlock(mem_name, ownerBlock);
	  ADOMeRestlet adom = new ADOMeRestlet(mem_name, mem_access);
	  
	  m_adoms.put(mem_name, adom);
	  if (myRouter != null)
	  {
	    adom.attach(myRouter);
	  }
	  
	  return true;
	}
	
	@Override
	public Restlet createInboundRoot() { 

		// this is executed when receiving the first request to the OMS
		
		Router router = new Router(getContext()); 
		List<String> names = new LinkedList<String>();
		
		for (String mem_name : m_adoms.keySet()){
			names.add(mem_name);
			ADOMeRestlet adom = m_adoms.get(mem_name);
			adom.attach(router);
			m_restletList.add(adom);
		}
		
		// Add components to list the memory names as HTML and JSON
		ListMemories html_list = new ListMemories(names, "html");
		html_list.attach(router, "/htmllist");
		
		ListMemories json_list = new ListMemories(names, "json");
		json_list.attach(router, "/list");
		
		if (myRouter == null)
		{
  		myRouter = router;
		}
		else
		{
		  throw new IllegalStateException("ADOMContainer initialized twice.");
		}

		String hostWithDomain = null, hostWODomain = null;
		try
		{
			InetAddress hostAdress = InetAddress.getLocalHost(); 
			hostWithDomain = hostAdress.getCanonicalHostName();
			hostWODomain = hostAdress.getHostName();
		}
		catch(Exception e) {e.printStackTrace();}
		
		System.out.println("Adding Server URLs to Security Host:");
		AddServerURL("http://"+hostWithDomain+":" + Config.PORT + Start.contextPath + "/");
		if (!hostWithDomain.equals(hostWODomain)) AddServerURL("http://"+hostWODomain+":" + Config.PORT + Start.contextPath + "/");
		AddServerURL("http://localhost:" + Config.PORT + Start.contextPath + "/");
		
		AddServerURL("https://"+hostWithDomain+":" + Config.PORT_SSL + Start.contextPath + "/");
		if (!hostWithDomain.equals(hostWODomain)) AddServerURL("https://"+hostWODomain+":" + Config.PORT_SSL + Start.contextPath + "/");
		AddServerURL("https://localhost:" + Config.PORT_SSL + Start.contextPath + "/");
		
		for(String addServer : m_additionalServerNames)
		{
			AddServerURL("http://"+addServer+":" + Config.PORT + Start.contextPath + "/");
			AddServerURL("https://"+addServer+":" + Config.PORT_SSL + Start.contextPath + "/");
		}
		
		if (FeatureNegotiation.RouteReplacement != null)
		{
			AddServerURL("http://" + FeatureNegotiation.RouteReplacement);
			//AddServerURL(FeatureNegotiation.RouteReplacement + ":" + Config.PORT_SSL + Start.contextPath + "/");
		}
				
		// add authenticator to routing
		return ACLAuthentication.getAuthenticator(getContext(), router, ACLAuthentication.getVerifier());		
	}
	
	/** Adds a server URL to the URLs known in {@link ACLAuthentication}. 
	 * @param url Server URL as a {@link String}.
	 */
	protected static void AddServerURL(String url)
	{
		if (url == null || url.isEmpty()) return;
		
		if (ACLAuthentication.serverUrls.add(url))
			System.out.println("\t"+url);
		else
			System.err.println("\t ADDING FAILED: "+url);
	}
}
