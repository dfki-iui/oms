package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.OMMSubjectTag;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds a block's complete subject. */
public class SubjectValueHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a block's complete subject. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("subjectvalue");
		// partArg = partArg.replace("subjectvalue", "");
		String hash = partArg.split("!")[1];

		String blockName = "";
		try {
			blockName = URLDecoder.decode(partArg.split("!")[2].split("_")[1],
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		OMMBlock block = omm.getBlock(blockName);
		OMMSubjectTag tag = Tools.getSubjectTag(block, hash);
		String tagvalues = tag.getValue();
		while (tag.getChild() != null) {
			tag = tag.getChild();
			tagvalues += "," + tag.getValue();
		}
		LOGGER.debug(hash);
		Tools.transferBufferWithCompression(tagvalues.getBytes(), response, request);
	}

}
