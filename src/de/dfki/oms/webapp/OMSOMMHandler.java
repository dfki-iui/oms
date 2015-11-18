package de.dfki.oms.webapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.dfki.omm.events.OMMEvent;
import de.dfki.omm.events.OMMEventType;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.impl.OMMImpl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.interfaces.OMMHeader;
import de.dfki.omm.tools.OMMXMLConverter;
import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMSourceType;
import de.dfki.oms.adom.ADOMContainer;
import de.dfki.oms.history.OMMVersionManager;
import de.dfki.oms.history.OMMVersionManagerHistory;
import de.dfki.oms.webapp.handler.QueryCommandDispatcher;

/** A handler for operations on the OMS issued via HTTP requests. */
public class OMSOMMHandler {
	
	protected static final boolean AUTHENTICATION_ENABLED = false;
	
	public static OMMVersionManagerHistory HISTORY_STATE = OMMVersionManagerHistory.Enabled;
	public static final String FILE_SEPARATOR = System .getProperty("file.separator");
	public static String MEMORY_PATH = System.getProperty("user.dir")
			+ FILE_SEPARATOR + "resources" + FILE_SEPARATOR + "memories";
	public static final String CURRENT_FILE_NAME = "current.xml";
	protected static final Logger LOGGER = Logger.getLogger(OMSOMMHandler.class);

	/** Analyses and answers requests, discarding invalid ones.  
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 *  @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @throws IOException
	 */
	public static void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		LOGGER.debug("request="+request.toString());
		String requestPath = request.getPathInfo();
		if (requestPath.startsWith("/")){
			requestPath = requestPath.substring(1);
		}
		final String[] pathParts = requestPath.split("/");
		System.out.println(pathParts[0]+ "::"+pathParts.length+":"+ request.getQueryString());

		Map<String, List<String>> query = Tools.getUrlParameters("?" + request.getQueryString());
		if(query.containsKey("name")){
			pathParts[0] = query.get("name").get(0);
		}
		
		if (pathParts.length < 1 || pathParts[0] == null || "".equals(pathParts[0])) // should never happen
		{			
			
			if (query.size() > 0 && query.containsKey("cmd"))
			{
				switch(query.get("cmd").get(0))
				{
					case "help":
						Tools.transferBufferWithCompression(HTMLData.getHelpPage("./").getBytes(), response, request);	
						break;
				}
			}
			else
			{		
				
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				System.out.println("noMemorySpecified");
				Tools.transferBufferWithCompression(HTMLData.getNoMemoryPage("Invalid URL: Please specify a memory!", "./", MEMORY_PATH).getBytes(), response, request);						
			}
			System.out.println("return");
			return;
		}

