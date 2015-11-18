package de.dfki.oms.webapp.handler;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.dfki.omm.interfaces.OMM;
import de.dfki.oms.webapp.OMSOMMHandler;

/** Responsible for a specific type of query command providing OMS functionality according to the command. */
public interface QueryCommandHandler {
	
	public static final Logger LOGGER = Logger.getLogger(OMSOMMHandler.class);

	/** Responds to a request by executing the handler's function on the OMS. */
	public void execute();

	/** Responds to a request by executing the handler's function on the OMS.
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 */
	public void execute(final HttpServletRequest request,
			final HttpServletResponse response);

	/** Responds to a request by executing the handler's function on the OMS.
	 * @param request The {@link HttpServletRequest} sent to the handler. 
	 * @param response The {@link HttpServletResponse} sent back from the handler. 
	 * @param memoryName Name of the memory.
	 * @param currentFile Directory containing the memory folder as a {@link File}. 
	 * @param partArg The query's "part" argument.
	 * @param query The whole query as a {@link Map}<{@link String}, {@link List}<{@link String}>>. 
	 * @param omm The {@link OMM} for which to execute the function. 
	 */
	public void execute(final HttpServletRequest request,
			final HttpServletResponse response, final String memoryName,
			final File currentFile, String partArg, Map<?, ?> query, OMM omm);
}
