package de.dfki.oms.test;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.ext.servlet.ServerServlet;

import de.dfki.adom.rest.Config;
import de.dfki.oms.adom.ADOMContainer;
import de.dfki.oms.webapp.OMSHandler;
import de.dfki.oms.webapp.OMSOMMHandler;

/** Test version of the OMS. */
@Deprecated
public class ADOMTest 
{
	
	public static void main(String[] args)
	{
		int port = 10082;
		
		try
		{
			System.out.print("OMS2");
			System.out.println(" (starting on Java " + System.getProperty("java.version") +") with port "+port);
			
			PropertyConfigurator.configureAndWatch( "log4j.properties" );
			
			SslContextFactory fac = new SslContextFactory();
			fac.setKeyStorePath("./keystore");
			fac.setKeyStorePassword("jettypwd");
			fac.setProtocol("TLSv1");
			
			Server server = new Server(port);
			
//			HTTPSPDYServerConnector spdy = new HTTPSPDYServerConnector(server);//fac);
//			spdy.setForwarded(true);
//			spdy.setPort(port);
//			spdy.setConfidentialPort(port);
			
			
			ServerConnector sc = new ServerConnector(server);
			//sc.setForwarded(true);
			sc.setPort(port);
			//scc.setHostHeader("oms.sb.dfki.de");			
	        
//	        server.setConnectors(new Connector[]{spdy});
	        server.setConnectors(new Connector[]{sc});

	        ContextHandlerCollection contexts = new ContextHandlerCollection();
	        
	        ServletContextHandler contextOMS = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextOMS.setContextPath("/web");	        
	        contextOMS.addServlet(new ServletHolder(new OMSHandler(false)),"/*");
	        System.out.println("* OMS Web Application ('/web')");
	        
	        ServletContextHandler contextOMSQuery = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextOMSQuery.setContextPath("/query");	        
	        contextOMSQuery.addServlet(new ServletHolder(new OMSHandler(true)),"/*");
	        System.out.println("* OMS Query ('/query')");
	        
	        ServletContextHandler contextREST = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        contextREST.setInitParameter("org.restlet.application", ADOMContainer.class.getCanonicalName());
	        contextREST.setContextPath("/rest");	        
	        contextREST.addServlet(new ServletHolder(new ServerServlet()),"/*");
	        Config.PORT = port;
	        System.out.println("* RESTlet ('/rest')");
	        
	        contexts.setHandlers(new Handler[] { contextOMS, contextOMSQuery, contextREST });
	 
	        server.setHandler(contexts);
	        server.start();

	        System.out.println("Memory path: " + OMSOMMHandler.MEMORY_PATH);
	        server.join();
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
		}	
	}
}
