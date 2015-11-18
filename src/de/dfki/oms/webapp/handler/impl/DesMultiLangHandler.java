package de.dfki.oms.webapp.handler.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.oms.webapp.Tools;
import de.dfki.oms.webapp.handler.QueryCommandHandler;

/** Handler that responds a list of all languages for which the block does not have a description yet. */
public class DesMultiLangHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a list of all languages for which the block does not have a description yet. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("Des multilang");
		
		// get block name
		String multilangarg[] = partArg.split("!");
		String blockName = "";
		try {
			blockName = URLDecoder.decode(multilangarg[1].split("_")[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// get all description languages of the block
		StringBuilder langs = new StringBuilder();
		Locale locales[] = Locale.getAvailableLocales();
		List<String> multilangs = new ArrayList<String>();
		List<String> availableLangs = new ArrayList<String>();
		OMMBlock block = omm.getBlock(blockName);
		List<String> curLangs = new ArrayList<String>();
		if(block.getDescription() != null){
			// Description block exists.
			for (Locale loc : block.getDescription().keySet()) {
				curLangs.add(loc.getDisplayLanguage(Locale.ENGLISH));
			}
		}
		
		// get all available languages
		for (Locale loc : locales) {
			availableLangs.add(loc.getDisplayLanguage(Locale.ENGLISH));
		}
		Tools.removeDuplicate(availableLangs);
		
		// get all languages not used in the block's descriptions
		for (String lang : availableLangs) {
			if (!curLangs.contains(lang))
				multilangs.add(lang);
		}

		// respond these
		Collections.sort(multilangs);
		// langs.deleteCharAt(langs.lastIndexOf(","));
		for (int i = 0; i < multilangs.size(); i++) {
			langs.append(multilangs.get(i));
			langs.append(",");
		}
		langs.deleteCharAt(langs.lastIndexOf(","));
		LOGGER.debug("toString "+langs.toString());
		Tools.transferBufferWithCompression(langs.toString().getBytes(), response, request);
	}

}
