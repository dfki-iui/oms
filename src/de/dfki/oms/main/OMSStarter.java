package de.dfki.oms.main;

import java.net.InetAddress;

import de.dfki.oms.websocket.SocketServlet;
//import de.dfki.oms.websocket.tmp.WebSocketTest;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.ext.servlet.ServerServlet;

import de.dfki.adom.rest.Config;
import de.dfki.adom.rest.Start;
import de.dfki.adom.rest.nodes.FeatureNegotiation;
import de.dfki.adom.ssdp.SSDP;
import de.dfki.omm.types.BinaryValue;
import de.dfki.oms.adom.ADOMContainer;
import de.dfki.oms.adom.ADOMManagementContainer;
import de.dfki.oms.history.OMMVersionManagerHistory;
import de.dfki.oms.webapp.OMSHandler;
import de.dfki.oms.webapp.OMSOMMHandler;

/** Starts the OMS. */
public class OMSStarter
{
	
	/** <p>Initializes and starts the server. </p>
	 * <p>Argument structure:</p>
	 * <ul>
	 * <li><b>port</b> or <b>p</b>: The port on which the OMS is available (default: 10082)</li>
	 * <li><b>route</b> or <b>r</b>: The OMS route (for example: localhost:10082 -> myserver.com/123)</li>
	 * <li><b>disable-history</b> or <b>dh</b>: Configures the OMS not to maintain a memory history</li>
	 * <li><b>ip-address</b> or <b>ip</b>: Configures the OMS to use the IP address instead of the hostname for OMS URLs</li>
	 * <li><b>upnp</b>: Configures the OMS to use Universal Plug and Play to broadcast memories</li>
	 * <li><b>additionalServer</b> or <b>as</b>: Can be used to add additional server names</li>
	 * </ul>
	 * @param args Arguments. 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args)
	{
		org.apache.xml.security.Init.init();
		BinaryValue.initCodec();
		int port = 10082;
		OMMVersionManagerHistory history = OMMVersionManagerHistory.Enabled;

		try
		{
			Options options = new Options();
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
			Option optUPnP = OptionBuilder.withDescription("use UPnP server to broadcast memories")
                    .create( "upnp" );
			Option optAddServer = OptionBuilder.withDescription("add addtional server names")
					.hasArgs()
                    .withLongOpt("additionalServer")
                    .create( "as" );

			options.addOption(optPort);
			options.addOption(optRoute);
			options.addOption(optHistory);
			options.addOption(optIP);
			options.addOption(optUPnP);
			options.addOption(optAddServer);
			
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
			}
			
			String name = InetAddress.getLocalHost().getCanonicalHostName();
			if (cmd.hasOption(optIP.getOpt())) name = InetAddress.getLocalHost().getHostAddress();
			Config.setHost(name);
			FeatureNegotiation.RouteSupplamental = "rest/";
			
			if (cmd.hasOption(optAddServer.getOpt()))
			{	
				String[] addServers = cmd.getOptionValues(optAddServer.getOpt());
				for(String server : addServers) ADOMContainer.AddAdditionalServerName(server);
			}
			
			System.out.print("OMS2");
			System.out.print(" (starting on Java " + System.getProperty("java.version") +") with port "+port);
			if (FeatureNegotiation.RouteReplacement != null) 
				System.out.println(" and route replacement -> " + FeatureNegotiation.RouteReplacement);
			else if (FeatureNegotiation.RouteSupplamental != null) 
				System.out.println(" and route supplamental -> " + FeatureNegotiation.RouteSupplamental);
			else
				System.out.println();
				
			
			PropertyConfigurator.configureAndWatch( "log4j.properties" );
			
			HttpConfiguration httpConfig = new HttpConfiguration();
			httpConfig.setSecurePort(Config.PORT_SSL);
			httpConfig.addCustomizer(new SecureRequestCustomizer());			
			
			Server server = new Server(port);
			
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
			
	        
	        server.setConnectors(new Connector[]{scUsec, sc});

	        ContextHandlerCollection contexts = new ContextHandlerCollection();
	        
	        ADOMContainer.initializeADOMs(history);
	        OMSOMMHandler.HISTORY_STATE = history;
	        
	        ServletContextHandler contextOMS = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextOMS.setContextPath("/web");	        
	        contextOMS.addServlet(new ServletHolder(new OMSHandler(false)), "/*");
	        System.out.println("* OMS Web Application ('/web')");

			ServletContextHandler contextOMSQuery = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextOMSQuery.setContextPath("/query");	        
	        contextOMSQuery.addServlet(new ServletHolder(new OMSHandler(true)), "/*");
	        System.out.println("* OMS Query ('/query')");
	        
	        ServletContextHandler contextREST = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextREST.setInitParameter("org.restlet.application", ADOMContainer.class.getCanonicalName());
	        contextREST.setContextPath(Start.contextPath);	        
	        contextREST.addServlet(new ServletHolder(new ServerServlet()),"/*");
	        //ServletHolder foo = new ServletHolder();
	        Config.PORT = port;
	        System.out.println("* RESTlet ('/rest')");

	        ServletContextHandler contextRESTManagement = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextRESTManagement.setInitParameter("org.restlet.application", ADOMManagementContainer.class.getCanonicalName());
	        contextRESTManagement.setContextPath("/mgmt");
	        contextRESTManagement.addServlet(new ServletHolder(new ServerServlet()), "/*");
	        System.out.println("* Management ('/mgmt')");

			ServletContextHandler contextWS = new ServletContextHandler();
			contextWS.setContextPath("/");
			contextWS.addServlet(SocketServlet.class, "/websocket");
			//WebSocketTest.test();

			if (cmd.hasOption(optUPnP.getOpt()))
	        {
	        	SSDP_OMS_Handler.addHandler(Config.getHostURL(), Config.PORT);		
	        	SSDP.listenToMulticastResponsesAsync();
	        	SSDP.listenToUnicastResponsesAsync();
	        }
	        
	        
	        contexts.setHandlers(new Handler[] { contextOMS, contextOMSQuery, contextREST, contextRESTManagement, contextWS });

	        server.setHandler(contexts);
	        server.start();

	        System.out.println("Memory path: " + OMSOMMHandler.MEMORY_PATH);
	        if (history == OMMVersionManagerHistory.Disabled) System.out.println("History DISABLED!");
	        server.join();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}
