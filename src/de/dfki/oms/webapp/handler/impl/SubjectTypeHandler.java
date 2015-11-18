package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.types.OMMSubjectTagType;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds a list of all known subject types. */
public class SubjectTypeHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a list of all known subject types. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("subjecttype");
		String subtypes = OMMSubjectTagType.Ontology.name() + ","
				+ OMMSubjectTagType.Text.name();
		Tools.transferBufferWithCompression(subtypes.getBytes(), response,
				request);
	}

}
