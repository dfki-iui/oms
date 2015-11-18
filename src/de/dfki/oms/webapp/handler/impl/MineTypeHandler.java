package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.types.OMMFormat;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds all known MIME types used for {@link OMMFormat}s. */
public class MineTypeHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds all known MIME types used for {@link OMMFormat}s. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("minetype");
		List<String> mimeTypes = OMMFormat.getMIMETypes();
		String mimeTypesString = Tools.getString(mimeTypes);
		Tools.transferBufferWithCompression(mimeTypesString.getBytes(), response, request);
	}

}
