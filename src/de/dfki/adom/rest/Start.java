/**
 * 
 */
package de.dfki.adom.rest;

import java.net.InetAddress;
import java.util.List;

import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.servlet.ServerServlet;
import org.restlet.routing.Router;

import de.dfki.adom.rest.nodes.FeatureNegotiation;
import de.dfki.adom.security.ACLAuthentication;
import de.dfki.omm.types.BinaryValue;


/** An {@link Application} running a REST interface for an OMS. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Start extends Application { // TODO This class is not used anywhere, except for its static variable "contextPath"

	private String m_memoryName = null;
	private ADOMeRestlet m_adom;
	public final static String contextPath = "/rest";

	public Start() {
	}
	
	
	public Start(String memoryName) {
		m_memoryName = memoryName;
	}
	
	public ADOMeRestlet getADOM() {
		return m_adom;
	}
	
//	public void initialize() {
//		Component component = new Component();
//		component.getServers().add(Protocol.HTTP, Config.PORT);
//		
//		// todo: add ssl connection
//		/*Server sslServer = component.getServers().add(Protocol.HTTPS, Config.PORT_SSL);		
//		Series<Parameter> parameters = sslServer.getContext().getParameters();
//        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
//        parameters.add("keyStorePath", "pathserverX.jks");
//        parameters.add("keyStorePassword", "password");
//        parameters.add("keyPassword", "password");
//        parameters.add("keyStoreType", "JKS");*/		
//		
//		
//		//component.getDefaultHost().attach("", this);
//		component.getDefaultHost().attach("", ACLSecurity.getAuthenticator(getContext(), this));
//		try {
//			component.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public Restlet createInboundRoot() { 

		org.apache.xml.security.Init.init();
		
		// Read the available OMM memories
		MemoryAccess mem_access = new OMMBinding(m_memoryName);
		List<String> mem_names = mem_access.getMemoryNames();
		System.out.println("Got memory names: "+mem_names);
		
		if ((mem_names == null) || (mem_names.size() == 0))
			return null;
		
		// Take the first memory if there should be more than one

		m_adom = new ADOMeRestlet(mem_names.get(0), mem_access, Config.DEFAULT_HEARTBEAT);
		// Initialize Stand-Alone-Mode: Handle Memory-Storage without an OMS.
		StorageManager storage_manager = new EmptyStorageManager(m_adom);
		m_adom.initStandAloneMode(storage_manager);
		// Set routing.
		Router router = new Router(getContext()); 
		//Encoder encoder = new Encoder(getContext(), false, true, new EncoderService(true)); // gzip compression		
		//encoder.setNext(router);

		m_adom.attach(router);
		
		String hostWithDomain = "<hostname>", hostWODomain = "host";
		try
		{
			InetAddress hostAdress = InetAddress.getLocalHost(); 
			hostWithDomain = hostAdress.getCanonicalHostName();
			hostWODomain = hostAdress.getHostName();
		}
		catch(Exception e) {e.printStackTrace();}
		
		System.out.println("Adding Server URLs to Security Host");
		ACLAuthentication.serverUrls.add("http://"+hostWithDomain+":" + Config.PORT + Start.contextPath + "/");
		if (!hostWithDomain.equals(hostWODomain)) ACLAuthentication.serverUrls.add("http://"+hostWODomain+":" + Config.PORT + Start.contextPath + "/");
		ACLAuthentication.serverUrls.add("http://localhost:" + Config.PORT + Start.contextPath + "/");
		ACLAuthentication.serverUrls.add("https://"+hostWithDomain+":" + Config.PORT + Start.contextPath + "/");
		if (!hostWithDomain.equals(hostWODomain)) ACLAuthentication.serverUrls.add("https://"+hostWODomain+":" + Config.PORT + Start.contextPath + "/");
		ACLAuthentication.serverUrls.add("https://localhost:" + Config.PORT + Start.contextPath + "/");
		
				
		System.out.println("ADOM " + m_adom.getMemoryName() +" connected in stand-alone mode:");
		System.out.println("    http://localhost:" + Config.PORT + "/" + m_adom.getMemoryName());
		System.out.println("    http://"+hostWithDomain+":" + Config.PORT + "/" + m_adom.getMemoryName());
		if (!hostWithDomain.equals(hostWODomain)) System.out.println("    http://"+hostWODomain+":" + Config.PORT + "/" + m_adom.getMemoryName());
		System.out.println("    https://localhost:" + Config.PORT_SSL + "/" + m_adom.getMemoryName());		
		System.out.println("    https://"+hostWithDomain+":" + Config.PORT_SSL + "/" + m_adom.getMemoryName());
		if (!hostWithDomain.equals(hostWODomain)) System.out.println("    https://"+hostWODomain+":" + Config.PORT_SSL + "/" + m_adom.getMemoryName());
		
		
		return ACLAuthentication.getAuthenticator(getContext(), router, ACLAuthentication.getVerifier());
		
	}
	


