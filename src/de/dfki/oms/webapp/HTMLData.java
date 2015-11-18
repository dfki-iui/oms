package de.dfki.oms.webapp;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;

import org.jsoup.Jsoup;

/** A helper class to convert data from different sources and to HTML documents. */
public class HTMLData {
	
	public static String HEADER_PATH_SEPARATOR = "»";
	
	/** Retrieves an HTML header with an opened body element.
	 * 
	 * @param pathCorrection Path to the resources folder.
	 * @param loggedInUser Name of the current user.
	 * @param headerText Title to use in the header.
	 * @param memoryName Memory name to use as part of the storage path. 
	 * @return The HTML document as {@link String}. 
	 */
	public static String getHeader(String pathCorrection, String loggedInUser, String headerText, String memoryName) {
		StringBuilder sb = new StringBuilder();

		sb.append("<!DOCTYPE HTML>");
		sb.append("<html lang=\"en\">");
		sb.append("<head>");

		sb.append("<meta charset=utf-8>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">");
		sb.append("<meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\">");

		sb.append("<meta name=\"HandheldFriendly\" content=\"true\" />");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, height=device-height, user-scalable=no\" />");
		sb.append("<meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />");
		sb.append("<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" />");

		sb.append("<link rel=\"apple-touch-icon\" href=\"" + pathCorrection
				+ "resources/rescom.png\"/>");
		sb.append("<link rel=\"apple-touch-icon-precomposed\" href=\""
				+ pathCorrection + "resources/rescom.png\"/>");
		sb.append("<link rel=\"apple-touch-startup-image\" href=\""
				+ pathCorrection + "resources/rescom.png\">");

		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection + "resources/style.css\">");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection + "resources/tile.css\">");
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection
				+ "resources/calendar_view/dhtmlgoodies_calendar.css\">");
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection
				+ "resources/calendar_view/calendarview.css\">");
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection
				+ "resources/calendar_view/protoplasm.css\">");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection
				+ "resources/calendar_view/timepicker/timepicker.css\">");
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection + "resources/fileuploader.css\">");
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ pathCorrection + "resources/github.css\">");
		
		
		// Roboto font
		sb.append("<link rel=\"stylesheet\" href=\"http://fonts.googleapis.com/css?family=Roboto:regular,italic,bold\">");
		//:regular,medium,thin,italic,mediumitalic
		
		/*
		 * sb.append("<script type=\"text/javascript\">  " + "function load() {"
		 * + "var el = document.getElementById(\"t\");" +
		 * 
		 * "el.addEventListener('touchstart', function(e) { " +
		 * "e.preventDefault(); " +
		 * "offsetLeft = ($(window).width()-$(this).outerWidth(true))/2; " +
		 * "data.x = e.targetTouches[0].pageX - offsetLeft;);}" +
		 * 
		 * "element.addEventListener('touchmove', function(e) {" +
		 * "e.preventDefault();"+
		 * "var diffX = (e.targetTouches[0].pageX - offsetLeft) - data.x;"+
		 * "  if (diffX > 150) $(\"nav.next-prev a.next\").click()" +
		 * "}, false);" + "</script>");
		 */

		sb.append("<title>OMS2"+headerText.replace("<strong>", "").replace("</strong>", "")+"</title>");
		sb.append("</head>");
		//sb.append("<body id=\"_MAIN_PAGE_\" onload=\"load();\">");
		sb.append("<body id=\"_MAIN_PAGE_\">");
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/prototype.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/scriptaculous.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/fileuploader.js\"></script>");
		
		/*
		 * Haiyang Xu 15:03 Oct 4 2011 include resources/actionscripts.js
		 */
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/actionscripts.js\"></script>");
		
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/calendar_view/dhtmlgoodies_calendar.js\"></script>");
		
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/calendar_view/calendarview.js\"></script>");
		
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/calendar_view/protoplasm.js\"></script>");
		sb.append("<script type=\"text/javascript\" src=\"" + pathCorrection
				+ "resources/calendar_view/timepicker/timepicker.js\"></script>");
		sb.append("<script type=\"text/javascript\"> "
				+ "function showElementByID(das)" + "{ "
				+ "if(document.getElementById(das).style.display=='none') "
				+ "document.getElementById(das).style.display=''; "
				+ "else document.getElementById(das).style.display='none';"
				+ "} " + "</script> ");
		sb.append("<script type=\"text/javascript\"> "
				+ "function removeNode(das)"
				+ "{ "
				+ "document.getElementById(das).parentNode.removeChild(document.getElementById(das)); "
				+ "} " + "</script> ");
		sb.append("<script type=\"text/javascript\">\n"
				+ "function setText(id, time, text, altText)\n"
				+ "{\n"
				+ "var c = document.getElementById(id);\n"
				+ "c.innerHTML = text;\n"
				+ "var text = \"setText('\"+id+\"', '\"+time+\"', '\"+altText+\"', '\"+text+\"')\";\n"
				+ "window.setTimeout(text, time);\n" + "}\n" + "</script>\n");
		
		sb.append("<script src=\""+pathCorrection+"resources/rainbow-custom.min.js\" type=\"text/javascript\" charset=\"utf-8\"></script>");		
		//sb.append("<script src=\""+pathCorrection+"resources/ace/src/ace.js\" type=\"text/javascript\" charset=\"utf-8\"></script>");
		//sb.append("<style type=\"text/css\" media=\"screen\">#lua_editor { position: relative;top: 0;right: 0;bottom: 0;left: 0;}</style>");
	
		if (memoryName != null) sb.append("<form name=\"logoffform\" method=\"POST\" action=\""+pathCorrection+memoryName+"/st\"><input type=\"hidden\" name=\"logoff\" value=\"doit\"></form>");
		
		// action bar 
		sb.append("<div class=\"topbar\"><div style=\"color:white;\">OMS2 " + headerText + "</div>" +  
				  "<div style=\"font-size: 0.53em; margin:10px; margin-top:-29px;\" align=\"right\">" +
					(loggedInUser == null ? "" : "User: <span style=\"color:white; font-weight:bold;\">"+loggedInUser+"</span>&nbsp;&nbsp;&nbsp;&nbsp;") +
					(loggedInUser != null ? 
							"<span style=\"cursor: pointer; \" title=\"Logoff this user\" onclick=\"document.logoffform.submit();\">Logoff</span>&nbsp;&nbsp;&nbsp;&nbsp;" :
							"") +
							//"<span style=\"cursor: pointer; \" title=\"Login user\" onclick=\"\">Login</span>") +
							"<br />" +
					//"<span style=\"cursor: pointer; \" title=\"Configure OMS\" onclick=\"alert('Not yet implemented!');\">Settings</span>&nbsp;&nbsp;&nbsp;&nbsp;" +
					//"<span style=\"cursor: pointer; \" title=\"Get additional help\" onclick=\"window.location='/oms/?cmd=help';\">Get Help</span>" +
				"</div>" +
				"</div>");
		
		return sb.toString();
	}

	/** Retrieves an HTML document containing final remarks and closing body and html tags. 
	 * 
	 * @param pathCorrection  Path to the resources folder.
	 * @return The HTML document as {@link String}. 
	 */
	public static String getFooter(String pathCorrection) {
		StringBuilder sb = new StringBuilder();

		sb.append("<br />");
		
		// W3C HTML5 logo
		//sb.append("<a href=\"http://www.w3.org/html/logo/\"><img src=\"http://www.w3.org/html/logo/badge/html5-badge-h-connectivity-css3-device-multimedia-performance-semantics-storage.png\" width=\"325\" height=\"64\" alt=\"HTML5 Powered with Connectivity / Realtime, CSS3 / Styling, Device Access, Multimedia, Performance &amp; Integration, Semantics, and Offline &amp; Storage\" title=\"HTML5 Powered with Connectivity / Realtime, CSS3 / Styling, Device Access, Multimedia, Performance &amp; Integration, Semantics, and Offline &amp; Storage\"></a>");

		// OMM logo
		sb.append("<br /><span style=\"font-size: 0.8em\">Based on <a href=\"http://www.w3.org/2005/Incubator/omm/XGR-omm-20111026/\"><img alt=\"OMM\" align=\"absmiddle\" src=\""+pathCorrection+"/../resources/images/omm_logo.png\" /></a>-Format.&nbsp;&nbsp;A technology enabled by <a href=\"http://www.res-com-project.org\"><img alt=\"RES-COM\" align=\"absmiddle\" src=\""+pathCorrection+"/../resources/images/rescom.png\" /></a></span>");

		//
		// Copyright		
		sb.append("<div style=\"margin:10px; margin-top:-30px;\" align=\"right\">&copy; 2011-2012 <a href=\"http://www.dfki.de\"><img align=\"absmiddle\" src=\""+pathCorrection+"/../resources/images/DFKI.jpg\" alt=\"DFKI GmbH\" /></a></div>");
		sb.append("<div style=\"margin:10px; margin-top:-10px;\" align=\"right\"><span style=\"font-size: 0.8em\">Remark: All time data are presented in UTC time zone.</span></div>");


		
		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
	}

	/** Creates an HTML table text containing a key and value in a row.
	 * 
	 * @param key The key.
	 * @param value The value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getKeyValueAsTable(String key, String value,
			String tableClass, EnumSet<EditStatus> edit) {
		return getKeyValueAsTable(key, value, tableClass, null, null, edit);
	}

	/** Creates an HTML table text containing a key and value in a row.
	 * 
	 * @param key The key.
	 * @param value The value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @param hash Hashed type-value-pair. (Use {@link #Hash} to hash a String.)
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getKeyValueAsTableWithHash(String key, String value,
			String tableClass, EnumSet<EditStatus> edit, String hash) {
		return getKeyValueAsTableWithHash(key, value, tableClass, null, null,
				edit, hash);
	}

	/** Creates an HTML table text containing a key and value in a row.
	 * 
	 * @param key The key.
	 * @param value The value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param tableID ID of the container of the key-value-pair. 
	 * @param style Table style.
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @param hash Hashed type-value-pair. (Use {@link #Hash} to hash a String.)
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getKeyValueAsTableWithHash(String key, String value,
			String tableClass, String tableID, String style,
			EnumSet<EditStatus> edit, String hash) {
		StringBuilder sb = new StringBuilder();

		sb.append("<tr");
		if (tableClass != null)
			sb.append(" class=\"" + tableClass + "\"");
		/*
		 * Haiyang Xu 11.52 Oct 5 2011 add the key to table id
		 */
		if (tableID != null)
			sb.append(" id=\"" + key + "_" + tableID + "\"");
		else
			sb.append(" id=\"entry_" + key + "\"");
		// if(key != null) sb.append(" hash=\""+Tools.Hash(key+value, "")+"\"");
		if (hash != null)
			sb.append(" hash=\"" + hash + "\"");
		if (style != null)
			sb.append(" style=\"" + style + "\"");
		sb.append(">");
		sb.append("<td>");
		sb.append(key);
		sb.append("</td>");
		sb.append("<td>");
		sb.append(value);
		sb.append("</td>");
		if (edit != null && edit.size() > 0)
			sb.append("<td align=\"right\">");
		if (edit != null && edit.contains(EditStatus.Add)) {
			sb.append(getAddButton(key) + "&nbsp;");
		}
		if (edit != null && edit.contains(EditStatus.Edit)) {
			sb.append(getEditButton(key) + "&nbsp;");
		}
		if (edit != null && edit.contains(EditStatus.Remove)) {
			sb.append(getRemoveButton(key));
		}
		sb.append("</td>");
		sb.append("</tr>");

		return sb.toString();
	}

	/** Creates an HTML table text containing a key and value in a row.
	 * 
	 * @param key The key.
	 * @param value The value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param tableID ID of the container of the key-value-pair. 
	 * @param style Table style.
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getKeyValueAsTable(String key, String value,
			String tableClass, String tableID, String style,
			EnumSet<EditStatus> edit) {
		StringBuilder sb = new StringBuilder();

		sb.append("<tr");
		if (tableClass != null)
			sb.append(" class=\"" + tableClass + "\"");
		/*
		 * Haiyang Xu 11.52 Oct 5 2011 add the key to table id
		 */
		if (tableID != null)
			sb.append(" id=\"" + key + "_" + tableID + "\"");
		else
			sb.append(" id=\"entry_" + key + "\"");
		if (key != null)
			sb.append(" hash=\"" + Tools.Hash(key + Jsoup.parse(value).text(), "") + "\"");
		if (style != null)
			sb.append(" style=\"" + style + "\"");
		sb.append(">");
		sb.append("<td>");
		sb.append(key);
		sb.append("</td>");
		sb.append("<td>");
		sb.append(value);
		sb.append("</td>");
		if (edit != null && edit.size() > 0)
		{
			sb.append("<td align=\"right\">");
			if (edit != null && edit.contains(EditStatus.Add)) {
				sb.append(getAddButton(key) + "&nbsp;");
			}
			if (edit != null && edit.contains(EditStatus.Edit)) {
				sb.append(getEditButton(key) + "&nbsp;");
			}
			if (edit != null && edit.contains(EditStatus.Remove)) {
				sb.append(getRemoveButton(key));
			}
			sb.append("</td>");
		}
		sb.append("</tr>");

		return sb.toString();
	}

	/** Creates an HTML table text containing three values in a row.
	 * 
	 * @param value1 First value.
	 * @param value2 Second value.
	 * @param value3 Third value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getTripleAsTable(String value1, String value2,
			String value3, String tableClass, EnumSet<EditStatus> edit) {
		return getTripleAsTable(value1, value2, value3, tableClass, null, null,
				edit);
	}

	/** Creates an HTML table text containing three values in a row.
	 * 
	 * @param value1 First value.
	 * @param value2 Second value.
	 * @param value3 Third value.
	 * @param tableClass Class of the table ("odd" or "even").
	 * @param tableID ID of the container of the key-value-pair. 
	 * @param style Table style.
	 * @param edit Set of allowed edit operations as an {@link EnumSet}<{@link EditStatus}>. 
	 * @return The table as an HTML {@link String}. 
	 */
	public static String getTripleAsTable(String value1, String value2,
			String value3, String tableClass, String tableID, String style,
			EnumSet<EditStatus> edit) {
		StringBuilder sb = new StringBuilder();
		System.out.println("get Triple as table");
		sb.append("<tr");
		if (tableClass != null)
			sb.append(" class=\"" + tableClass + "\"");
		if (tableID != null)
			sb.append(" id=\"" + tableID + "\"");
		else
			sb.append(" id=\"entry_" + value1 + "\"");
		if (value1 != null)
			sb.append(" hash=\"" + Tools.Hash(value1 + Jsoup.parse(value2).text(), "") + "\"");
		if (style != null)
			sb.append(" style=\"" + style + "\"");
		sb.append(">");
		sb.append("<td>");
		sb.append(value1);
		sb.append("</td>");
		sb.append("<td>");
		sb.append(value2);
		sb.append("</td>");
		sb.append("<td>");
		sb.append(value3);
		sb.append("</td>");
		if (edit != null && edit.size() > 0 && !edit.contains(EditStatus.EmptyPlaceholder))
		{
			sb.append("<td align=\"right\">");
			if (edit != null && edit.contains(EditStatus.Add)) {
				sb.append(getAddButton(value1) + "&nbsp;");
			}
			if (edit != null && edit.contains(EditStatus.Edit)) {
				sb.append(getEditButton(value1) + "&nbsp;");
			}
			if (edit != null && edit.contains(EditStatus.Remove)) {
				sb.append(getRemoveButton(value1));
			}
			sb.append("</td>");
		}
		sb.append("</tr>");

		return sb.toString();
	}

	/** Creates an HTML button text for an "add" button.
	 * 
	 * @param innerText Argument of the button operation (what to add).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getAddButton(String innerText) {
		return getButton("add", "button_plus", innerText, "Add '" + innerText + "'");
	}
	
	/** Creates an HTML caption button text for an "add" button.
	 * 
	 * @param innerText Argument of the button operation (what to add).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getCaptionAddButton(String innerText) {
		return getCaptionButton("add", "button_plus", innerText, "Add '" + innerText + "'");
	}

	/** Creates an HTML button text for a "remove" button.
	 * 
	 * @param innerText Argument of the button operation (what to remove).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getRemoveButton(String innerText) {
		/*
		 * Haiyang Xu 11.36 Oct 5 2011 change the first parameter from null to
		 * "rmArg" to test the add remove button
		 */
		return getRemoveButton("remove", innerText);
	}

	/** Creates an HTML button text for a "remove" button.
	 * 
	 * @param rmArg Type of button.
	 * @param innerText Argument of the button operation (what to remove).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getRemoveButton(String rmArg, String innerText) {
		return getButton(rmArg, "button_minus", innerText, "Remove '" + innerText + "'");
	}
	
	/** Creates an HTML caption button text for a "remove" button.
	 * 
	 * @param rmArg Type of button.
	 * @param innerText Argument of the button operation (what to remove).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getCaptionRemoveButton(String rmArg, String innerText) {
		return getCaptionButton(rmArg, "button_minus", innerText, "Remove '" + innerText + "'");
	}

	/** Creates an HTML button text for an "edit" button.
	 * 
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getEditButton() {
		return getButton("edit", "button_edit", "", "Edit");
	}
	
	/** Creates an HTML button text for an "edit" button.
	 * 
	 * @param innerText Argument of the button operation (what to remove).
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getEditButton(String innerText) {
		return getButton("edit", "button_edit", "Edit", "Edit '" + innerText + "'");
	}

	/** Creates a complete HTML document containing a header, a table structure and a footer. 
	 * 
	 * @param pathCorrection Path to the resources folder.
	 * @param caption The document's headline. 
	 * @param structure The document's structure as a {@link TileStructure}. 
	 * @param id ID that is incremented per table cell. 
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getTilePage(String pathCorrection, String caption,
			TileStructure structure, String id) {
		StringBuffer sb = new StringBuffer();

		sb.append(getHeader(pathCorrection, null, "", null));
		sb.append("<h1><div style=\"margin:10px; color:white;\">" + caption
				+ "</div></h1>\n");

		sb.append("<table class=\"tile\">\n");

		for (int i = 0; i < structure.getRows(); i++) {
			sb.append("<tr>\n");

			for (int k = 0; k < structure.getColumns(); k++) {
				Tile tile = structure.getTile(i, k);
				String action = getHTMLAction(tile.getAction());
				sb.append("<td class=\"tile" + (i + 1) + "" + (k + 1)
						+ "\" id=\"" + (id + i + "" + k) + "\" onclick=\""
						+ action + "\" align=\"center\" valign=\"middle\">"
						+ tile.toHtml5(id + i + "" + k) + "</td>\n");
			}

			sb.append("</tr>\n");
		}

		sb.append("</table>\n");

		/*
		 * sb.append(
		 * "<table style=\"margin-top:-10px; border:0; width:100%; border-collapse:separate; border-spacing:10px; font-weight:regular; font-size:42px; \">"
		 * ); sb.append("</table>");
		 */

		sb.append(getFooter(pathCorrection));

		return sb.toString();
	}

	
	/** Creates a complete HTML document containing a header, an error message for a memory that could not be found, and a footer. 
	 * 
	 * @param memoryname Name of the missing memory.
	 * @param pathCorrection Path to the resources folder.
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getMissingMemoryPage(String memoryname, String pathCorrection){
		StringBuffer buffer = new StringBuffer();

		File memory = new File(memoryname);
		buffer.append(getHeader(pathCorrection, null, "> "+memory.getName(), null));

		buffer.append("<h1><p>" + "OMS2" + "</p></h1>");
		buffer.append("<p>The specified Memory \""+memory.getName()+"\" does not exist</p>");
		buffer.append("<p style=\"text-align:left;\">Create new memory "
				+ "<span onclick=\"onAddMemoryClick();\" class=\"button_plus\">&nbsp;Create&nbsp;&nbsp;</span></p><br />");
		buffer.append(getFooter(pathCorrection));

		return buffer.toString();
	}
	
	/** Creates a complete HTML document containing a header, a recovery message for an invalid memory request, and a footer. 
	 * 
	 * @param error Error text to display.
	 * @param pathCorrection Path to the resources folder.
	 * @param memoryPath Directory of the OMS's object memories.
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getNoMemoryPage(String error, String pathCorrection, String memoryPath) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getHeader(pathCorrection, null, "> Error", null));

		buffer.append("<h1><p>" + "OMS2" + "</p></h1>");

		buffer.append("<p>An Error occured: " + error + "</p>");
		buffer.append("<p style=\"text-align:left;\">Create new memory "
				+ "<span onclick=\"onAddMemoryWithoutPathClick();\" class=\"button_plus\">&nbsp;Create&nbsp;&nbsp;</span></p><br />");
		buffer.append("<p>Visit existing memories: </p>");
		File memdir = new File(memoryPath);
		buffer.append("<p><select id=\"select\">");
		if(memdir.exists()){
			File[] mems = memdir.listFiles();
			for(File f : mems){
				if(f.isDirectory()){
					buffer.append("<option value=\"/web/"+f.getName()+"/\">"+f.getName()+"</option>");
				}
			}
		}
		buffer.append("</select>");
		buffer.append("<span onclick=\"onVisitClick();\" class=\"button_plus\">&nbsp;Visit&nbsp;&nbsp;</span></p><br />");
		
		buffer.append(getFooter(pathCorrection));

		return buffer.toString();
	}
	
	/** Creates a complete HTML document containing a header, an error message, and a footer. 
	 * 
	 * @param error Error text to display.
	 * @param pathCorrection Path to the resources folder.
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getErrorPage(String error, String pathCorrection) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getHeader(pathCorrection, null, "> Error", null));

		buffer.append("<h1><p>" + "OMS2" + "</p></h1>");

		buffer.append("<p>An Error occured: " + error + "</p>");
		buffer.append(getFooter(pathCorrection));

		return buffer.toString();
	}
	
	/** Creates a complete HTML document containing a header, a login area, and a footer. 
	 * 
	 * @param memoryName Name of the memory that needs authentication.
	 * @param pathCorrection Path to the resources folder.
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getLoginPage(String memoryName, String pathCorrection) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getHeader(pathCorrection, null, HTMLData.HEADER_PATH_SEPARATOR
				+" <strong>"+memoryName + "</strong> "+HTMLData.HEADER_PATH_SEPARATOR+" Storage " + HTMLData.HEADER_PATH_SEPARATOR + " Login", memoryName));

	
		buffer.append("<br /><br /><p>You have to authenticate to view the memory '"+memoryName+"'. Please login with your email address:</p><br />");
		
		buffer.append("<form method=\"POST\" action=\""+pathCorrection+memoryName+"/st\"> " +
				"<table border=\"0\">" +
				"<tr><td>Email:</td><td><input type=\"text\" name=\"username\" size=\"40\" /></td></tr>" +
				//"<tr><td>Password:</td><td><input type=\"password\" name=\"password\" size=\"25\" /></td></tr>" +
				"</table>" +
				"<p><input type=\"submit\" value=\"Login\" /></p>" +
				"</form><br />");

		buffer.append(getFooter(pathCorrection));

		return buffer.toString();
	}
	
	/** Creates a complete HTML help page. 
	 * 
	 * @param pathCorrection
	 * @return The document as an HTML {@link String}. 
	 */
	public static String getHelpPage(String pathCorrection) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getHeader(pathCorrection, null, "> Get Help", null));

		buffer.append("<h1><p>" + "Get Help" + "</p></h1>");

		buffer.append("<p>Help Page. TODO</p>");

		buffer.append(getFooter(pathCorrection));

		return buffer.toString();
	}

	/** Creates an HTML snippet that either sets the active window's full URI to a given URL or outpus a message via "alert". 
	 * 
	 * @param obj The object of action (a {@link URL} or a {@link String}. 
	 * @return The snippet as an HTML {@link String}. 
	 */
	public static String getHTMLAction(Object obj) {
		if (obj instanceof URL)
			return "self.location.href='" + ((URL) obj).toString() + "'";
		else if (obj instanceof String)
			return "alert('" + ((String) obj) + "');";

		return "";
	}

	/** Creates an HTML button text implementing the specified properties.
	 * 
	 * @param editArg Type of button ("remove", "add" or "edit").
	 * @param additionalClass Class of the button (unless it is "button").
	 * @param innerText Argument of the button operation.
	 * @param tooltipText What to display as a tooltip text. 
	 * @return The button as an HTML {@link String}. 
	 */
	public static String getButton(String editArg, String additionalClass,
			String innerText, String tooltipText) {
		StringBuffer sb = new StringBuffer();

		sb.append("<span class=\"" + ((additionalClass == null || additionalClass.equals("")) ? "button" : additionalClass) + "\" " + "title=\"" + tooltipText + "\" ");

		if (editArg != null && editArg.equals("remove")) {
			sb.append("onclick=\"new onRemoveClick(event, this, '', '?cmd=remove&', { method: 'get', cache: false });\" value=\"click\"");
			sb.append(">" + innerText + "&nbsp;&nbsp;</span>");
		} else if (editArg != null && editArg.equals("add")) {
			sb.append(" addname=\""+innerText+"\" ");
			sb.append("onclick=\"new onAddClick(event, this, '', '?cmd=add&"
					+ innerText
					+ "', { method: 'get', cache: false });\" value=\"click\"");
			sb.append(">" + innerText + "&nbsp;&nbsp;</span>");
		} else if (editArg != null && editArg.equals("edit")) {
			
			sb.append("onclick=\"new onEditClick(event, this, '', '?cmd=add&"
					+ innerText
					+ "', { method: 'get', cache: false });\" value=\"click\"");
			sb.append(">" + innerText + "&nbsp;&nbsp;</span>");
		}
		else if (editArg != null)
		{
			sb.append("onclick=\"new Ajax.Updater('body', '?cmd="+editArg+"&"+innerText+"', {method : 'get', cache : false});\"");
			
			/*sb.append("onclick=\"new onEditClick(event, this, '', '?cmd="+editArg+"&"
					+ innerText
					+ "', { method: 'get', cache: false });\" value=\"click\"");*/
			sb.append(">" + tooltipText + "&nbsp;&nbsp;</span>");
		}

		return sb.toString();
	}
	
	/** Creates an HTML caption button text implementing the specified properties.
	 * 
	 * @param editArg Type of button ("remove", "add" or "edit").
	 * @param additionalClass Class of the button (unless it is "button").
	 * @param innerText Argument of the button operation.
	 * @param tooltipText What to display as a tooltip text. 
	 * @return The button as an HTML {@link String}. 
	 */
	private static String getCaptionButton(String editArg, String additionalClass,
			String innerText, String tooltipText) {
		StringBuffer sb = new StringBuffer();

		sb.append("<span ");
		
		if (editArg != null && editArg.equals("remove")) {
			sb.append("class=\"text_minus\"");
		}
		else if (editArg != null && editArg.equals("add")) {
			sb.append("class=\"text_plus\"");
		}
		
		sb.append(" style=\"color:#888;\" title=\"" + tooltipText + "\" ");

		if (editArg != null && editArg.equals("remove")) {
			sb.append("onclick=\"new onRemoveClick(event, this, '', '?cmd=remove&', { method: 'get', cache: false });\" value=\"click\"");
		} else if (editArg != null && editArg.equals("add")) {
			sb.append(" addname=\""+innerText+"\" ");
			sb.append("onclick=\"new onAddClick(event, this, '', '?cmd=add&"
					+ innerText
					+ "', { method: 'get', cache: false });\" value=\"click\"");
		} else if (editArg != null && editArg.equals("edit")) {
			
			sb.append("onclick=\"new onEditClick(event, this, '', '?cmd=add&"
					+ innerText
					+ "', { method: 'get', cache: false });\" value=\"click\"");
		}

		sb.append(">" + innerText + "&nbsp;&nbsp;</span>");

		return sb.toString();
	}
}
