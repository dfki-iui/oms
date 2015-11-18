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

/** Handler that responds a list of all languages for which the block does not have a title yet. */
public class TitleMultiLangHandler implements QueryCommandHandler {

	/** Unused for this handler. */
	@Override
	public void execute() {
	}

	/** Unused for this handler. */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
	}

	/** Responds a list of all languages for which the block does not have a title yet. */
	@Override
	public void execute(HttpServletRequest request,
			HttpServletResponse response, String memoryName, File currentFile,
			String partArg, Map<?, ?> query, OMM omm) {

		LOGGER.debug("multilang");
		String multilangarg[] = partArg.split("!");
		String blockName = "";
		try {
			blockName = URLDecoder.decode(multilangarg[1].split("_")[1],
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		StringBuilder langs = new StringBuilder();
		Locale locales[] = Locale.getAvailableLocales();
		List<String> multilangs = new ArrayList<String>();
		List<String> availableLangs = new ArrayList<String>();
		OMMBlock block = omm.getBlock(blockName);
		List<String> curLangs = new ArrayList<String>();
		for (Locale loc : block.getTitle().keySet()) {
			curLangs.add(loc.getDisplayLanguage(Locale.ENGLISH));
		}
		for (Locale loc : locales) {
			availableLangs.add(loc.getDisplayLanguage(Locale.ENGLISH));
		}
		Tools.removeDuplicate(availableLangs);
		for (String lang : availableLangs) {
			if (!curLangs.contains(lang))
				multilangs.add(lang);
		}

		Collections.sort(multilangs);
		// langs.deleteCharAt(langs.lastIndexOf(","));
		for (int i = 0; i < multilangs.size(); i++) {
			langs.append(multilangs.get(i));
			langs.append(",");
		}
		langs.deleteCharAt(langs.lastIndexOf(","));
		LOGGER.debug(langs.toString());
		Tools.transferBufferWithCompression(langs.toString().getBytes(),
				response, request);
	}

}
