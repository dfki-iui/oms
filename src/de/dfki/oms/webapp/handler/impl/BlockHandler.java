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

/** Handler that retrieves a block by its ID given in the request and responds it in form of an HTML table. */
public class BlockHandler implements QueryCommandHandler {

	private String blockName;

	/** Constructor.
	 * @param blockName ID of the block that is retrieved by this handler. 
	 */
	public BlockHandler(String blockName) {
		this.blockName = blockName;
	}
	
	public BlockHandler(){
		
	}
	
	/** Sets the ID of the block that is retrieved by this handler. 
	 * @param blockName ID of the block that is retrieved by this handler. 
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

	/** Retrieves a block by the ID known to the handler and responds it in form of an HTML table. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {
		OMMBlock block = omm.getBlock(blockName);
		if (block == null) {
			return;
		}

		LOGGER.debug("PART:BLOCK_" + blockName + " retrieved!");
		// output as raw xml file
		Tools.transferBufferWithCompression(OMM_HTML_Converter
				.convertOMMBlockToHTMLTable(block, memoryName).getBytes(),
				response, request);

	}

}