		handleMemoryStorage(request, response, pathParts[0]);
	}

	/** <p>Handles a DELETE request.</p> 
	 * <p>Deletes the resource given in the request if it is in the storage.</p>
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	public static void handleDelete(final HttpServletRequest request, final HttpServletResponse response)
	{
		String requestPath = request.getPathInfo();

		if (requestPath.startsWith("/")) requestPath = requestPath.substring(1);
		final String[] pathParts = requestPath.split("/");
		final String memoryName = pathParts[0];
		
		String command = "";
		
		if (pathParts.length > 1) { command = pathParts[1]; }

		if ("st".equals(command)) // storage access
		{
			handleDeleteREST(request, response, memoryName, pathParts);
		}
	}
	
	/** Handles requests to the storage, discarding or delegating them as necessary.  
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void handleMemoryStorage(final HttpServletRequest request, final HttpServletResponse response, final String memoryName)
			throws IOException, UnsupportedEncodingException {
		
		final String currentDir = getMemoryPath(memoryName);
		
		final String currentFileStr = currentDir;
		final File currentFile = new File(currentFileStr);
		System.out.println("handleMemoryStorage");
		Map<String, List<String>> query = Tools.getUrlParameters("?" + request.getQueryString());
		System.out.println(Arrays.toString(query.keySet().toArray()));
		if (request.getRequestURI().endsWith("adome")){
			System.out.println("got config request.");
		}
		if(!query.containsKey("cmd")){
			if (!currentFile.exists()) {
				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				Tools.transferBufferWithCompression(
						HTMLData.getMissingMemoryPage(currentFileStr, "../").getBytes(), response, request);
//					HTMLData.getErrorPage("Unknown Memory Name", "../")
//							.getBytes(), response, request);
				return;
			}
		}

		handleWebApplication(request, response, memoryName, currentFile);
	}
		
//	private static void bufferBlockOptions(OMM omm, StringBuffer target, String preselection){
//		target.append("<option value=\"everyblock\">Every block</option>");
//		for (OMMBlock block : omm.getAllBlocks()){
//		  if (preselection != null && preselection.equals(block.getID())){
//		    target.append("<option selected=\"selected\" value=\"");
//		  }
//		  else {
//		    target.append("<option value=\"");
//		  }
//			target.append(block.getID());
//			target.append("\">");
//			target.append(block.getID());
//			target.append("</option>");
//		}
//	}

	/** Handles DELETE requests to a memory's storage. 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param pathParts The path given in the request in a {@link String}[]. 
	 */
	private static void handleDeleteREST(final HttpServletRequest request, final HttpServletResponse response, 
			final String memoryName, final String[] pathParts)
	{
		if (pathParts == null || pathParts.length == 0)
		{
			// delete entire memory
			response.setStatus(HttpServletResponse.SC_OK);
			//OMM omm = OMMVersionManager.create(memoryName).getCurrentVersion();
		}
		else
		{		
			switch(pathParts[0])
			{
				case "block":
					try
					{
						if (pathParts.length == 2) // delete block with given ID
						{
							response.setContentType("application/xml");
							String blockID = URLDecoder.decode(pathParts[1], "utf-8");
							OMM omm = OMMVersionManager.create(memoryName, HISTORY_STATE).getCurrentVersion();
							omm.removeBlock(blockID, Tools.getEntity(request));
							response.setStatus(HttpServletResponse.SC_OK);
						}
						else if (pathParts.length == 3) // delete block part with given ID
						{						
							OMM omm = OMMVersionManager.create(memoryName, HISTORY_STATE).getCurrentVersion();						
							String blockID = URLDecoder.decode(pathParts[1], "utf-8");						
							String blockPart = URLDecoder.decode(pathParts[2], "utf-8");
							OMMBlock block = omm.getBlock(blockID);
							if (block == null)
							{
								// TODO: throw Error
								response.setStatus(HttpServletResponse.SC_NOT_FOUND);
								return;
							}
							
							switch(blockPart)
							{
								default:
									// TODO: throw Error
									return;
								case "payload":
									if (block.getPayload() == null)
									{
										response.setStatus(HttpServletResponse.SC_NOT_FOUND);
										return;
									}
									
									block.removePayload(Tools.getEntity(request));
									response.setStatus(HttpServletResponse.SC_OK);
									break;
								case "link":
									if (block.getLink() == null)
									{
										// TODO: throw Error
										response.setStatus(HttpServletResponse.SC_NOT_FOUND);
										return;
									}
									
									block.removeLink(Tools.getEntity(request));
									response.setStatus(HttpServletResponse.SC_OK);
									break;
							}												
						}
					} 
					catch (UnsupportedEncodingException e) { e.printStackTrace(); }
					break;				
			}
		}
	}

	/** Checks a request for authentication information if necessary and delegates the request's command to the responsible methods. 
	 *  
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param memoryDirectory Directory containing the memory folder as a {@link File}. 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void handleWebApplication(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File memoryDirectory) throws IOException,
			UnsupportedEncodingException
	{
		HttpSession session = request.getSession(true);

		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		
		if (userName != null && session != null)
		{
			session.setAttribute("omm_entity", userName);
			session.setAttribute("omm_entity_passwd", password);
		}
		else if (request.getParameter("logoff") != null && session != null)
		{
			session.removeAttribute("omm_entity");
			session.removeAttribute("omm_entity_passwd");
		}
		
		if (AUTHENTICATION_ENABLED)
		{
			if (session == null || session.getAttribute("omm_entity") == null)
			{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				Tools.transferBufferWithCompression(HTMLData.getLoginPage(memoryName, "../").getBytes(), response, request);
				return;
			}
		}
		
		LOGGER.debug(request.getQueryString());

		Map<String, List<String>> query = Tools.getUrlParameters("?"
				+ request.getQueryString());

		String cmdArg = "download", outputArg = "xml", partArg = null;

		if (isWebBrowser(request)) // present html web page to end user browser
		{
			outputArg = "html_rw";
		}

		if (query.containsKey("cmd")){
			cmdArg = query.get("cmd").get(0);
		}	
		if (query.containsKey("output"))
			outputArg = query.get("output").get(0);
		if (query.containsKey("part"))
			partArg = query.get("part").get(0);

		switch (cmdArg) {
		case "upload":
			LOGGER.debug("upload command!");
			uploadCommand(request, memoryName);
			break;
		case "download":
			LOGGER.debug("download command!");
			downloadCommand(request, response, memoryName, memoryDirectory,
					outputArg, partArg);
			break;
		case "remove":
			LOGGER.debug("remove command!");
			LOGGER.debug("rmArg = " + partArg);
			removeCommand(request, response, memoryName, memoryDirectory,
					outputArg, partArg, partArg);
			break;
		case "edit":
			LOGGER.debug("edit command!");
			LOGGER.debug("editArg = " + partArg);
			editCommand(request, response, memoryName, memoryDirectory, outputArg,
					partArg, partArg, query);
			break;
		case "add":
			LOGGER.debug("add command!");
			LOGGER.debug("editArg = " + partArg);
			addCommand(request, response, memoryName, memoryDirectory, outputArg,
					partArg, partArg, query);
			break;
		case "withdraw":
			OMMVersionManager.create(memoryName, HISTORY_STATE).withdrawNewerVersion(Integer.parseInt(query.get("version").get(0)));
			
			downloadCommand(request, response, memoryName, memoryDirectory,
					outputArg, partArg);
			break;
		}
	}
		
	/** Checks whether a request is issued via web browser.
	 * @param request The {@link HttpServletRequest} to check. 
	 * @return True, if the request's user agent entry contains the name of a known web browser. 
	 */
	private static boolean isWebBrowser(final HttpServletRequest request) {
		
		String userAgent = request.getHeader("user-agent");
		if (userAgent == null) return false;

		userAgent = userAgent.toLowerCase();
		return (userAgent.contains("mozilla") || userAgent.contains("opera")
				|| userAgent.contains("lynx") || userAgent.contains("webkit"));
	}

	/** Handles a sent "add" command by either creating and responding a new memory or responding an existing memory's current version. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @param addArg The query's "add" argument.
	 * @param query The whole query as a {@link Map}<{@link String}, {@link List}<{@link String}>>. 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void addCommand(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File currentFile, String outputArg, String partArg,
			String addArg, Map<String, List<String>> query) throws IOException,
			UnsupportedEncodingException {
		LOGGER.debug("addArg = " + addArg);
		partArg = "add_" + addArg;
		LOGGER.debug("new partArg=" + partArg);
		if(!partArg.equals("add_new_memory")){
			downloadCommand(request, response, memoryName, currentFile, outputArg, partArg, query);
		}else{
			createMemoryCommand(memoryName, currentFile, request, response);
		}
	}

	/** Creates a new memory from the given data. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	private static void createMemoryCommand(String memoryName, File currentFile, HttpServletRequest request, HttpServletResponse response) {
		if(!currentFile.exists()){
			currentFile.mkdirs();
		}
		String remoteURL = request.getRequestURL().toString();
		if(!remoteURL.endsWith(memoryName+"/")){
			remoteURL += memoryName+"/";
		}

		createNewMemory(memoryName, currentFile, Tools.getEntity(request), remoteURL, null, null);
	
		response.setStatus(HttpServletResponse.SC_OK);
		Tools.transferBufferWithCompression("success".getBytes(), response, request);
	}
	
	/** Retrieves a request's body. 
	 * @param request The {@link HttpServletRequest}. 
	 * @return The request's body as a {@link String}.
	 * @throws IOException
	 */
	public static String getBody(HttpServletRequest request) throws IOException 
	{
	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}

	/** Retrieves the complete path to a memory. 
	 * @param memoryName Name of the memory. 
	 * @return Path to the memory with the given name. 
	 */
	public static String getMemoryPath(String memoryName)
	{
		return MEMORY_PATH + FILE_SEPARATOR + memoryName;
	}
	
	
	/** Clones a given memory on the OMS. 
	 * @param memoryName Name of the memory. 
	 * @param currentFile Path to the memory. 
	 * @param entity The {@link OMMEntity} creating the memory. 
	 * @param remoteURL URL to the memory's remote location. 
	 * @param header An {@link OMMSecurityHeader} describing the new memory. 
	 * @param ownerBlock An {@link OMMBlock} of the new memory. 
	 * @param blocks An {@link ArrayList}<{@link OMMBlock}> containing all the memory's blocks. 
	 * @return True, if the creation was successful. 
	 */
	public static boolean cloneMemory (String memoryName, File currentFile, OMMEntity entity, String remoteURL, OMMHeader header, OMMBlock ownerBlock, ArrayList<OMMBlock> blocks) {

		// create basic OMM
		OMM emptyOmm = null;
		if (header == null) emptyOmm = OMMFactory.createEmptyOMM(OMMXMLConverter.getTypedValue("url", remoteURL));
		else emptyOmm = OMMFactory.createEmptyOMM(header);
		
		// add blocks
		OMMImpl newOmm = (OMMImpl) emptyOmm;
		for (OMMBlock block : blocks) {
			newOmm.addBlock(block, block.getCreator());
		}

		// save and register OMM
		newOmm.setSource(new File(currentFile.getAbsolutePath()+"/v0.xml"));
		newOmm.setSourceType(OMMSourceType.LocalFile);
		OMMFactory.saveOMM(newOmm, true);
		OMMEvent ev = new OMMEvent(newOmm, null, entity, OMMEventType.MEMORY_CREATED);
		OMMVersionManager.createWithNewMemoryInfo(ev, memoryName, HISTORY_STATE);
		newOmm.fireOMMEvent(ev);
		return ADOMContainer.registerNewMemory(memoryName, HISTORY_STATE, ownerBlock);
	}
	
	
	/** Creates a new memory on the OMS. 
	 * @param memoryName Name of the memory. 
	 * @param currentFile Path to the memory. 
	 * @param entity The {@link OMMEntity} creating the memory. 
	 * @param remoteURL URL to the memory's remote location. 
	 * @param header An {@link OMMSecurityHeader} describing the new memory. 
	 * @param ownerBlock An {@link OMMBlock} of the new memory. 
	 * @return True, if the creation was successful. 
	 */
	public static boolean createNewMemory(String memoryName, File currentFile, OMMEntity entity, String remoteURL, OMMHeader header, OMMBlock ownerBlock) 
	{		
		OMM newOmm = null;

		if (header == null)
			newOmm = OMMFactory.createEmptyOMM(OMMXMLConverter.getTypedValue("url", remoteURL));
		else
			newOmm = OMMFactory.createEmptyOMM(header);
		
		((OMMImpl)newOmm).setSource(new File(currentFile.getAbsolutePath()+"/v0.xml"));
		((OMMImpl)newOmm).setSourceType(OMMSourceType.LocalFile);
		OMMFactory.saveOMM(newOmm, true);
		OMMEvent ev = new OMMEvent(newOmm, null, entity, OMMEventType.MEMORY_CREATED);
		OMMVersionManager.createWithNewMemoryInfo(ev, memoryName, HISTORY_STATE);
		//System.out.println("fire event");
		((OMMImpl)newOmm).fireOMMEvent(ev);
		//System.out.println("event fired");
		return ADOMContainer.registerNewMemory(memoryName, HISTORY_STATE, ownerBlock);
	}

	/** Handles a sent "edit" command by delegating it to the appropriate handler. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @param editArg The query's "edit" argument.
	 * @param query The whole query as a {@link Map}<{@link String}, {@link List}<{@link String}>>. 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void editCommand(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File currentFile, String outputArg, String partArg,
			String editArg, Map<String, List<String>> query) throws IOException,
			UnsupportedEncodingException {
		
		LOGGER.debug("editArg = " + editArg);
		partArg = "edit_" + editArg;

		downloadCommand(request, response, memoryName, currentFile, outputArg, partArg, query);
	}

	/** Handles a sent "remove" command by delegating it to the appropriate handler. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @param rmArg The query's "remove" argument.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void removeCommand(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File currentFile, String outputArg, String partArg,
			String rmArg) throws IOException, UnsupportedEncodingException {
		
		LOGGER.debug("rmArg = " + rmArg);
		partArg = "remove_" + rmArg;

		downloadCommand(request, response, memoryName, currentFile, outputArg, partArg);
	}

	/** Retrieves a memory in the format specified by the request's "output" argument and writes it into the response. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void downloadCommand(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File memoryDirectory, String outputArg, String partArg)
			throws IOException, UnsupportedEncodingException {
		downloadCommand(request, response, memoryName, memoryDirectory, outputArg, partArg, null);
	}

	/** Retrieves a memory in the format specified by the request's "output" argument and writes it into the response. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @param query The whole query as a {@link Map}<{@link String}, {@link List}<{@link String}>>. 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static void downloadCommand(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File memoryDirectory, String outputArg, String partArg, Map<String, List<String>> query)
			throws IOException, UnsupportedEncodingException {
		switch (outputArg) {
		case "xml":
			// output as raw xml file
			LOGGER.debug("raw XML retrieved!");			
			Tools.transferFile(request, response, OMMVersionManager.create(memoryName, HISTORY_STATE).getCurrentVersionFile(), "application/xml");
			break;
		case "html":
			// TODO
			break;
		case "html_rw":
			if (partArg != null) {
				LOGGER.debug("serve memory part");
				serveMemoryPart(request, response, memoryName, memoryDirectory, partArg, query);
			} else {
				// output as html file
				LOGGER.debug("complete HTML retrieved!");
				String pathCorrection = "../";
				OMMVersionManager manager = OMMVersionManager.create(memoryName, HISTORY_STATE);
				Tools.transferBufferWithCompression(OMM_HTML_Converter
						.convertOMMToHTML(manager, pathCorrection, memoryName, getCurrentUser(request)).getBytes(),
						response, request);
			}
			break;
		}
	}

	/** Retrieves a part of a memory in HTML format.
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param outputArg The query's "output" argument.
	 * @param partArg The query's "part" argument.
	 * @param query The whole query as a {@link Map}<{@link String}, {@link List}<{@link String}>>. 
	 * @throws UnsupportedEncodingException
	 */
	private static void serveMemoryPart(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File currentFile, String partArg, Map<String, List<String>> query)
			throws UnsupportedEncodingException {
		
		OMM omm = OMMVersionManager.create(memoryName, HISTORY_STATE).getCurrentVersion();
		LOGGER.debug("exec");
		QueryCommandDispatcher.GetHandler(partArg).execute(request, response,
				memoryName, currentFile, partArg, query, omm);
	}

	/** Uploads a request's contents as a new memory to the OMS. 
	 * 
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 */
	private static void uploadCommand(final HttpServletRequest request, final String memoryName) {
		
		final String compression = request.getHeader("Accept-Encoding");
		final OMMEntity entity = getEntityFromHTTPRequest(request);
		try {
			InputStream gis = null;
			if (compression.contains("gzip"))
				gis = new GZIPInputStream(request.getInputStream());
			else
				gis = request.getInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (gis.available() > 0) {
				byte[] buffer = new byte[gis.available()];
				gis.read(buffer);
				baos.write(buffer);
			}
			gis.close();
			baos.flush();
			baos.close();
			String xml = new String(baos.toByteArray(), "UTF-8").trim();
			
			// update version manager with new OMM
			OMMVersionManager.create(memoryName, HISTORY_STATE).setNewOMM(xml, entity);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Retrieves an OMM entity issuing a request.  
	 * @param request The {@link HttpServletRequest} sent by the user.
	 * @return The {@link OMMEntity} linked to the request. 
	 */
	private static OMMEntity getEntityFromHTTPRequest(final HttpServletRequest request)
	{
		final String entity = request.getHeader("X-OMM-Entity");
		if (entity == null) return null;
		
		String[] parts = entity.split("###");
		
		try
		{
		return new OMMEntity(
				URLDecoder.decode(parts[0], "utf-8"), 
				URLDecoder.decode(parts[1], "utf-8"), 
				ISO8601.getISO8601String(new Date()));
		}
		catch(Exception e){ e.printStackTrace(); }
		
		return null;
	}

//	private static void updateOMMFile(final String memoryName, final String newXMLContent) {
//		final String currentDir = MEMORY_PATH + FILE_SEPARATOR + memoryName;
//
//		final String currentFileStr = currentDir + FILE_SEPARATOR
//				+ CURRENT_FILE_NAME;
//		final File currentFile = new File(currentFileStr);
//		final File directory = currentFile.getParentFile();
//		if (!directory.exists()) {
//			directory.mkdirs();
//		}
//
//		final File freeName = findNextFreeFileName(currentFile);
//
//		currentFile.renameTo(freeName);
//
//		try {
//			FileWriter fw = new FileWriter(currentFileStr);
//			fw.write(newXMLContent);
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

//	private static File findNextFreeFileName(File currentFile) {
//		File[] files = currentFile.getParentFile().listFiles();
//
//		int max = 0;
//		for (File file : files) {
//			try {
//				int iName = Integer
//						.parseInt(file.getName().replace(".xml", ""));
//				if (iName > max)
//					max = iName;
//			} catch (NumberFormatException ex) {
//			}
//		}
//		max++;
//
//		String fileName = currentFile.getParent() + FILE_SEPARATOR + max
//				+ ".xml";
//
//		return new File(fileName);
//	}

	/** Retrieves the name of a user issuing a request.  
	 * @param request The {@link HttpServletRequest} sent by the user.
	 * @return Name of the user who sent the request. 
	 */
	private static String getCurrentUser(HttpServletRequest request)
	{
		HttpSession s = request.getSession();
		if (s == null) return null;
		return (String)s.getAttribute("omm_entity");
	}
}
