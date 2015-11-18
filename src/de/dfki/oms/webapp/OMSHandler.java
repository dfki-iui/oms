package de.dfki.oms.webapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Server;

import de.dfki.oms.history.OMMVersionManager;
import de.dfki.oms.query.OMSQuery;
import de.dfki.oms.query.OMSQueryProcessor;

/** A handler for HTTP requests to the OMS' "/web" and "/query" interfaces. */
public class OMSHandler extends HttpServlet
{
	private static final long serialVersionUID = -5248006317062796800L;
	private final long startUp;
	private boolean queryMode = false;
	
	/** Constructor. 
	 * @param queryMode True, if requests should be handled in query mode (for the "/query" interface). 
	 */
	public OMSHandler(boolean queryMode)
	{
		this.queryMode = queryMode;
		this.startUp = new GregorianCalendar().getTimeInMillis();
	}

	
	/** <p>Handles a GET request.</p> 
	 * <p>Creates an info page for requests to the "/info" node and returns resources for requests beneath the "/resources" node. Delegates all other calls to an {@link OMSOMMHandler}.</p>
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 *  @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setHeader("Server", "OMS2 (Jetty "+Server.getVersion()+")");
		
		if (request.getPathInfo().startsWith("/info"))
		{
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			Tools.transferBufferWithCompression(getInfoPage(request).getBytes(), response, request) ;		    
		}		
		else if (request.getPathInfo().startsWith("/resources"))
		{
			File file = new File("."+request.getPathInfo());			
			Tools.transferFile(request, response, file, getMimeType(file));
		}		
		else if (!queryMode)
		{			
			OMSOMMHandler.handleRequest(request, response);
		}
	}
	
	/** <p>Handles a POST request.</p> 
	 * <p>Used to end a session or to process a query in query mode.</p>
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 *  @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setHeader("Server", "OMS2 (Jetty "+Server.getVersion()+")");
		
		String pi = request.getPathInfo();
		if (!pi.endsWith("/")) pi += "/";
		
		if (request.getParameter("logoff") != null)
		{
			HttpSession session = request.getSession(true);
			
			if (session != null)
			{
				session.removeAttribute("omm_entity");
				session.removeAttribute("omm_entity_passwd");
			}
			OMSOMMHandler.handleRequest(request, response);
		}
		else if (queryMode)
		{
			InputStream is = request.getInputStream();
			int nRead;
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();
			OMSQuery query = new OMSQuery(buffer.toByteArray());						
			String resultStr = OMSQueryProcessor.processAsJson(query, OMMVersionManager.getOMMDateMap());
			if (resultStr != null)
			{
				byte[] result = resultStr.getBytes();
								
				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
				response.setCharacterEncoding("utf-8");
				OutputStream os = response.getOutputStream();
				
				String acceptedCompression = (String) request.getAttribute("Accept-Encoding");
				
				if (acceptedCompression != null && acceptedCompression.contains("gzip")) {
					response.setHeader("Content-Encoding", "gzip");

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					GZIPOutputStream gz = new GZIPOutputStream(baos);
					gz.write(result);
					gz.flush();
					gz.close();

					result = baos.toByteArray();
					baos.close();
				} else if (acceptedCompression != null
						&& acceptedCompression.contains("deflate")) {
					response.setHeader("Content-Encoding", "deflate");

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DeflaterOutputStream def = new DeflaterOutputStream(baos);
					def.write(result);
					def.flush();
					def.close();

					result = baos.toByteArray();
					baos.close();
				}
				
				response.setContentLength(result.length);
				os.write(result);
				os.flush();
			}
			else
			{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} 
		else		
		{
			OMSOMMHandler.handleRequest(request, response);
		} 
		
	}
	
	/** <p>Handles a PUT request.</p> 
	 * <p>PUT is not supported by this handler, so it just sets the response's header to contain basic information.</p>
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 *  @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setHeader("Server", "OMS2 (Jetty "+Server.getVersion()+")");
	}
	
	/** <p>Handles a DELETE request.</p> 
	 * <p>Deletes a resource.</p>
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 *  @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setHeader("Server", "OMS2 (Jetty "+Server.getVersion()+")");
		OMSOMMHandler.handleDelete(request, response);	
	}

	
	
	
	/** Retrieves an HTML representation creating the web interface's info page. 
	 * @param request A request from which to get the session ID. 
	 * @return An HTML snippet describing the info page to add into an HTML skeleton.  
	 * @throws IOException
	 */
	private String getInfoPage(HttpServletRequest request) throws IOException
	{
		int count = 0;
		File[] fileList = new File(OMSOMMHandler.MEMORY_PATH).listFiles();
		if (fileList != null)
		{
			for(File file : fileList)
			{
				if (!file.isDirectory()) continue;
				count++;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(HTMLData.getHeader("../", null, "> Info", null));
		sb.append("<h1><p>OMS2 - Information</p></h1>");
		
		sb.append("<table border=\"1\">");
		sb.append("<caption>Current Status</caption>");
		sb.append("<tr>");
		sb.append("<th>Key</th>");
		sb.append("<th>Value</th>");		    
		sb.append("</tr>");
		sb.append(HTMLData.getKeyValueAsTable("Version", "1.0", "odd", EnumSet.noneOf(EditStatus.class)));		   
		sb.append(HTMLData.getKeyValueAsTable("Session Key", request.getSession(true).getId(), "even", EnumSet.noneOf(EditStatus.class)));
		sb.append(HTMLData.getKeyValueAsTable("Uptime", Tools.getFriendlyTime(new GregorianCalendar().getTimeInMillis() - startUp), "odd", EnumSet.noneOf(EditStatus.class)));
		sb.append(HTMLData.getKeyValueAsTable("Java Version", System.getProperty("java.version"), "even", EnumSet.noneOf(EditStatus.class)));
		sb.append(HTMLData.getKeyValueAsTable("Jetty Version", new Server().getClass().getPackage().getImplementationVersion(), "odd", EnumSet.noneOf(EditStatus.class)));		
		sb.append(HTMLData.getKeyValueAsTable("Number of Memories", count+"", "even", EnumSet.noneOf(EditStatus.class)));
		sb.append("</table>");
		sb.append("<br />");
		sb.append("<p><a href=\"../\">&gt; Home</a></p>");
		sb.append(HTMLData.getFooter("../"));
		
		return sb.toString();
	}
	
//	private String getWelcomePage(HttpServletRequest request) throws IOException
//	{
//		StringBuilder sb = new StringBuilder();
//		
//		/*sb.append("<script type=\"text/javascript\">" +
//				  "function doit(canvasid) " +
//				  "{" +
//				  "var c = document.getElementById(canvasid); " +
//				  "var cxt = c.getContext(\"2d\"); " +
//				  "cxt.fillStyle = \"#FFFFFF\"; " +
//				  "cxt.fillRect(0, 0, c.width, c.height); " +
//				  "}" +
//				  "</script>");*/
//		
//		TileStructure ts = new TileStructure(3, 3);
//		
//		ts.addTile(0, 0, "Link to OMM", URI.create("http://www.w3.org/2005/Incubator/omm/").toURL());
//		ts.addTile(0, 1, new RotatingTextTile("OMS Status", "Online", URI.create(request.getRequestURL()+"info/").toURL()));
//		ts.addTile(0, 2, "Sample Memory", URI.create(request.getRequestURL()+"./m/sample").toURL());
//		
//		ts.addTile(1, 0, "More...", "The server enables you to access object memories based on the Object Memory Model (OMM). To access a memory please use this url and add /m/&lt;memory name&gt;.");
//		ts.addTile(1, 1, "Comming soon!", "Comming soon!");		
//		ts.addTile(1, 2, "DFKI", URI.create("http://www.dfki.de").toURL());
//		
//		ts.addTile(2, 0, "Project RES-COM", URI.create("http://www.res-com-project.org").toURL());
//		ts.addTile(2, 1, "Project SemProM", URI.create("http://www.semprom.org").toURL());
//		ts.addTile(2, 2, "Project PIZZA", URI.create("http://www.dfki.de/pizza").toURL());
//		
//		sb.append(HTMLData.getTilePage("./", "OMS2 - The DFKI Object Memory Server", ts, "welcome"));
//		
//		return sb.toString();
//	}

	/** Retrieves the MIME type of a file by examining its extension. 
	 * @param file The {@link File}.  
	 * @return The MIME type as {@link String}. 
	 */
	private String getMimeType(File file)
	{
		int dotPos = file.toString().lastIndexOf(".");
		String extension = file.toString().substring(dotPos);
		
		if (extension != null && !extension.equals(""))
		{
			switch(extension)
			{
				case ".css":
					return "text/css";
				case ".txt":
					return "text/plain";
				case ".html":
				case ".htm":
				case ".shtml":
					return "text/html";
				case ".xsd":
				case ".xsl":
				case ".xml":
					return "application/xml";
				case ".owl":
				case ".rdf":
					return "application/rdf+xml";
				case ".js":
					return "application/javascript";
				case ".png":
					return "image/png";
				case ".jpg":
				case ".jpeg":
					return "image/jpeg";
				case ".gif":
					return "image/gif";
				case ".qt":
				case ".mov":
					return "video/quicktime";
				case ".avi":
					return "video/x-msvideo";
				case ".mpg":
				case ".mpeg":
				case ".mpe":
					return "video/mpeg";
				case "mp4":
					return "video/mp4";
				case "mp2":
				case "mp3":
					return "audio/x-mpeg";
			}
		}
			
		return "application/octet-stream";		
	}
	
}
