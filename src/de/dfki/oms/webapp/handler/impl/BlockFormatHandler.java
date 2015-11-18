package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.OMMFormat;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** A handler to retrieve a requested block and respond its format as a MIME type. */
public class BlockFormatHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Retrieves a requested block and responds its format as a MIME type. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("blockformat");
		String blockName = "";
		try {
			blockName = URLDecoder.decode(partArg.split("!")[1].split("_")[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		OMMBlock block = omm.getBlock(blockName);
		OMMFormat format = block.getFormat();
		Tools.transferBufferWithCompression(format.getMIMEType().getBytes(), response, request);
	}

}
