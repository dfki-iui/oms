package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.webapp.EditStatus;
import de.dfki.oms.webapp.HTMLData;
import de.dfki.oms.webapp.OMM_HTML_Converter;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds a block's payload as an HTML table. */
public class UpdateHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a block's payload as an HTML table. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("update!");
		partArg = partArg.replace("update_", "");
		String args[] = partArg.split("!");
		String blockName = args[0].split("_")[1];
		OMMBlock block = omm.getBlock(blockName);
		boolean odd = args[3] == "odd" ? true : false;
		switch (args[1].split("_")[0]) {
		case "Payload":
		  String payload = null;
		  //if (block.getFormat().getMIMEType().equals("text/x-lua")){
		    //payload = block.getPayloadAsString();
		  //}
		  //else {
		    payload = OMM_HTML_Converter.convertPayload(block);
		  //}
			Tools.transferBufferWithCompression(
					HTMLData.getKeyValueAsTable("Payload",
					    payload,
							(odd ? "odd" : "even"), block.getID(), "",
							EnumSet.of(EditStatus.Edit, EditStatus.Remove))
							.getBytes(), response, request);
			//OMMFactory.saveOMM(omm, false);
			break;
		default:
			break;
		}
	}

}
