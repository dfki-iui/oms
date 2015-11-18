package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.oms.webapp.OMM_HTML_Converter;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds an empty header. */
public class EmptyHeaderHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds an empty header. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		LOGGER.debug("PART:EMPTY_HEADER retrieved!");
		// output as raw xml file
		Tools.transferBufferWithCompression(OMM_HTML_Converter
				.convertOMMHeaderToHTMLEmpty(omm.getHeader(), memoryName)
				.getBytes(), response, request);
	}

}
