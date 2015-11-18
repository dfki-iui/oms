package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMFormat;
import de.dfki.omm.types.OMMMultiLangText;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler for adding blocks to an OMM. */
public class AddBlockHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Adds an empty block to the given object memory and writes the created block in the response. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		OMMMultiLangText title = new OMMMultiLangText();
		title.put(Locale.ENGLISH, "Sample Block Title, <Please Change!>");
		LOGGER.debug("Try to add a new block");
		OMMEntity creator = Tools.getEntity(request);

		OMMBlock block;
		try
		{
			block = OMMFactory
					.createEmptyOMMBlock(
							omm,
							URI.create("http://www.samplenamespace_please_change.org"),
							URI.create("http://purl.org/dc/dcmitype/Dataset").toURL(),
							title, 
							new OMMFormat("application/xml", null, "none"));

			omm.addBlock(block, creator);
			Tools.transferBufferWithCompression(("block_" + block.getID()).getBytes(), response, request);			
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}		
	}
}
