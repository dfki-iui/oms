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

/** Handler that edits an OMM element (from header to block field). */
public class EditHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Edits an OMM element (from header to block field). */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		LOGGER.debug("edit!");
		String editArg = partArg.replace("edit_", "");
		String[] editPath = editArg.split("!");
		String blockname = "";
		boolean odd = false;
		try {
			if (URLDecoder.decode(editPath[0], "UTF-8").equals("header")) {
				LOGGER.debug("try to modify a header entry!");
				Tools.editOMMHeaderEntry(editPath, query, odd, omm, request, response);
				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			blockname = URLDecoder.decode(editPath[0].split("_")[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		OMMBlock block = omm.getBlock(blockname);
		LOGGER.debug("length = " + editPath.length);
		switch (editPath.length) {
		case 5:
			LOGGER.debug("try to modify an entry!");

			Tools.editOMMEntry(editPath, query, block, odd, omm, request, response);
			break;
		case 4: // try to modify a metadata
			LOGGER.debug("try to modify a metadata");
			Tools.editOMMEntry(editPath, query, block, odd, omm, request, response);
			break;

		default:
			break;
		}
	}

}
