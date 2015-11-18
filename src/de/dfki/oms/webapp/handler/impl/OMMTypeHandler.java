package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.impl.OMMBlockImpl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds all known types used for block payloads. */
public class OMMTypeHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds all known types used for block payloads. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("ommtype");
		String ommtypes = Tools.getString(OMMBlockImpl.getTypes());
		Tools.transferBufferWithCompression(ommtypes.getBytes(), response, request);
	}

}
