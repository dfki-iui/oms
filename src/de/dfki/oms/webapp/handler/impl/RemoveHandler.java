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

/** Handler that removes an OMM element using a given path. */
public class RemoveHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Removes an OMM element using a given path. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		
		String rmArg = partArg.replace("remove_", "");
		LOGGER.debug("rmArg=" + rmArg);
		if (rmArg == null) {
			System.out.println("block null!");
			return;
		}
		String removePath[] = rmArg.split("!");
		LOGGER.debug("rmArg=" + rmArg);
		try {
			if (URLDecoder.decode(removePath[0], "UTF-8").equals("header")) {
				LOGGER.debug("try to remove a header entry!");
				Tools.removeOMMHeaderEntry(removePath, omm);
				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String blockName = "";
		try {
			blockName = URLDecoder.decode(removePath[0].split("_")[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		OMMBlock block = omm.getBlock(blockName);
		LOGGER.debug("PART:REMOVE_BLOCK_" + blockName + " removed!");
		switch (removePath.length) {
		case 1: // remove a block: eg: rmArg = block_1!
			omm.removeBlock(block, Tools.getEntity(request));
			break;

		case 2: // remove a function eg: rmArg = block_2!Description_2!
			LOGGER.debug("what to remove: " + removePath[1].split("_")[0]);
			Tools.removeBlockFunction(removePath[1].split("_")[0], block, request);
			break;
		case 4: // remove an entry eg:
				// rmArg=block_2!Subject_2!entry_Text!hash_e39e954499abd7ab0315fdbd6132f2395bf59786de413cc2b7e36d1fc5bbc050!
			LOGGER.debug("entry to remove " + removePath[2] + " hash = "
					+ removePath[3]);
			Tools.removeBlockFunctionEntry(removePath[1].split("_")[0],
					removePath[2].split("_")[1], removePath[3].split("_")[1],
					block, request);
			break;
		default:
			break;
		}

	}

}
