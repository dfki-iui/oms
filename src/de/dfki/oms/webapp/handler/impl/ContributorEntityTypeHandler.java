package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.types.OMMEntity;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that retrieves and responds a list of all known OMM entity types. */
public class ContributorEntityTypeHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Retrieves and responds a list of all known OMM entity types. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("multilang");
		List<String> types = OMMEntity.getEntityTypes();
		String entitytypes = Tools.getString(types);
		Tools.transferBufferWithCompression(entitytypes.getBytes(), response, request);
	}

}
