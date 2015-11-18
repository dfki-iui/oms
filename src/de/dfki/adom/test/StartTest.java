package de.dfki.adom.test;

import java.util.List;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;
import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.EmptyStorageManager;
import de.dfki.adom.rest.MemoryAccess;
import de.dfki.adom.rest.OMMBinding;
import de.dfki.adom.rest.nodes.ListMemories;

/** Tests the REST API by creating a tentative REST interface for the OMS and calling tests via {@link ApiTester}. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
@Deprecated
public class StartTest extends Application {

	private ADOMeRestlet m_adom;
	
	/** Empty constructor. */
	public StartTest() {
	}

	
	@Override
	public Restlet createInboundRoot() { 
		
		// Read the available OMM memories
		MemoryAccess mem_access = new OMMBinding();
		List<String> mem_names = mem_access.getMemoryNames();
		
		if ((mem_names == null) || (mem_names.size() == 0))
			return null;
		
		// Take the first memory if there should be more than one
		m_adom = new ADOMeRestlet(mem_names.get(0), mem_access, Config.DEFAULT_HEARTBEAT);
		// Initialize Stand-Alone-Mode: Handle Memory-Storage without an OMS.
		EmptyStorageManager storage_manager = new EmptyStorageManager(m_adom);
		m_adom.initStandAloneMode(storage_manager);
		// Set routing.
		Router router = new Router(getContext()); 
		m_adom.attach(router);
		
		// --- Optional Code ---
		// Add components to list the memory names as HTML and JSON
		ListMemories html_list = new ListMemories(mem_names, "html");
		html_list.attach(router, "/htmllist");
		
		ListMemories json_list = new ListMemories(mem_names, "json");
		json_list.attach(router, "/list");
		// --- ---
		
		return router;
	}
	
	
	/** Retrieves the connected ADOM.
	 * @return The connected {@link ADOMeRestlet}. 
	 */
	public ADOMeRestlet getADOM() {
		return m_adom;
	}
	
	

	/** Main method that starts a new Test Application and tests the API via {@link ApiTester}.
	 * @param args Arguments (unused.)
	 */
	public static void main(String[] args) {
		StartTest test = new StartTest();
		String path = "";   // no "/" at the end.  other example: "/adom"   leads to http://localhost:8182/adom/sample/st/block/3/meta
		
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, Config.PORT);
		component.getDefaultHost().attach(path, test);
		
		try {
			// Perform the tests
			component.start();
			ApiTester api_tester = new ApiTester(test.getADOM());
			api_tester.test();
			component.stop();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