//	public static void main2(String[] args) throws Exception {
//		String memory_name = null;
//		if (args.length > 0)
//			memory_name = args[0];
//		Start start = new Start(memory_name);
//		start.initialize();
////		Component component = new Component();
////		component.getServers().add(Protocol.HTTP, Config.getPort());
////		component.getDefaultHost().attach("", start);
////		component.start();
//	}
	
	

	/** Initializes and runs the Application. 
	 * @param args Arguments (unused).
	 */
	public static void main(String[] args)
	{
		BinaryValue.initCodec();
//		int port = 10082;
		
		try
		{
			/*Options options = new Options();
			Option optPort = OptionBuilder.withArgName( "port" )
                    .hasArg()
                    .withDescription(  "OMS port (e.g., 10082 as default)" )
                    .withLongOpt("port")
                    .create( "p" );
			Option optRoute = OptionBuilder.withArgName( "route" )
                    .hasArg()
                    .withDescription(  "OMS route (e.g., localhost:10082 -> myserver.com/123)" )
                    .withLongOpt("route")
                    .create( "r" );
			Option optHistory = OptionBuilder.withDescription("OMS port (e.g., 10082 as default)")
                    .withLongOpt("disable-history")
                    .create( "dh" );
			Option optIP = OptionBuilder.withDescription("use IP address insted of hostname for OMS URLs")
                    .withLongOpt("ip-address")
                    .create( "ip" );

			options.addOption(optPort);
			options.addOption(optRoute);
			options.addOption(optHistory);
			options.addOption(optIP);
			
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse( options, args);
			
			if (cmd.hasOption(optPort.getOpt()))
			{
				port = Integer.parseInt(cmd.getOptionValue(optPort.getOpt()));
			}
			if (cmd.hasOption(optHistory.getOpt())) history = OMMVersionManagerHistory.Disabled;
			if (cmd.hasOption(optRoute.getOpt()))
			{
				String route = cmd.getOptionValue(optRoute.getOpt());
				FeatureNegotiation.RouteReplacement = route;
			}*/
			
			String name = InetAddress.getLocalHost().getCanonicalHostName();

			//if (cmd.hasOption(optIP.getOpt())) name = InetAddress.getLocalHost().getHostAddress();
			Config.setHost(name);
			FeatureNegotiation.RouteSupplamental = "rest/";
			
			System.out.print("OMS2");
			System.out.print(" (starting on Java " + System.getProperty("java.version") +") with port "+Config.PORT);
			if (FeatureNegotiation.RouteReplacement != null) 
				System.out.println(" and route replacement -> " + FeatureNegotiation.RouteReplacement);
			else if (FeatureNegotiation.RouteSupplamental != null) 
				System.out.println(" and route supplamental -> " + FeatureNegotiation.RouteSupplamental);
			else
				System.out.println();
				
			
			//PropertyConfigurator.configureAndWatch( "log4j.properties" );
			
			
			org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(Config.PORT);
			
			HttpConfiguration httpConfig = new HttpConfiguration();
			httpConfig.setSecurePort(Config.PORT_SSL);
			httpConfig.addCustomizer(new SecureRequestCustomizer());			
			
			// unsecure connection
			ServerConnector scUsec = new ServerConnector(server);
			scUsec.setPort(Config.PORT);
			scUsec.addConnectionFactory(new HTTP2ServerConnectionFactory(httpConfig));
			
			
			// SSL/TLS connection
			SslContextFactory fac = new SslContextFactory();
			fac.setKeyStorePath("./resources/keystore");
			fac.setKeyStorePassword("123456");
			fac.setProtocol("TLSv1");
			fac.addExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

			ServerConnector sc = new ServerConnector(server, fac);

			//sc.setForwarded(true);
			sc.setPort(Config.PORT_SSL);
			sc.addIfAbsentConnectionFactory(new SslConnectionFactory(fac, "http/1.1"));
			sc.addConnectionFactory(new HttpConnectionFactory());
			sc.addConnectionFactory(new HTTP2ServerConnectionFactory(httpConfig));
			//sc.addConnectionFactory(new ALPNServerConnectionFactory("http/1.1", "http/2"));
		
			
			/*SelectChannelConnector scc = new SelectChannelConnector();
			scc.setForwarded(true);
			scc.setPort(port);
			//scc.setHostHeader("oms.sb.dfki.de");*/
			
	        
	        server.setConnectors(new org.eclipse.jetty.server.Connector[]{scUsec, sc});

	        ContextHandlerCollection contexts = new ContextHandlerCollection();
	        
	        ServletContextHandler contextREST = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextREST.setInitParameter("org.restlet.application", Start.class.getCanonicalName());
	        contextREST.setContextPath(Start.contextPath);	        
	        contextREST.addServlet(new ServletHolder(new ServerServlet()),"/*");
	        
	        /*SSDP_OMS_Handler.addHandler(Config.getHostURL(), Config.getPort());		
			SSDP.listenToMulticastResponsesAsync();
			SSDP.listenToUnicastResponsesAsync();*/
	        
	        contexts.setHandlers(new Handler[] { contextREST });
	 
	        server.setHandler(contexts);
	        server.start();

	        System.out.println("RUNNING");
	        server.join();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	
	
	
	
}
