package de.dfki.oms.webapp;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;

import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.interfaces.OMMHeader;
import de.dfki.omm.interfaces.OMMIdentifierBlock;
import de.dfki.omm.interfaces.OMMStructureBlock;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMEntityCollection;
import de.dfki.omm.types.OMMFormat;
import de.dfki.omm.types.OMMMultiLangText;
import de.dfki.omm.types.OMMStructureInfo;
import de.dfki.omm.types.OMMSubjectCollection;
import de.dfki.omm.types.OMMSubjectTag;
import de.dfki.omm.types.TypedValue;
import de.dfki.oms.history.OMMHistoryElement;
import de.dfki.oms.history.OMMVersionManager;

/** Helper class that converts OMS elements to HTML. */
public class OMM_HTML_Converter {
	
	/** Converts an object memory to an HTML representation. 
	 * 
	 * @param ommVM The memory's {@link OMMVersionManager}. 
	 * @param pathCorrection Path to the resources folder.
	 * @param memoryName The memory's name. 
	 * @param loggedInUser Identifier of the current user.
	 * @return The memory as an HTML {@link String}. 
	 */
	public static String convertOMMToHTML(final OMMVersionManager ommVM,
			final String pathCorrection, final String memoryName,
			final String loggedInUser) {

		OMM omm = ommVM.getCurrentVersion();
		StringBuilder sb = new StringBuilder();
		sb.append(HTMLData.getHeader(pathCorrection, loggedInUser,
				HTMLData.HEADER_PATH_SEPARATOR + " <strong>" + memoryName
						+ "</strong> " + HTMLData.HEADER_PATH_SEPARATOR
						+ " Storage", memoryName));

		// sb.append("<h1><p>Memory '" + ommVM.getMemoryName() + "'</p></h1>");
		sb.append("<br />");

		addBlockView(ommVM, sb);
		addHistoryLine(ommVM, sb);

		// global add block
		sb.append("<p style=\"text-align:right;\">Add new empty block: "
				+ "<span onclick=\"onAddBlockClick();\" class=\"button_plus\">&nbsp;Block&nbsp;&nbsp;</span></p><br />");

		sb.append("<header id=\"header\">");
		sb.append(convertOMMHeaderToHTMLEmpty(omm.getHeader(),
				ommVM.getMemoryName()));
		sb.append("</header>");

		sb.append("<br />");
		sb.append("<br />");

		for (OMMBlock block : omm.getAllBlocks()) {
			String blockIDEncoded = block.getID();
			try {
				blockIDEncoded = URLEncoder.encode(block.getID(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			sb.append("<article id=\"block_" + blockIDEncoded + "\">");
			sb.append(convertOMMBlockToHTMLTable(block, ommVM.getMemoryName()));
			sb.append("</article>");
			sb.append("<br />");
		}

		sb.append(HTMLData.getFooter(pathCorrection));
		return sb.toString();
	}

	/** Adds a view of all blocks in an object memory to a string builder.  
	 * @param ommVM The memory's {@link OMMVersionManager}. 
	 * @param sb The {@link StringBuilder} to which to add the block view. 
	 */
	private static void addBlockView(final OMMVersionManager ommVM, StringBuilder sb) {

		String color = "orange";
		sb.append("<div>Memory structure diagram (not to scale, " +
				  "<span style=\"color:lightgreen\">HEADER</span>, " +
				  "<span style=\"color:orange\">BLOCK</span>, " +
				  "<span style=\"color:red\">SCRIPT</span>):</div>");

		sb.append("<table width=\"100%\" style=\"background:#fff; border-collapse: separate; border-spacing: 5px;\">");
		sb.append("<tr>");
		sb.append("<td style=\"background: lightgreen;\">Memory Header</td>");
		Collection<OMMBlock> blockCollection = ommVM.getCurrentVersion().getAllBlocks();
		int counter = 0;
		boolean firstRound = true;
		int MAX_VALUE = 5;
		if (((blockCollection.size() + 1) % 4) > ((blockCollection.size() + 1) % 5))
			MAX_VALUE = 4;

		for (OMMBlock block : blockCollection) {
			if ((counter == MAX_VALUE - 1 && firstRound)
					|| (!firstRound && counter == MAX_VALUE)) {
				counter = 0;
				firstRound = false;
				sb.append("</tr><tr>");
			}

			String blockIDEncoded = block.getID();
			try {
				blockIDEncoded = URLEncoder.encode(block.getID(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String blockTitle = block.getTitle(Locale.ENGLISH);
			if(blockTitle==null && block.getTitle().keySet().size()>0){
				blockTitle = block.getTitle(block.getTitle().keySet().iterator().next());
			}
			
			String localColor = color;
			if (block.getFormat() != null && block.getFormat().getMIMEType().equals("text/x-lua"))
			{
				localColor = "#F78181";
			}			
			
			sb.append("<td style=\"background: " + localColor
					+ "; cursor: pointer;\" onclick=\"" +
					//"new Ajax.Updater('block_" +
					//blockIDEncoded + "', '" + "st?part=block_" +
					//blockIDEncoded + "', { parameters: { evalJS: true }}); " +
					"Effect.ScrollTo('block_" + blockIDEncoded + "', {" +
					"duration : '1'});\">" + "(" + block.getID() + ") " +
					blockTitle + "</td>");
			color = (color.equals("orange") ? "#FFBE47" : "orange");

			counter++;
		}

		sb.append("</tr></table><br />");
	}

	/** Adds a history line to a string builder, containing the memory's last change and further history. 
	 * @param ommVM The memory's {@link OMMVersionManager}. 
	 * @param sb The {@link StringBuilder} to which to add the history information. 
	 */
	private static void addHistoryLine(final OMMVersionManager ommVM,
			StringBuilder sb) {
		OMMEntity lastChangeEntity = ommVM.getLastChangeEntity();
		Date lastChangeDate = new Date();
		String lceValue = "", lceType = "";
		if (lastChangeEntity != null)
		{
			lastChangeDate = lastChangeEntity.getDate();
			lceValue = lastChangeEntity.getValue();
			lceType = lastChangeEntity.getType();
		}
		String lastChangeDateString = convertToHtml(lastChangeDate);

		System.out.println("Add History Line");
		sb.append("<h3 id=\"_HISTORY_BANNER_\">"
				+ "<div style=\"background:#d9d9d9;\">Last Change: "
				+ lastChangeDateString
				+ " &nbsp;<span style=\"font-size:0.8em;\">by</span>&nbsp; "
				+ lceValue
				+ " <span style=\"font-size:0.8em;\">("
				+ lceType
				+ ")</span> "
				+ "&#x007C; ("
				+ ommVM.getLastChangeTimeSpan()
				+ ")"
				+ "<div style=\""
				+ getTableCaptionConfig()
				+ ""
				+ "cursor: pointer;\" onclick=\""
				+ "document.getElementById('_HISTORY_BANNER_').style.display = 'none';"
				+ "document.getElementById('_HISTORY_TABLE_').style.display = '';"
				+ "\">&#8595; Show History</div></div><br />" + "</h3>");

		sb.append("<div id=\"_HISTORY_TABLE_\" style=\"display:none;\">"
				+ "<div style=\"text-align:right;cursor: pointer;\" onclick=\""
				+ "document.getElementById('_HISTORY_BANNER_').style.display = '';"
				+ "document.getElementById('_HISTORY_TABLE_').style.display = 'none';"
				+ "\">Close</div>"
				+ "<table width=\"100%\"><tr><th>Revision</th><th>Date</th><th>Entity</th><th>Block</th><th>Changes</th><th>Actions</th></tr>");

		int counter = 0;
		for (int version : ommVM.getHistory().keySet()) {
			OMMHistoryElement hist = ommVM.getHistory().get(version);
			OMMEntity entity = hist.OMMEntity;
			System.out.println("histblock: "+hist.OMMBlock);
			String histBlockName = "N/A";
			if(hist.OMMBlock != null){
				histBlockName = hist.OMMBlock.getTitle(Locale.ENGLISH);
				if((histBlockName == null || histBlockName.equals(""))&&hist.OMMBlock.getTitle().keySet().size()>0){
					Locale key = hist.OMMBlock.getTitle().keySet().iterator().next();
					histBlockName = hist.OMMBlock.getTitle(key);
				}
			}
			
			
			
			sb.append("<tr "
					+ (counter == ommVM.getHistory().size() - 1 ? "style=\"background: lightgray;\" "
							: "")
					+ " >"
					+ "<td>"
					+ (counter == ommVM.getHistory().size() - 1 ? "CURRENT ("
							+ version + ")" : "[" + version + "]")
					+ "</td>"
					+ "<td>"
					+ convertToHtml(entity.getDate())
					+ " ("
					+ Tools.getFriendlyTime(new Date().getTime()
							- entity.getDate().getTime())
					+ " ago)</td>"
					+ "<td>"
					+ entity.getValue()
					+ " ("
					+ entity.getType()
					+ ")</td>"
					+ "<td>"
					+ (hist.OMMBlock == null ? "N/A" : "("
							+ hist.OMMBlock.getID()
							+ ") "
							+ Tools.encodeHTML(histBlockName))
					+ "</td>"
					+ "<td>"
					+ (hist.Change == null ? "N/A" : hist.Change)
					+ "</td>"
					+ "<td>"
					+ (counter != 0 ? "<span class=\"button_minus\" onclick=\"new Ajax.Updater('_MAIN_PAGE_', '?cmd=withdraw&version="
							+ version
							+ "', {method : 'get', cache : false});\">"
							+ "Withdraw this version" + "&nbsp;&nbsp;</span>"
							: "") + "</td>" + "</tr>");
			counter++;
		}

		sb.append("</table><br /><br /></div>");
	}

	/** Converts an object memory's header to an HTML table String. 
	 * 
	 * @param header The {@link OMMHeader} to convert. 
	 * @param memoryName The memory's name. 
	 * @return The header as an HTML table snippet. 
	 */
	public static String convertOMMHeaderToHTMLTable(final OMMHeader header, final String memoryName) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"maintable\" border=\"1\">");
		sb.append("<caption style=\"cursor: pointer;\" onclick=\"new Ajax.Updater('header', '"
				+ "st?part=empty_header', { method: 'get', cache: false });\">"
				+ "<span title=\"Collapse Header\">Memory Header</span><div style=\""
				+ getTableCaptionConfig()
				+ "\">&#8593; Collapse</div>"
				+ "</caption>");
		sb.append("<tr class=\"" + "even" + "\">");
		sb.append("<th id=\"products\">Key</th>");
		sb.append("<th>Value</th>");
		sb.append("<th>Action</th>");
		sb.append("</tr>");
		sb.append(HTMLData.getKeyValueAsTable("Version", header.getVersion()
				+ "", "odd", EnumSet.of(EditStatus.EmptyPlaceholder)));

		sb.append(HTMLData.getKeyValueAsTable("Primary ID",
				convertToHtml(header.getPrimaryID(), true), "even",
				EnumSet.of(EditStatus.EmptyPlaceholder)));

		if (header.getAdditionalBlocks() != null)
			sb.append(HTMLData.getKeyValueAsTable("AdditionalBlocks",
					convertToHtml(header.getAdditionalBlocks()), "odd",
					EnumSet.of(EditStatus.Add, EditStatus.Remove)));
		sb.append("</table><br />");

		return sb.toString();
	}

	/** Converts a memory block to an HTML table String. 
	 * 
	 * @param header The {@link OMMBlock} to convert. 
	 * @param memoryName The memory's name. 
	 * @return The header as an HTML table snippet. 
	 */
	public static String convertOMMBlockToHTMLTable(final OMMBlock block, final String memoryName) {
		String blockIDEncoded = block.getID();
		try {
			blockIDEncoded = URLEncoder.encode(block.getID(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		boolean odd = true;
		StringBuilder sb = new StringBuilder();
		// StringBuffer rmArgsb = new StringBuffer();
		// rmArgsb.append("rmArg=");
		// rmArgsb.append("blockId_" + blockIDEncoded + "!" + "Key_Title!" );
		// String rmArg = rmArgsb.toString();

		sb.append("<br /><table class=\"maintable\" border=\"1\">");
		sb.append("<caption style=\"cursor: pointer;\" onclick=\"new Ajax.Updater('block_"
				+ blockIDEncoded
				+ "', '"
				+ "st?part=empty_block_"
				+ blockIDEncoded
				+ "', { method: 'get' });\"><span title=\"Show Block "
				+ block.getID()
				+ " '"
				+ block.getTitle(Locale.ENGLISH)
				+ "'\">Block "
				+ block.getID()
				+ "</span><div style=\""
				+ getTableCaptionConfig()
				+ "\">&#8593; Collapse</div>"
				+ "&nbsp;<div style=\"float:right;padding:0;margin:0;\">"
				+ getGlobalAddButtons(block)
				+ getGlobalRemoveButton(block)
				+ "|&nbsp;&nbsp;</div>" + "</caption>");
		sb.append("<tr class=\"" + "even" + "\">");
		sb.append("<th class=\"keyvalue\">Key</th>");
		sb.append("<th class=\"keyvalue\">Value</th>");
		sb.append("<th class=\"action\">Action</th>");
		sb.append("</tr>");
		if (block.getTitle() != null) {
			sb.append(HTMLData.getKeyValueAsTable("Title",
					convertToHtml(block.getTitle()) + "",
					(odd ? "odd" : "even"), block.getID(), "",
					EnumSet.of(EditStatus.Add)));
			odd = !odd;
		}
		if (block.getNamespace() != null) {
			sb.append(HTMLData.getKeyValueAsTable("Namespace",
					convertToHtml(block.getNamespace()) + "", (odd ? "odd"
							: "even"), block.getID(), "", EnumSet.of(
							EditStatus.Remove, EditStatus.Edit)));
			odd = !odd;
		}
		if (block.getCreator() != null) {
			sb.append(HTMLData.getKeyValueAsTable("Creator",
					"<table style=\"width:100%;\"><th>Entity-Type</th><th>Value</th><th>Date</th>"
							+ convertToHtml(block.getCreator()) + "</table>",
					(odd ? "odd" : "even"), block.getID(), "",
					EnumSet.of(EditStatus.EmptyPlaceholder)));
			odd = !odd;
		}
		if (block.getContributors() != null
				&& block.getContributors().size() > 0) {
			sb.append(HTMLData.getKeyValueAsTable("Contributors",
					convertToHtml(block.getContributors()) + "", (odd ? "odd"
							: "even"), block.getID(), "", EnumSet
							.of(EditStatus.EmptyPlaceholder)));
			odd = !odd;
		}
		if (block.getType() != null) {
			sb.append(HTMLData.getKeyValueAsTable("Type",
					convertToHtml(block.getType()) + "",
					(odd ? "odd" : "even"), block.getID(), "",
					EnumSet.of(EditStatus.Remove, EditStatus.Edit)));
			odd = !odd;
		}
		if (block.getDescription() != null && block.getDescription().size() > 0) {
			sb.append(HTMLData.getKeyValueAsTable("Description",
					convertToHtml(block.getDescription()) + "", (odd ? "odd"
							: "even"), block.getID(), "", EnumSet.of(
							EditStatus.Remove, EditStatus.Add)));
			odd = !odd;
		}
		if (block.getSubject() != null && block.getSubject().size() > 0) {
			sb.append(HTMLData.getKeyValueAsTable("Subject",
					convertToHtml(block.getSubject()) + "", (odd ? "odd"
							: "even"), block.getID(), "", EnumSet.of(
							EditStatus.Remove, EditStatus.Add)));
			odd = !odd;
		}
		if (block.getFormat() != null) {
			sb.append(HTMLData.getKeyValueAsTable("Format",
					convertToHtml(block.getFormat()) + "", (odd ? "odd"
							: "even"), block.getID(), "", EnumSet
							.of(EditStatus.Remove)));
			odd = !odd;
		}
		if (block.getLink() != null) {
			sb.append(HTMLData.getKeyValueAsTable(
					"Link",
					convertLink(block.getFormat(), block.getLink(),
							block.getLinkHash())
							+ "", (odd ? "odd" : "even"), block.getID(), "",
					EnumSet.of(EditStatus.Remove, EditStatus.Edit)));
			odd = !odd;
		}
		if (block.getPayload() != null || block.getPayloadElement() != null) {
			System.out.println("there is payload!");
			sb.append(HTMLData.getKeyValueAsTable("Payload",
					convertPayload(block), (odd ? "odd" : "even"),
					block.getID(), "",
					EnumSet.of(EditStatus.Edit, EditStatus.Remove)));
			odd = !odd;
		}

		sb.append("</table><br /><br />");

		return sb.toString();
	}

	/** Creates an empty HTML table with header table functionality. 
	 * 
	 * @param header An {@link OMMHeader} that is not used at all. 
	 * @param memoryName Name of the memory that is not used either. 
	 * @return An HTML table snippet without content. 
	 */
	public static String convertOMMHeaderToHTMLEmpty(final OMMHeader header, final String memoryName) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table class=\"maintable\" border=\"1\">");
		sb.append("<caption style=\"cursor: pointer;\" onclick=\"new Ajax.Updater('header', '"
				// + memoryName
				+ "st?part=header', { method: 'get', cache: false });\"><span title=\"Show Header\">Memory Header</span><div style=\""
				+ getTableCaptionConfig() + "\">&#8595; Open</div></caption>");
		sb.append("</table>");

		return sb.toString();
	}

	/** Creates an HTML table representing a block without payload. 
	 * 
	 * @param header An {@link OMMBlock} from which to get basic information, like title and ID. 
	 * @param memoryName Name of the memory that contains the block. 
	 * @return An HTML table snippet without content. 
	 */
	public static String convertOMMBlockToHTMLEmpty(final OMMBlock block, final String memoryName) {
		
		if (block == null || memoryName == null)
			return null;

		String title = block.getTitle(Locale.ENGLISH);
		System.out.println("Title: " + title);
		if (title == null) {
			if(block.getTitle().size() > 0){
				System.out.println("blockid: "+block.getID());
				title = block.getTitle().get(
						block.getTitle().keySet().iterator().next());
			}else{
				title="Please Add a Title";
			}
		}
		String blockIDEncoded = block.getID();
		try {
			blockIDEncoded = URLEncoder.encode(block.getID(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"maintable\" border=\"1\">");
		sb.append("<caption style=\"cursor: pointer;\" onclick=\"new Ajax.Updater('block_"
				+ blockIDEncoded
				+ "', '"
				+ "st?part=block_"
				+ blockIDEncoded
				+ "', { method: 'get' });"
				+ "Effect.ScrollTo('block_"
				+ blockIDEncoded
				+ "', {"
				+ "duration : '1'});\">"
				+ "<span title=\"Show Block "
				+ block.getID()
				+ " '"
				+ block.getTitle(Locale.ENGLISH)
				+ "'\">Block "
				+ block.getID()
				+ ": '"
				+ Tools.encodeHTML(title)
				+ "'</span><div style=\""
				+ getTableCaptionConfig() + "\">&#8595; Open</div></caption>");
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts a typed value containing a link to an HTML snippet.
	 * @param link The {@link TypedValue} containing the link.
	 * @param linkHash An optional hash for the link.
	 * @return The typed value as an HTML table snippet. 
	 */
	private static String convertLinkToHtml(final TypedValue link, final String linkHash) {
		
//		String color = "#C0C0C0";
		StringBuilder sb = new StringBuilder();

		String newLinkHash = "";

		if (linkHash != null) {
			for (int i = 0; i < linkHash.length(); i += 4) {
				newLinkHash += linkHash.substring(i, i + 4) + " ";
			}
		}

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr>");
		sb.append("<th>Type</th>");
		sb.append("<th>Link</th>");
		sb.append("<th>Hash</th>");
		// sb.append("<th>Action</th>");
		sb.append("</tr>");
		sb.append("<tr class=\"odd\">");
		sb.append("<td>");
		sb.append(convertToHtml(link.getType()));
		sb.append("</td>");
		sb.append("<td>");
		sb.append(convertToHtml(link.getValue()));
		sb.append("</td>");
		sb.append("<td>");
		sb.append(newLinkHash);
		sb.append("</td>");
		/*
		 * sb.append("<td align=\"right\">");
		 * sb.append("<img style=\"background:" + color +
		 * ";\" title=\"Edit this item\" src=\"./../resources/images/edit.png\" alt=\"[EDIT]\" />&nbsp;&nbsp;"
		 * ); sb.append("<img style=\"background:" + color +
		 * ";\" title=\"Remove this item\" src=\"./../resources/images/remove.png\" alt=\"[REMOVE]\" />"
		 * ); sb.append("</td>");
		 */
		sb.append("</tr>");
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts a block's payload to an HTML representation. 
	 * @param block The {@link OMMBlock} containing the payload.
	 * @return An HTML representation of the payload, varying with different payload types. 
	 */
	public static String convertPayload(OMMBlock block) {
		System.out.println("convert payload!");
		OMMFormat format = block.getFormat();
		TypedValue payload = block.getPayload();
		String encoding = "";

		if (block instanceof OMMStructureBlock) {
			return convertToHtml((OMMStructureBlock) block);
		} else if (block instanceof OMMIdentifierBlock) {
			return convertToHtml((OMMIdentifierBlock) block);
		}

		if (payload != null && payload.getType() != null && !payload.getType().equals(""))
		{
			encoding = payload.getType() + ",";
			System.out.println("type of payload : " + payload.getType());
		}		

		StringBuilder sb = new StringBuilder();

		switch (format.getMIMEType()) {
		case "image/png":
		case "image/jpeg":
		case "image/pjpeg":
		case "image/gif":
		case "image/svg+xml":
		case "image/vnd.microsoft.icon":
			sb.append("<img src=\"data:" + format.getMIMEType() + ";"
					+ encoding + payload.getValue().toString() + "\" />");
			break;
		case "audio/mp4":
		case "audio/mpeg":
		case "audio/ogg":
		case "audio/vorbis":
		case "audio/x-ms-wma":
		case "audio/vnd.wave":
		case "audio/webm":
			sb.append("<audio controls=\"controls\"><source src=\"data:"
					+ format.getMIMEType()
					+ ";"
					+ encoding
					+ payload.getValue().toString()
					+ "\" type=\""
					+ payload.getType()
					+ "\" />Your browser does not support the audio element.</audio>");
			break;
		case "video/mpeg":
		case "video/mp4":
		case "video/ogg":
		case "video/quicktime":
		case "video/x-ms-wmv":
		case "video/webm":
			sb.append("<video controls=\"controls\"><source src=\"data:"
					+ format.getMIMEType()
					+ ";"
					+ encoding
					+ payload.getValue().toString()
					+ "\" type=\""
					+ payload.getType()
					+ "\" />Your browser does not support the video element.</video>");
			break;
		case "text/x-lua":
			String replacedText = block.getPayloadAsString();
			if (replacedText.startsWith("\n")) replacedText = replacedText.substring(1);
			if (replacedText.endsWith("\n")) replacedText = replacedText.substring(0, replacedText.length() - 1);
			replacedText = replacedText.trim();

			sb.append("<div>ADOMe LUA code:</div><pre style=\"white-space: pre-wrap;\"><code data-language=\"lua\">"+replacedText+"</code></pre>");
			sb.append("<script type=\"text/javascript\">Rainbow.color(); console.log(\"Recoloring.\");</script>");
			break;
		case "text/plain":
		case "text/csv":
			try {
				if (payload.getType() == null
						|| payload.getType().equals("none")) {
					sb.append(Jsoup.parse(
							((String) payload.getValue()).replace("\n",
									"<br />")).text());
				} else {
					sb.append(new String(Base64.decodeBase64(((String) payload
							.getValue()).getBytes()), "UTF-8").replace("\n", "<br />"));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		default:
			sb.append(convertToHtml(payload));
			break;
		}

		return sb.toString();
	}

	/** Converts a typed value containing a link to an HTML snippet, respecting its format.
	 * @param format The link's {@link OMMFormat}. 
	 * @param link The {@link TypedValue} containing the link.
	 * @param linkHash An optional hash for the link.
	 * @return The typed value as an HTML table snippet. 
	 */
	public static String convertLink(OMMFormat format, TypedValue link, String linkHash) {
		
		StringBuilder sb = new StringBuilder();

		switch (format.getMIMEType()) {
		case "image/png":
		case "image/jpeg":
		case "image/pjpeg":
		case "image/gif":
		case "image/svg+xml":
		case "image/vnd.microsoft.icon":
			sb.append("<img src=\"" + link.getValue().toString() + "\" />");
			break;
		case "audio/mp4":
		case "audio/mpeg":
		case "audio/ogg":
		case "audio/vorbis":
		case "audio/x-ms-wma":
		case "audio/vnd.wave":
		case "audio/webm":
			sb.append("<audio controls=\"controls\"><source src=\""
					+ link.getValue().toString()
					+ "\" type=\""
					+ format.getMIMEType()
					+ "\" />Your browser does not support the audio element.</audio>");
			break;
		case "video/mpeg":
		case "video/mp4":
		case "video/ogg":
		case "video/quicktime":
		case "video/x-ms-wmv":
		case "video/webm":
			sb.append("<video controls=\"controls\"><source src=\""
					+ link.getValue().toString()
					+ "\" type=\""
					+ format.getMIMEType()
					+ "\" />Your browser does not support the video element.</video>");
			break;
		default:
			sb.append(convertLinkToHtml(link, linkHash));
			break;
		}

		return sb.toString();
	}

	/** Retrieves all add buttons together in an HTML snippet. 
	 * @param block The {@link OMMBlock} for which the add buttons are to be retrieved.
	 * @return An HTML snippet containing all add buttons for the block.
	 */
	private static String getGlobalAddButtons(OMMBlock block) {
		StringBuilder sb = new StringBuilder();

		// CONTRIBUTORS ARE ADDED AUTOMATICALLY
		/*
		 * if (block.getContributors() == null || block.getContributors().size()
		 * < 1) sb.append(HTMLData.getCaptionAddButton("Contributor") +
		 * "&nbsp;");
		 */

		if (block.getType() == null)
			sb.append(HTMLData.getCaptionAddButton("Type") + "&nbsp;");

		if (block.getFormat() == null)
			sb.append(HTMLData.getCaptionAddButton("Format") + "&nbsp;");

		if (block.getDescription() == null || block.getDescription().size() < 1)
			sb.append(HTMLData.getCaptionAddButton("Description") + "&nbsp;");

		if (block.getSubject() == null || block.getSubject().size() < 1)
			sb.append(HTMLData.getCaptionAddButton("Subject") + "&nbsp;");

		if (block.getPayload() == null && block.getPayloadElement() == null
				&& block.getLink() == null) {
			sb.append(HTMLData.getCaptionAddButton("Payload") + "&nbsp;");
			sb.append(HTMLData.getCaptionAddButton("Link") + "&nbsp;");
		}

		if (sb.toString().equals(""))
			return "";

		return "&nbsp;&nbsp;&nbsp;<span align=\"right\">" + sb.toString()
				+ "</span>|";
	}

	/** Retrieves a global remove button as an HTML snippet. 
	 * @param block The {@link OMMBlock} for which the remove button is to be retrieved.
	 * @return Global remove button for the block. 
	 */
	private static String getGlobalRemoveButton(OMMBlock block) {

		String part = HTMLData.getCaptionRemoveButton("remove", " Entire Block&nbsp;");

		return "&nbsp;<span align=\"right\">" + part + "</span>";
	}

	/** Converts a URI to an HTML anchor element. 
	 * @param uri The {@link URI} to convert. 
	 * @return An HTML anchor snippet. 
	 */
	public static String convertToHtml(URI uri) {
		return "<a href=\"" + uri.toASCIIString() + "\">" + uri.toASCIIString()
				+ "</a>";
	}

	/** Converts a URL to an HTML anchor element. 
	 * @param url The {@link URL} to convert. 
	 * @return An HTML anchor snippet. 
	 */
	public static String convertToHtml(URL url) {
		try {
			return convertToHtml(url.toURI());
		} catch (Exception e) {
			return "ERROR";
		}
	}

	/** Converts an OMM subject to an HTML table. 
	 * @param collection The {@link OMMSubjectCollection} to convert. 
	 * @return An HTML table snippet. 
	 */
	private static String convertToHtml(final OMMSubjectCollection collection) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr><th>Subject-Type</th><th>Value</th><th>Actions</th></tr>");
		boolean odd = true;
		for (OMMSubjectTag entry : collection) {
			sb.append(convertToHtml(entry));
			odd = !odd;
		}
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts an OMM entity collection (for example a block's contributors) to an HTML table. 
	 * @param collection The {@link OMMEntityCollection} to convert. 
	 * @return An HTML table snippet. 
	 */
	private static String convertToHtml(final OMMEntityCollection collection) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr><th>Entity-Type</th><th>Value</th><th>Date</th></tr>");
		boolean odd = true;
		for (OMMEntity entry : collection) {
			sb.append(convertToHtml(entry));
			odd = !odd;
		}
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts a single OMM subject tag to an HTML table. 
	 * @param tag The {@link OMMSubjectTag} to convert. 
	 * @return An HTML snippet. 
	 */
	public static String convertToHtml(final OMMSubjectTag tag) {
		switch (tag.getType()) {
		case Ontology:
			try {
				return HTMLData.getKeyValueAsTableWithHash("Ontology",
						convertToHtml(new URL(tag.getValue())), "null",
						EnumSet.of(EditStatus.Edit, EditStatus.Remove),
						Tools.Hash("Ontology" + tag.getValue(), ""));
			} catch (Exception e) {
				e.printStackTrace();
				return tag.toString();
			}
		case Text:
			String retVal = tag.getValue();
			OMMSubjectTag root = tag;
			while (true) {
				if (root.getChild() == null)
					break;
				root = root.getChild();
				retVal += "." + root.getValue();
			}

			return HTMLData.getKeyValueAsTableWithHash("Text", retVal, "null",
					EnumSet.of(EditStatus.Edit, EditStatus.Remove),
					Tools.Hash("Text" + tag.getValue(), ""));
		default:
			break;
		}

		return tag.toString();
	}

	/** Converts a map entry to an HTML table. 
	 * @param collection The {@link Map}.{@link Entry} to convert. 
	 * @param odd Class of the table ("odd" or "even").
	 * @return An HTML table snippet. 
	 */
	public static String convertToHtml(Map.Entry<?, ?> entry, boolean odd) {
		return HTMLData.getKeyValueAsTable(convertToHtml(entry.getKey()),
				convertToHtml(entry.getValue()), odd ? "odd" : "even",
				EnumSet.of(EditStatus.Remove, EditStatus.Edit));
	}

	/** Converts an OMM multi language text map (for example a block's title or description) to an HTML table. 
	 * @param map The {@link OMMMultiLangText} to convert. 
	 * @return An HTML table snippet. 
	 */
	private static String convertToHtml(final OMMMultiLangText map) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr>");
		sb.append("<th class=\"keyvalue\">Key</th>");
		sb.append("<th class=\"keyvalue\">Value</th>");
		sb.append("<th class=\"action\">Action</th>");
		sb.append("</tr>");
		boolean odd = true;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			sb.append(HTMLData.getKeyValueAsTable(
					convertToHtml(entry.getKey()),
					convertToHtml(entry.getValue()), (odd ? "odd" : "even"),
					EnumSet.of(EditStatus.Remove, EditStatus.Edit)));
			odd = !odd;
		}
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts an OMM structure info to an HTML table entry. 
	 * @param info The {@link OMMStructureInfo} to convert. 
	 * @param odd Class of the table ("odd" or "even").
	 * @return An HTML table entry snippet. 
	 */
	public static String convertToHtml(OMMStructureInfo info, boolean odd) {
		StringBuilder sb = new StringBuilder();

		String date = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		if (info.getEndDate() != null) {
			long diff = info.getEndDate().getTime()
					- info.getStartDate().getTime();
			long days = Math.round((double) diff / (24. * 60. * 60. * 1000.)); // diff
																				// in
																				// days

			date = "" + sdf.format(info.getStartDate()) + " -> "
					+ sdf.format(info.getEndDate()) + " (" + days + " "
					+ (days == 1 ? "day" : "days");

		} else {
			date = "Since " + sdf.format(info.getStartDate());
		}

		String hash = Tools.Hash(info.getRelationType().toString()
				+ info.getRelationTarget().getValue(), "");
		sb.append("<tr id=\"entry_" + info.getRelationType().toString()
				+ "\" class=\"" + (odd ? "odd" : "even") + "\" hash=\"" + hash
				+ "\"><td id=\"date" + hash + "\">" + date
				+ "</td><td id=\"source" + hash
				+ "\">&lt;this&gt;</td> <td id=\"relation" + hash + "\">"
				+ info.getRelationType().toString() + "</td> <td id=\"target"
				+ hash + "\">&lt;"
				+ convertToHtml(info.getRelationTarget().getValue())
				+ "&gt;</td><td id=\"actions" + hash + "\">"
				+ HTMLData.getEditButton("OMMStructure") + "&nbsp;&nbsp;"
				+ HTMLData.getRemoveButton(info.getRelationType().toString())
				+ "</td></tr>");

		return sb.toString();
	}

	/** Converts an OMM structure block to an HTML document. 
	 * @param block The {@link OMMStructureBlock} to convert. 
	 * @return An HTML snippet. 
	 */
	private static String convertToHtml(final OMMStructureBlock block) {
		StringBuilder sb = new StringBuilder();

		sb.append("<span><strong>OMM Structure Information:</strong></span><br />");

		sb.append("<table><tr><th>Date</th><th>Source</th><th>Relation</th><th>Target</th><th>Actions</th></tr>");

		boolean odd = true;

		for (OMMStructureInfo info : block.getStructureInfos()) {
			String date = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			if (info.getEndDate() != null) {
				long diff = info.getEndDate().getTime()
						- info.getStartDate().getTime();
				long days = Math.round((double) diff
						/ (24. * 60. * 60. * 1000.)); // diff in days

				date = "" + sdf.format(info.getStartDate()) + " -> "
						+ sdf.format(info.getEndDate()) + " (" + days + " "
						+ (days == 1 ? "day" : "days") + ")";

			} else {
				date = "Since " + sdf.format(info.getStartDate());
			}

			String hash = Tools.Hash(info.getRelationType().toString()
					+ info.getRelationTarget().getValue(), "");
			sb.append("<tr id=\"entry_"
					+ info.getRelationType().toString()
					+ "\" class=\""
					+ (odd ? "odd" : "even")
					+ "\" hash=\""
					+ hash
					+ "\"><td id=\"date"
					+ hash
					+ "\">"
					+ date
					+ "</td><td id=\"source"
					+ hash
					+ "\">&lt;this&gt;</td> <td id=\"relation"
					+ hash
					+ "\">"
					+ info.getRelationType().toString()
					+ "</td> <td id=\"target"
					+ hash
					+ "\">&lt;"
					+ convertToHtml(info.getRelationTarget().getValue())
					+ "&gt;</td><td id=\"actions"
					+ hash
					+ "\">"
					+ HTMLData.getEditButton("OMMStructure")
					+ "&nbsp;&nbsp;"
					+ HTMLData.getRemoveButton(info.getRelationType()
							.toString()) + "</td></tr>");
			odd = !odd;
		}

		sb.append("</table>");

		return sb.toString();
	}

	/** Converts an OMM identifier block to an HTML document. 
	 * @param block The {@link OMMIdentifierBlock} to convert. 
	 * @return An HTML snippet. 
	 */
	private static String convertToHtml(final OMMIdentifierBlock block) {
		StringBuilder sb = new StringBuilder();

		sb.append("<span><strong>OMM Indentification Information:</strong></span><br />");

		sb.append("<table><tr><th>Key</th><th>Value</th><th>Actions</th></tr>");

		boolean odd = true;

		for (TypedValue tv : block.getIdentifier()) {
			sb.append(HTMLData.getKeyValueAsTableWithHash(tv.getType(),
					convertToHtml(tv.getValue()), (odd ? "odd" : "even"),
					EnumSet.of(EditStatus.Remove, EditStatus.Edit),
					Tools.Hash(tv.getType() + tv.getValue(), "")));
			odd = !odd;
		}

		sb.append("</table>");

		return sb.toString();
	}

	/** Converts a typed value to an HTML table. 
	 * @param typedValue The {@link TypedValue} to convert. 
	 * @return An HTML table snippet. 
	 */
	public static String convertToHtml(final TypedValue typedValue) {
		return convertToHtml(typedValue, false);
	}

	/** Converts a typed value to an HTML table. 
	 * @param typedValue The {@link TypedValue} to convert. 
	 * @param forceNoRemove True, if there should be no removal option. 
	 * @return An HTML table snippet. 
	 */
	public static String convertToHtml(final TypedValue typedValue, final boolean forceNoRemove) {
		if (typedValue == null) return "";
		switch (typedValue.getType()) {
		case "email":
			return "<a href=\"mailto:" + typedValue.getValue().toString()
					+ "\">" + typedValue.getValue().toString() + "</a>";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr>");
		sb.append("<th class=\"keyvalue\">Key</th>");
		sb.append("<th class=\"keyvalue\">Value</th>");
		sb.append("<th class=\"action\">Action</th>");
		sb.append("</tr>");
		if (forceNoRemove) {
			sb.append(HTMLData.getKeyValueAsTable(typedValue.getType(),
					convertToHtml(typedValue.getValue().toString()), "odd",
					EnumSet.of(EditStatus.Edit)));
		} else {
			sb.append(HTMLData.getKeyValueAsTable(typedValue.getType(),
					convertToHtml(typedValue.getValue().toString()), "odd",
					EnumSet.of(EditStatus.Remove, EditStatus.Edit)));
		}
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts an OMM format information to an HTML table entry. 
	 * @param format The {@link OMMFormat} to convert. 
	 * @return An HTML table entry snippet. 
	 */
	public static String convertFormatToHtml(final OMMFormat format) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr class=\"odd\"" + " id=\"entry_" + format.getMIMEType()
				+ "\"" + " hash=\""
				+ Tools.Hash(format.getMIMEType() + format.getSchema(), "")
				+ "\">");

		sb.append("<td>" + convertToHtml(format.getMIMEType()) + "</td>");
		if (format.getSchema() != null)
			sb.append("<td>" + convertToHtml(format.getSchema()) + "</td>");
		if (format.getEncryption() != null)
			sb.append("<td>" + convertToHtml(format.getEncryption()) + "</td>");
		sb.append("<td align=\"right\">");
		sb.append(HTMLData.getEditButton("Format"));
		sb.append("</td>");
		sb.append("</tr>");
		return sb.toString();
	}

	/** Converts an OMM format information to an HTML table. 
	 * @param format The {@link OMMFormat} to convert. 
	 * @return An HTML table snippet. 
	 */
	public static String convertToHtml(final OMMFormat format) {
		
//		String color = "#C0C0C0";
		StringBuilder sb = new StringBuilder();

		sb.append("<table style=\"width:100%\" border=\"1\">");
		sb.append("<tr>");
		sb.append("<th>MIME-Type</th>");
		if (format.getSchema() != null)
			sb.append("<th>Schema</th>");
		if (format.getEncryption() != null)
			sb.append("<th>Encryption</th>");
		sb.append("<th>Actions</th>");
		sb.append("</tr>");
		sb.append("<tr class=\"odd\"" + " id=\"entry_" + format.getMIMEType()
				+ "\"" + " hash=\""
				+ Tools.Hash(format.getMIMEType() + format.getSchema(), "")
				+ "\">");

		sb.append("<td>" + convertToHtml(format.getMIMEType()) + "</td>");
		if (format.getSchema() != null)
			sb.append("<td>" + convertToHtml(format.getSchema()) + "</td>");
		if (format.getEncryption() != null)
			sb.append("<td>" + convertToHtml(format.getEncryption()) + "</td>");
		sb.append("<td align=\"right\">");
		sb.append(HTMLData.getEditButton("Format"));
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");

		return sb.toString();
	}

	/** Converts an OMM entity type to an HTML anchor element pointing to a wikipedia page describing the type. 
	 * @param string The entity type to convert as {@link String}. 
	 * @return An HTML snippet. 
	 */
	private static String convertToHtml(final String string) {
		try {
			URL url = new URL(string);
			return convertToHtml(url);

		} catch (Exception _) {
		}

		switch (string) {
		case "url":
			return "<a href=\"http://en.wikipedia.org/wiki/URL\">" + string
					+ "</a>";
		case "email":
			return "<a href=\"http://en.wikipedia.org/wiki/Email\">" + string
					+ "</a>";
		case "duns":
			return "<a href=\"http://en.wikipedia.org/wiki/DUNS\">" + string
					+ "</a>";
		case "base64":
			return "<a href=\"http://en.wikipedia.org/wiki/Base64\">" + string
					+ "</a>";
		case "uuEncode":
			return "<a href=\"http://en.wikipedia.org/wiki/UUEncode\">"
					+ string + "</a>";
		}

		return string.replace(">", "&gt;").replace("<", "&lt;")
				.replace("\"", "&quot;");
	}

	/** Converts an OMM entity to an HTML table. 
	 * @param entity The {@link OMMEntity} to convert. 
	 * @return An HTML table snippet. 
	 */
	public static String convertToHtml(final OMMEntity entity) {
		return (HTMLData.getTripleAsTable(entity.getType(),
				convertToHtml(entity.getValue()),
				convertToHtml(entity.getDate()), "",
				EnumSet.of(EditStatus.EmptyPlaceholder)));
	}

	/** Converts a date to a String to be used in HTML text. 
	 * @param date The {@link Date} to convert. 
	 * @return Date as {@link String}. 
	 */
	private static String convertToHtml(final Date date) 
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ENGLISH);
		tf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String dateStr = df.format(date);
		String timeStr = tf.format(date);
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// return df.format(date);
		return dateStr + " " + timeStr;
	}

	/** Converts a Locale to a String to be used in HTML text. 
	 * @param locale The {@link Locale} to convert. 
	 * @return Date as {@link String}. 
	 */
	private static String convertToHtml(final Locale locale) {
		return locale.getDisplayLanguage(Locale.ENGLISH);
		// return locale.getDisplayName(Locale.ENGLISH);
	}

	/** Tries to convert an object to HTML text. 
	 * @param obj The {@link Object} to convert. The object can be of type {@link Locale}, {@link OMMEntity}, {@link String}, {@link URL}, {@link URI} or {@link OMMSubjectTag}. 
	 * @return The object as a {@link String} representation. 
	 */
	public static String convertToHtml(final Object obj) {
		if (obj instanceof Locale)
			return convertToHtml((Locale) obj);
		else if (obj instanceof OMMEntity)
			return convertToHtml((OMMEntity) obj);
		else if (obj instanceof String) {
			return convertToHtml((String) obj);
		} else if (obj instanceof URL) {
			return convertToHtml((URL) obj);
		} else if (obj instanceof URI)
			return convertToHtml((URI) obj);
		else if (obj instanceof OMMSubjectTag)
			return convertToHtml((OMMSubjectTag) obj);
		return obj.toString();
	}

	/** Retrieves a configuration String for table captions. 
	 * @return The configuration. 
	 */
	private static String getTableCaptionConfig() {
		return "font-size:0.8em;float:right;padding:0;margin:0;color:#888;padding-top:3px;";
	}
}
