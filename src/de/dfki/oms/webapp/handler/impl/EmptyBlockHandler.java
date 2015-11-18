package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.webapp.OMM_HTML_Converter;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds a block without payload. */
public class EmptyBlockHandler implements QueryCommandHandler{

	private String blockName;
	
	/** Constructor.
	 * @param blockName ID of the block that is handled. 
	 */
	public EmptyBlockHandler(String blockName){
		this.blockName = blockName;
	}
	
	public EmptyBlockHandler(){
		
	}
	
	/** Sets the ID of the block that is handled. 
	 * @param blockName ID of the block that is handled. 
	 */
	public void setBlockName(String blockName){
		this.blockName = blockName;
	}
	
	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a block without payload. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		
		OMMBlock block = omm.getBlock(blockName);
		if (block == null) {
			return;
		}

		LOGGER.debug("PART:EMPTY_BLOCK_" + blockName + " retrieved!");
		// output as raw xml file
		Tools.transferBufferWithCompression(OMM_HTML_Converter
				.convertOMMBlockToHTMLEmpty(block, memoryName).getBytes(),
				response, request);
	}

}
