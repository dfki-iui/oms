package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler for adding data an OMM or its blocks. */
public class AddHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Adds an entry to an OMM or OMM block. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		
		LOGGER.debug("add!");
		String addArg = partArg.replace("add_", "");
		String[] addPath = addArg.split("!");
		String blockName = "";
		try {
			blockName = URLDecoder.decode(addPath[0].split("_")[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		OMMBlock block = omm.getBlock(blockName);
		boolean odd = false;
		LOGGER.debug("length = " + addPath.length);
		switch (addPath.length) {
		case 3:
			LOGGER.debug("try to add an entry!");
			Tools.addOMMEntry(addPath, query, block, odd, omm, request, response);
			break;
		default:
			break;
		}
	}

}
