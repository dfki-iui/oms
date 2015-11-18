package de.dfki.oms.webapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.log4j.Logger;

import de.dfki.omm.impl.OMMBlockImpl;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.impl.OMMIdentifierBlockImpl;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.interfaces.OMMIdentifierBlock;
import de.dfki.omm.interfaces.OMMStructureBlock;
import de.dfki.omm.tools.OMMXMLConverter;
import de.dfki.omm.types.GenericTypedValue;
import de.dfki.omm.types.ISO8601;
import de.dfki.omm.types.OMMEntity;
import de.dfki.omm.types.OMMEntityCollection;
import de.dfki.omm.types.OMMFormat;
import de.dfki.omm.types.OMMMultiLangText;
import de.dfki.omm.types.OMMStructureInfo;
import de.dfki.omm.types.OMMStructureRelation;
import de.dfki.omm.types.OMMSubjectCollection;
import de.dfki.omm.types.OMMSubjectTag;
import de.dfki.omm.types.OMMSubjectTagType;
import de.dfki.omm.types.TypedValue;
import de.dfki.omm.types.URLType;

/** Helpful methods to retrieve often needed information or perform frequent tasks. */
public class Tools {
	
	protected static final Logger LOGGER = Logger.getLogger(OMSOMMHandler.class);

	/** Retrieves time span that is easily readable for humans, for example "4 hours and 7 minutes". 
	 * @param diff A {@link long} number containing the time span in milliseconds.
	 * @return A {@link String} containing the readable time span. 
	 */
	public static String getFriendlyTime(long diff) {
		StringBuffer sb = new StringBuffer();
		long diffInSeconds = (diff) / 1000;

		long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60
				: diffInSeconds;
		long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24
				: diffInSeconds;
		long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30
				: diffInSeconds;
		long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12
				: diffInSeconds;
		long years = (diffInSeconds = (diffInSeconds / 12));

		if (years > 0) {
			if (years == 1) {
				sb.append("a year");
			} else {
				sb.append(years + " years");
			}
			if (years <= 6 && months > 0) {
				if (months == 1) {
					sb.append(" and a month");
				} else {
					sb.append(" and " + months + " months");
				}
			}
		} else if (months > 0) {
			if (months == 1) {
				sb.append("a month");
			} else {
				sb.append(months + " months");
			}
			if (months <= 6 && days > 0) {
				if (days == 1) {
					sb.append(" and a day");
				} else {
					sb.append(" and " + days + " days");
				}
			}
		} else if (days > 0) {
			if (days == 1) {
				sb.append("a day");
			} else {
				sb.append(days + " days");
			}
			if (days <= 3 && hrs > 0) {
				if (hrs == 1) {
					sb.append(" and an hour");
				} else {
					sb.append(" and " + hrs + " hours");
				}
			}
		} else if (hrs > 0) {
			if (hrs == 1) {
				sb.append("an hour");
			} else {
				sb.append(hrs + " hours");
			}
			if (min > 1) {
				sb.append(" and " + min + " minutes");
			}
		} else if (min > 0) {
			if (min == 1) {
				sb.append("a minute");
			} else {
				sb.append(min + " minutes");
			}
			if (sec > 1) {
				sb.append(" and " + sec + " seconds");
			}
		} else {
			if (sec <= 1) {
				sb.append("about a second");
			} else {
				sb.append("about " + sec + " seconds");
			}
		}

		// sb.append(" ago");

		return sb.toString();
	}

	/** Converts a String to a format which can be integrated into HTML without causing errors (no illegal special characters).
	 * @param s The {@link String} to convert. 
	 * @return The converted {@link String}. 
	 */
	public static String encodeHTML(String s) {
		if (s == null) return null;
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>') {
				out.append("&#" + (int) c + ";");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	/** Writes a buffer's contents compressed into a server response. 
	 * 
	 * @param buffer The buffer as a <code>byte[]</code>. 
	 * @param response The {@link HttpServletResponse} to fill with the compressed contents.
	 * @param request The corresponding {@link HttpServletRequest} to retrieve accepted encodings. 
	 */
	public static void transferBufferWithCompression(byte[] buffer, HttpServletResponse response, HttpServletRequest request) {

		String acceptedCompression = request.getHeader("Accept-Encoding");
		if (acceptedCompression != null)
			acceptedCompression = acceptedCompression.toLowerCase();
		String userAgent = request.getHeader("User-Agent");

		if (userAgent != null && userAgent.contains("Mozilla/5.0")
				&& userAgent.contains("Android")
				&& !userAgent.contains("AppleWebKit"))
			acceptedCompression = null;

		try {
			response.setCharacterEncoding("UTF-8");
			OutputStream stream = response.getOutputStream();

			if (acceptedCompression != null && acceptedCompression.contains("gzip")) {
				// System.out.println("Serving with GZIP compression");
				response.setHeader("Content-Encoding", "gzip");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gz = new GZIPOutputStream(baos);
				gz.write(buffer);
				gz.flush();
				gz.close();

				buffer = baos.toByteArray();
				baos.close();
			} else if (acceptedCompression != null && acceptedCompression.contains("deflate")) {
				// System.out.println("Serving with Deflate compression");
				response.setHeader("Content-Encoding", "deflate");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DeflaterOutputStream def = new DeflaterOutputStream(baos);
				def.write(buffer);
				def.flush();
				def.close();

				buffer = baos.toByteArray();
				baos.close();
			}

			response.setContentLength(buffer.length);
			stream.write(buffer);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Writes a file's contents into a server response. 
	 * @param request A {@link HttpServletRequest} to retrieve accepted encodings.
	 * @param response The {@link HttpServletResponse} to fill with the file's contents.
	 * @param file The {@link File}. 
	 * @param contentType The type of content the file holds. 
	 * @throws IOException
	 */
	public static void transferFile(HttpServletRequest request,
			HttpServletResponse response, File file, String contentType)
			throws IOException {
		
		if (file.exists()) {
			response.setContentType(contentType);
			response.setStatus(HttpServletResponse.SC_OK);
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			fis.read(buffer);
			fis.close();
			Tools.transferBufferWithCompression(buffer, response, request);
			/*
			 * System.out.println("File '" + file.getAbsolutePath() +
			 * "' transferred!");
			 */
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			System.err.println("File '" + file.getAbsolutePath() + "' not found!");
		}
	}

	/** Retrieves parameters from an URL. Parameters are key-value-pairs in the form "[key]=[value]" which follow an "?" and are divided by "&"s. 
	 * @param url The URL to scan for parameters.
	 * @return A {@link Map}<{@link String}, {@link List}<{@link String}>> with the found keys and values. 
	 */
	public static Map<String, List<String>> getUrlParameters(String url) {
		try {
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			if (url == null || url.equals("") || url.equals("?null"))
				return params;
			String[] urlParts = url.split("\\?");
			if (urlParts.length > 1) {
				String query = urlParts[1];
				for (String param : query.split("&")) {
					String pair[] = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = URLDecoder.decode(pair[1], "UTF-8");
					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values);
					}
					values.add(value);
					// if(values.contains(value) && values.get(0) != value){
					// String temp = values.get(0);
					// values.set(0, value);
					// values.set(values.indexOf(value), temp);
					// }else
					// values.add(value);
				}
			}
			return params;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Retrieves parameters from an URL. Parameters are key-value-pairs in the form "[key]=[value]" which follow an "?" and are divided by "&"s. 
	 * @param url The URL to scan for parameters.
	 * @return A {@link Map}<{@link String}, {@link String}> with the found keys and values. 
	 */
	public static Map<String, String> getUrlEditParameters(String url) {
		try {
			Map<String, String> params = new HashMap<String, String>();
			if (url == null || url.equals("") || url.equals("?null"))
				return params;
			String[] urlParts = url.split("\\?");
			if (urlParts.length > 1) {
				String query = urlParts[1];
				for (String param : query.split("&")) {
					String pair[] = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = URLDecoder.decode(pair[1], "UTF-8");
					String values = params.get(key);
					if (values == null) {
						values = value;
						params.put(key, values);
					} else {
						params.put(key, values);
					}

				}
			}
			return params;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Retrieves the path of an entity as a collection of key-value-pairs. 
	 * @param actionArg The path as a {@link String}. The key-value-pairs are divided by "!"s and the keys and values themselves by "_"s. 
	 * @return The path divided into its segments an represented as a {@link Map}<{@link String}, {@link String}> containing the pairs. 
	 */
	public static Map<String, String> getActionEntityPath(String actionArg) {
		try {
			Map<String, String> path = new HashMap<String, String>();
			if (actionArg == null || actionArg.equals("") || actionArg.equals("?null"))
				return path;
			String[] entityPath = actionArg.split("\\!");
			if (entityPath.length > 0) {
				for (String pathLevel : entityPath) {
					String pair[] = pathLevel.split("_");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = URLDecoder.decode(pair[1], "UTF-8");
					path.put(key, value);
				}

			}
			return path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Hash method. Use SHA-256 Haiyang Xu Oct 13 2011
	 * 
	 * @param strSrc The source String to hash.
	 * @param encName An encryption algorithm (if this is null or empty, the default SHA-256 will be used).
	 * @return
	 */
	public static String Hash(String strSrc, String encName) {
		
		System.out.println("calculating hash: " + strSrc);
		MessageDigest md = null;
		String strDes = null;

		byte[] bt = strSrc.getBytes();
		try {
			if (encName == null || encName.equals("")) {
				encName = "SHA-256";
			}
			md = MessageDigest.getInstance(encName);
			md.update(bt);
			strDes = bytes2Hex(md.digest()); // to HexString
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return strDes;
	}

	/** Creates a String representation of a list. 
	 * @param list The {@link List} to convert.
	 * @return A {@link String} representing the list's contents.
	 */
	@SuppressWarnings("rawtypes") // List of Strings or URLs, depending on caller
	public static String getString (List list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i).toString());
			sb.append(",");
		}
		sb.deleteCharAt(sb.lastIndexOf(",")); // delete the last ","
		return sb.toString();
	}
	
	/** Converts a <code>byte[]</code> to a Hex String. 
	 * @param bts The <code>byte[]</code> to convert.
	 * @return The created Hex {@link String}.
	 */
	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	/** Retrieves a block's title in a specified language, if it can be found. 
	 * 
	 * @param entryKey The language to find.
	 * @param block The block whose title is to be retrieved.
	 * @return The {@link Entry} of the title map containing the title in the requested language. 
	 */
	public static Map.Entry<?, ?> getTitleEntry(String entryKey, OMMBlock block) { 
		
		Map.Entry<?, ?> ret = null;
		System.out.println("Trying to remove a title! Locale: " + entryKey);
		OMMMultiLangText titlelangs = block.getTitle();
		for (Map.Entry<?, ?> entry : titlelangs.entrySet()) {
			Locale tempLocale = (Locale) entry.getKey();
			String langName = tempLocale.getDisplayName(Locale.ENGLISH);
			if (langName.equals(entryKey)) {
				// block.getTitle().remove(entry.getKey());
				System.out.println("We got the right Title!");
				ret = entry;
				break;
			}
		}
		return ret;
	}

	/** Changes a block's title in a specified language, if it can be found. 
	 * 
	 * @param entryKey The language to find.
	 * @param hash A hash of the locale and title to edit.
	 * @param block The block whose title entry is to be changed. 
	 * @param newValue The new title for the specified language.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 */
	public static void editTitleEntry(String entryKey, String hash,
			OMMBlock block, String newValue, HttpServletRequest request) {
		
		System.out.println("value " + newValue);

		OMMMultiLangText titlelangs = block.getTitle();
		for (Map.Entry<?, ?> entry : titlelangs.entrySet()) {
			Locale tempLocale = (Locale) entry.getKey();
			System.out.println("entrykey: " + entryKey);
			System.out.println("mapkey " + entry.getKey() + " -mapvalue " + entry.getValue());
			// tempLocale = (Locale)(entry.getKey());s

			String tempTitle = (String) entry.getValue();
			String langName = tempLocale.getDisplayName(Locale.ENGLISH);
			System.out.println(entryKey + "_" + tempTitle);
			System.out.println(Hash(entryKey + tempTitle, "") + " =? " + hash);
			if (hash.equals(Hash(langName + tempTitle, ""))) {
				System.out.println("haha got the right title!");
				block.setTitle(tempLocale, newValue, getEntity(request));
				break;
			}

		}
	}

	/** Retrieves a subject tag from a memory block whose name and value fit a given hash value.
	 * 
	 * @param block The {@link OMMBlock} in which to look for the subject tag.
	 * @param hash Hashed name and value of the searched tag. (Use {@link #Hash} to hash a String.)
	 * @return The {@link OMMSubjectTag} if a match is found, otherwise <code>null</code>. 
	 */
	public static OMMSubjectTag getSubjectTag(OMMBlock block, String hash) {
		OMMSubjectTag ret = null;
		for (OMMSubjectTag entry : block.getSubject()) {
			if (hash.equals(Tools.Hash(entry.getType().name() + entry.getValue(), ""))) {
				ret = entry;
				break;
			}
		}
		return ret;
	}

	/** Updates a block's link entry to a new value.
	 * 
	 * @param block The {@link OMMBlock} to update.
	 * @param newLink New link URL.
	 * @return The block's new link entry as a {@link URLType}, or <code>null</code> if the given value is not a valid URL. 
	 * @throws MalformedURLException
	 */
	public static TypedValue updateLink(OMMBlock block, String newLink) throws MalformedURLException {
		TypedValue ret = null;
		ret = new URLType(new URL(newLink));
		if (block != null && ret != null) {
			block.setLink(ret, block.getCreator());
		}
		return ret;
	}

	/** Adds an entry to a block's subject list. 
	 * 
	 * @param block The {@link OMMBlock} to which to add.
	 * @param type The subject tag type (as described in {@link OMMSubjectTagType}). 
	 * @param valuechain All values as a {@link List}<{@link String}>. 
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @return The added {@link OMMSubjectTag}, or <code>null</code> if it could not be created. 
	 */
	public static OMMSubjectTag addSubjectEntry(OMMBlock block, String type, List<String> valuechain, HttpServletRequest request) {
		
		OMMSubjectTag ret = null;
		OMMSubjectTagType newtype = OMMSubjectTagType.valueOf(type);
		OMMSubjectTag newst = new OMMSubjectTag(newtype, valuechain.get(valuechain.size() - 1), null);
		
		if (type.equals(OMMSubjectTagType.Text.toString())) {
			for (int i = valuechain.size() - 2; i >= 0; i--) {
				OMMSubjectTag temp = new OMMSubjectTag(newtype, valuechain.get(i), newst);
				newst = temp;
			}
		}
		if (newst != null) {
			block.addSubject(newst, getEntity(request));
		}
		ret = newst;
		return ret;
	}

	/** Changes an existing subject entry in a block.
	 * 
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @param block The {@link OMMBlock} to edit.
	 * @param type New type of the tag to edit.
	 * @param valuechain New values of the tag to edit.
	 * @param hash Hashed name and value of the tag to edit. (Use {@link #Hash} to hash a String.)
	 * @return The edited {@link OMMSubjectTag}, or <code>null</code> if it could not be found or changed. 
	 */
	public static OMMSubjectTag editSubjectEntry(HttpServletRequest request, OMMBlock block, String type, List<String> valuechain, String hash) {
		
		OMMSubjectTag ret = null;
		OMMSubjectTag st = getSubjectTag(block, hash);
		OMMSubjectTagType newtype = OMMSubjectTagType.valueOf(type);
		
		// build a child as the last in the chain
		OMMSubjectTag newst = new OMMSubjectTag(newtype, valuechain.get(valuechain.size() - 1), null);
		if (type.equals(OMMSubjectTagType.Text.toString())) {
			for (int i = valuechain.size() - 2; i >= 0; i--) {
				OMMSubjectTag temp = new OMMSubjectTag(newtype, valuechain.get(i), newst);
				newst = temp;
			}
		}
		if (st != null && newst != null) {
			block.changeSubject(st, newst, getEntity(request));
			ret = newst;
		}
		return ret;
	}
	
	/** Changes an existing description entry in a block.
	 * 
	 * @param entryKey The Locale to edit (unused).
	 * @param hash Hashed name and value of the tag to edit. (Use {@link #Hash} to hash a String.)
	 * @param block The {@link OMMBlock} to edit.
	 * @param newValue New description.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @return The edited entry, or <code>null</code> if it could not be found or changed. 
	 */
	public static Map.Entry<?, ?> editDescriptionEntry(String entryKey, String hash, OMMBlock block, String newValue,
			HttpServletRequest request) {
		OMMMultiLangText deslangs = block.getDescription();
		Map.Entry<?, ?> ret = null;
		for (Map.Entry<?, ?> entry : deslangs.entrySet()) {
			Locale tempLocale = (Locale) entry.getKey();
			// tempLocale = (Locale)(entry.getKey());s

			String tempTitle = (String) entry.getValue();
			String langName = tempLocale.getDisplayName(Locale.ENGLISH);
			if (hash.equals(Hash(langName + tempTitle, ""))) {
				System.out.println("haha got the right title!");
				block.setDescription(tempLocale, newValue, getEntity(request));
				ret = entry;
				break;
			}
		}
		return ret;
	}

	/** Changes a block's namespace. 
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param newValue New namespace.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @throws URISyntaxException
	 */
	public static void editNamespaceEntry(OMMBlock block, String newValue,
			HttpServletRequest request) throws URISyntaxException {
		URI namespace = new URI(newValue);
		block.setNamespace(namespace, getEntity(request));
	}

	/** Changes a block's type. 
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param newValue New type.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @throws MalformedURLException
	 */
	public static void editTypeEntry(OMMBlock block, String newValue,
			HttpServletRequest request) throws MalformedURLException {
		URL type = new URL(newValue);
		block.setType(type, getEntity(request));
	}

	/** Sets a block's type or replaces an old one.
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param value Type to use on this block.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @return The new type as {@link URL}. 
	 * @throws MalformedURLException
	 */
	public static URL addTypeEntry(OMMBlock block, String value,
			HttpServletRequest request) throws MalformedURLException {
		URL type = new URL(value);
		block.setType(type, getEntity(request));
		return type;
	}

	/** Sets a block's link or replaces an old one.
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param value New link.
	 * @param odd Table class of the response (true for "odd", else "even").
	 * @return The new link formatted as an HTML table.
	 */
	public static String addLinkEntry(OMMBlock block, String value, boolean odd) {

		TypedValue newLink = null;
		try {
			newLink = Tools.updateLink(block, value);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		String Response = HTMLData.getKeyValueAsTable(
						"Link",
						OMM_HTML_Converter.convertLink(block.getFormat(), newLink, null) + "", 
						(odd ? "odd" : "even"),
						block.getID(), "",
						EnumSet.of(EditStatus.Remove, EditStatus.Edit));
		System.out.println(Response);
		return Response;
	}

	/** Changes a block's payload.
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param key New type of the payload.
	 * @param value New value of the payload.
	 * @param hash Hashed type and value of the payload to edit. (Use {@link #Hash} to hash a String.)
	 * @return The new payload as a {@link TypedValue}. 
	 * @throws MalformedURLException
	 */
	public static TypedValue editIdBlockPayloadEntry(OMMBlock block, String key,
			String value, String hash) throws MalformedURLException {
		TypedValue newvalue = null;
		Collection<TypedValue> payloads = ((OMMIdentifierBlock) block)
				.getIdentifier();
		for (TypedValue typedvalue : payloads) {
			if (Tools.Hash(typedvalue.getType() + typedvalue.getValue(), "")
					.equals(hash)) {
				System.out.println("we got the right payload!");
				((OMMIdentifierBlock) block).removeIdentifier(typedvalue);
				if (key.equals("url")) {
					System.out.println("this is a url!");
					newvalue = new URLType(new URL(value));
				} else {
					System.out.println("this is others");
					newvalue = new GenericTypedValue(key, value);
				}
				((OMMIdentifierBlock) block).addIdentifier(newvalue);
				break;
			}
		}
		System.out.println(((OMMIdentifierBlockImpl) block).getIdentifier()
				.size());
		return newvalue;
	}

	/** Changes a structure block's payload.
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param startDate New start date.
	 * @param endDate New end date.
	 * @param relation New relation type (see {@link OMMStructureRelation}).
	 * @param target New relation target.
	 * @param hash Hashed relation type and relation target of the payload to edit. (Use {@link #Hash} to hash a String.)
	 * @return New {@link OMMStructureInfo}, or <code>null</code> if it could not be created. 
	 * @throws MalformedURLException
	 */
	public static Object editStructureInfoBlockPayloadEntry(OMMBlock block,
			String startDate, String endDate, String relation, String target,
			String hash) throws MalformedURLException {
		OMMStructureInfo newInfo = null;
		for (OMMStructureInfo info : ((OMMStructureBlock) block)
				.getStructureInfos()) {

			if (Tools.Hash(
					info.getRelationType().toString()
							+ info.getRelationTarget().getValue(), "").equals(
					hash)) {
				System.out.println("We got the right Payload! + date"
						+ startDate + " " + endDate);
				((OMMStructureBlock) block).removeStructureInfo(info);
				Calendar start = ISO8601.parseDate(startDate);
				Calendar end = ISO8601.parseDate(endDate);
				if ((end == null || end.getTime().before(start.getTime())
						&& start != null)) {
					newInfo = new OMMStructureInfo(
							OMMStructureRelation.valueOf(relation),
							new URLType(new URL(target)), start.getTime());
				} else if (start != null && end != null && end.after(start)) {
					newInfo = new OMMStructureInfo(
							OMMStructureRelation.valueOf(relation),
							new URLType(new URL(target)), start.getTime(),
							end.getTime());
				}

				((OMMStructureBlock) block).addStructureInfo(newInfo);
				System.out.println(((OMMStructureBlock) block)
						.getStructureInfos().size());
				break;
			}
		}
		return newInfo;
	}

	/** Adds a title to a block.
	 * 
	 * @param entryKey New language.
	 * @param block The {@link OMMBlock} to which to add a title.
	 * @param value The new title.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 */
	public static void addTitleEntry(String entryKey, OMMBlock block,
			String value, HttpServletRequest request) {
		OMMMultiLangText titlelangs = block.getTitle();
		Locale newLocale = null;
		for (Locale a : Locale.getAvailableLocales()) {
			if (entryKey.equals(a.getDisplayName(Locale.ENGLISH))) {
				System.out
						.println("We got " + a.getDisplayName(Locale.ENGLISH));
				newLocale = a;
				break;
			}
		}
		if (newLocale != null) {
			titlelangs.put(newLocale, value);
			block.setTitle(newLocale, value, getEntity(request));
			System.out.println("We Add "
					+ newLocale.getDisplayName(Locale.ENGLISH) + " with "
					+ value);
		}
	}

	/** Adds a description to a block.
	 * 
	 * @param entryKey New language.
	 * @param block The {@link OMMBlock} to which to add a description.
	 * @param value The new description.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @return The new description entry as an {@link Map.Entry}<{@link Locale}, {@link String}>. 
	 */
	public static Map.Entry<Locale, String> addDescriptionEntry(
			String entryKey, OMMBlock block, String value,
			HttpServletRequest request) {
		OMMMultiLangText deslangs = block.getDescription();

		if (deslangs == null) {
			// create a new Description Block
			System.out.println("No Description Block present");
			deslangs = new OMMMultiLangText();
			block.setDescription(deslangs, getEntity(request));
		}

		Locale newLocale = null;
		Map.Entry<Locale, String> ret = null;
		System.out.println("Searching for locale " + entryKey);
		for (Locale a : Locale.getAvailableLocales()) {
			System.out.println("checking "+a.getDisplayName(Locale.ENGLISH));
			if ((a.getDisplayName(Locale.ENGLISH)).contains(entryKey)) {
				System.out
						.println("We got " + a.getDisplayName(Locale.ENGLISH));
				newLocale = a;
				break;
			}
		}
		if (newLocale != null) {
			System.out.println("put new locale");
			deslangs.put(newLocale, value);
			for (Map.Entry<Locale, String> entry : deslangs.entrySet()) {

				String langName = entry.getKey().getDisplayName(Locale.ENGLISH);
				if (langName.contains(entryKey)) {
					// block.getTitle().remove(entry.getKey());
					System.out.println("We got the right Title!");
					ret = entry;
					break;
				}
			}
			System.out.println("We Add "
					+ newLocale.getDisplayName(Locale.ENGLISH) + " with "
					+ value);
		}
		return ret;

	}

	/** Adds a contributor to a block.
	 * 
	 * @param entityType Type of the contributor entity.
	 * @param block The {@link OMMBlock} to which to add a contributor.
	 * @param value Identifier of the contributor entity.
	 * @param isotime Time the contributor entity is created or active.
	 * @return The added contributor as an {@link OMMEntity}. 
	 */
	public static OMMEntity addContributorsEntry(String entityType,
			OMMBlock block, String value, String isotime) {
		OMMEntityCollection contributors = block.getContributors();
		OMMEntity newContributor = new OMMEntity(entityType, value, isotime);
		contributors.add(newContributor);
		return newContributor;
	}

	/** Changes a block's creator. 
	 * 
	 * @param hash Hashed creator type and identifier to edit. (Use {@link #Hash} to hash a String.)
	 * @param block The {@link OMMBlock} to edit.
	 * @param newType New creator type.
	 * @param newValue New creator identifier.
	 * @param newIso8601date New creator date. 
	 * @return New creator as an {@link OMMEntity}. 
	 */
	public static OMMEntity editCreatorEntry(String hash, OMMBlock block,
			String newType, String newValue, String newIso8601date) {
		OMMEntity creator = block.getCreator();
		OMMEntity newCreator = new OMMEntity(newType, newValue, newIso8601date);
		if (hash.equals(Hash(creator.getType() + creator.getValue(), ""))) {
			System.out.println("we got the right creator!");
			((OMMBlockImpl) block).setCreator(newCreator);
			return newCreator;
		}
		return null;
	}

	/** Changes a block's contributor. 
	 * 
	 * @param hash Hashed contributor type and identifier to edit. (Use {@link #Hash} to hash a String.)
	 * @param block The {@link OMMBlock} to edit.
	 * @param newType New contributor type.
	 * @param newValue New contributor identifier.
	 * @param newIso8601date New contributor date. 
	 * @return New contributor as an {@link OMMEntity}. 
	 */
	public static OMMEntity editContributorsEntry(String hash, OMMBlock block,
			String newType, String newValue, String newIso8601date) {
		OMMEntityCollection contributors = block.getContributors();
		OMMEntity newContributor = new OMMEntity(newType, newValue,
				newIso8601date);
		for (OMMEntity contributor : contributors) {
			if (hash.equals(Hash(
					contributor.getType() + contributor.getValue(), ""))) {
				System.out.println("find the right contributor!");
				contributors.remove(contributor);
				contributors.add(newContributor);
				break;
			}
		}
		return newContributor;
	}

	/** Changes a block's format entry. 
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param minetype New MIME type.
	 * @param schema New schema.
	 * @param encrypt New encryption.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 * @return The new format as an {@link OMMFormat}.
	 * @throws MalformedURLException
	 */
	public static OMMFormat editFormatEntry(OMMBlock block, String minetype,
			String schema, String encrypt, HttpServletRequest request)
			throws MalformedURLException {
		URL newURL = null;
		System.out.println(minetype + " " + schema + " " + encrypt);
		if (schema != null && !schema.equals("")) {
			newURL = new URL(schema);
		}
		OMMFormat newFormat = new OMMFormat(minetype, newURL, encrypt);
		block.setFormat(newFormat, getEntity(request));
		return newFormat;
	}

	/** Removes a block's metadata entry.
	 * 
	 * @param metadata The entry to remove. 
	 * @param entryKey Key for entries that have keys (for example title or subject).
	 * @param hash Hashed identifier (key and value) for the entry to remove. (Use {@link #Hash} to hash a String.)
	 * @param block The {@link OMMBlock} to edit.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 */
	public static void removeBlockFunctionEntry(String metadata,
			String entryKey, String hash, OMMBlock block,
			HttpServletRequest request) {
		System.out.println("metadata=" + metadata + ",entryKey=" + entryKey
				+ ",hash=" + hash);
		switch (metadata) {
		case "Title":
			System.out.println("Trying to remove a title! Locale: " + entryKey);

			OMMMultiLangText titlelangs = block.getTitle();

			for (Map.Entry<?, ?> entry : titlelangs.entrySet()) {
				Locale tempLocale = (Locale) entry.getKey();
				// tempLocale = (Locale)(entry.getKey());s

				String tempTitle = (String) entry.getValue();
				String langName = tempLocale.getDisplayName(Locale.ENGLISH);
				if (hash.equals(Hash(langName + tempTitle, ""))) {
					block.removeTitle(tempLocale, getEntity(request));
					System.out.println("We got the right Title!");
					break;
				}

			}

			break;
		case "Namespace":
			break;
		case "Creator":
			break;
		case "Contributors":
			break;
		case "Type":
			// block.removeType();
			break;
		case "Format":
			// block.removeFormats();
			break;
		case "Description":
			// block.removeDescriptions();
			System.out.println("Trying to remove a descrption! Locale: "
					+ entryKey);

			OMMMultiLangText mlt = block.getDescription();

			for (Map.Entry<Locale, String> entry : mlt.entrySet()) {

				String langName = entry.getKey().getDisplayName(Locale.ENGLISH);
				if(langName.contains("(")&&langName.contains(")")){
					langName = langName.substring(0, langName.indexOf("(")).trim();
				}
				if (hash.equals(Hash(langName + entry.getValue(), ""))) {
					block.getDescription().remove(entry.getKey());
					System.out.println("We got the right description!");
					break;
				}
			}
			break;
		case "Subject":
			// block.removeSubjects();
			System.out.println("Trying to remove a subject! type: " + entryKey);
			OMMSubjectCollection sc = block.getSubject();
			for (int i = 0; i < sc.size(); i++) {
				String tagType = "";
				String tagValue = sc.get(i).getValue();
				if (sc.get(i).getType() == OMMSubjectTagType.Ontology) {
					tagType = "Ontology";
				} else if (sc.get(i).getType() == OMMSubjectTagType.Text) {
					tagType = "Text";
				}
				if (hash.equals(Hash(tagType + tagValue, ""))) {
					block.removeSubject(sc.get(i), getEntity(request));
					System.out.println("We got the right subject!");
				}
			}
			break;
		case "Payload":
			// block.removePayloads();
			if (block instanceof OMMStructureBlock) {
				for (OMMStructureInfo info : ((OMMStructureBlock) block)
						.getStructureInfos()) {

					if (Tools.Hash(
							info.getRelationType().toString()
									+ info.getRelationTarget().getValue(), "")
							.equals(hash)) {
						System.out.println("We got the right Payload!");
						((OMMStructureBlock) block).removeStructureInfo(info);
						System.out.println(((OMMStructureBlock) block)
								.getStructureInfos().size());
						break;
					}
				}
			} else if (block instanceof OMMIdentifierBlock) {
				for (TypedValue tv : ((OMMIdentifierBlock) block)
						.getIdentifier()) {
					if (Tools.Hash(tv.getType() + tv.getValue(), "").equals(
							hash)) {
						((OMMIdentifierBlock) block).removeIdentifier(tv);
						System.out.println("We got the right Payload!");
						break;
					}
				}
			}

			break;
		default:
			break;
		}
	}

	/** Modifies a payload upload.
	 * 
	 * @param block The {@link OMMBlock} to edit.
	 * @param request A {@link HttpServletRequest} that contains the new value. 
	 * @param response Corresponding {@link HttpServletResponse} (unused).
	 */
	public static void updateLink(OMMBlock block,
			final HttpServletRequest request, final HttpServletResponse response) {
		try {
			InputStream gis = null;
			gis = request.getInputStream();

			request.getHeader("X-File-Name");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (gis.available() > 0) {
				byte[] buffer = new byte[gis.available()];
				gis.read(buffer);
				baos.write(buffer);
			}
			gis.close();
			baos.flush();
			baos.close();

			block.setPayload(baos.toByteArray(), getEntity(request));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Removes a block's metadata entry.
	 * 
	 * @param metadata The entry to remove. 
	 * @param block The {@link OMMBlock} to edit.
	 * @param request A {@link HttpServletRequest} to retrieve the changing entity.
	 */
	public static void removeBlockFunction(String metadata, OMMBlock block,
			HttpServletRequest request) {
		switch (metadata) {
		case "Title":
			break;
		case "Namespace":
			break;
		case "Creator":
			break;
		case "Contributors":
			break;
		case "Type":
			block.removeType(getEntity(request));
			break;
		case "Format":
			if (block.getPayload() != null || block.getPayloadElement() != null)
				break;
			else
				block.removeFormat(getEntity(request));
			break;
		case "Description":
			block.removeDescriptions(getEntity(request));
			break;
		case "Subject":
			block.removeSubjects(getEntity(request));
			break;
		case "Link":
			block.removeLink(getEntity(request));
		case "Payload":
			block.removePayload(getEntity(request));
			break;
		default:
			break;
		}
	}

	/** Removes duplicates from a {@link List} of {@link String}s.
	 * 
	 * @param arlList The list.
	 */
	public static void removeDuplicate(List<String> arlList) {
		HashSet<String> h = new HashSet<String>(arlList);
		arlList.clear();
		arlList.addAll(h);
	}

	/** Removes the header from an OMM. 
	 * 
	 * @param removePath Entry and hashed type of the header.
	 * @param omm The {@link OMM} from which to remove the header. 
	 */
	public static void removeOMMHeaderEntry(String[] removePath, OMM omm) {
		String entry = removePath[1].split("_")[1];
//		String key = removePath[2].split("_")[1];
		String hash = removePath[3].split("_")[1];
		TypedValue temp;
		String temphash = "";
		switch (entry) {
		case "Primary ID":
			temp = omm.getHeader().getPrimaryID();
			temphash = Tools.Hash(
					temp.getType()
							+ OMM_HTML_Converter.convertToHtml(temp.getValue()
									.toString()), "");
			if (temphash.equals(hash)) {
				omm.getHeader().setPrimaryID(null);
				OMMFactory.saveOMM(omm, false);
			} else
				break;
		case "AdditionalBlocks":
			temp = omm.getHeader().getAdditionalBlocks();
			temphash = Tools.Hash(
					temp.getType()
							+ OMM_HTML_Converter.convertToHtml(temp.getValue()
									.toString()), "");
			if (temphash.equals(hash)) {
				omm.getHeader().setAdditionalBlocks(null);
				OMMFactory.saveOMM(omm, false);
			} else
				break;
		default:
			break;
		}
	}

	/** Parses an OMM usable entity from a request. 
	 * 
	 * @param request A {@link HttpServletRequest} from which to retrieve the entity. 
	 * @return The parsed {@link OMMEntity}. 
	 */
	public static OMMEntity getEntity(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		String type = "ipv6";

		if (request.getHeader("X_FORWARDED_FOR") != null) {
			remoteAddr = request.getHeader("X_FORWARDED_FOR");
		}

		if (InetAddressValidator.getInstance().isValidInet4Address(remoteAddr))
			type = "ipv4";

		HttpSession session = request.getSession(true);

		if (session != null) {
			String userName = (String) session.getAttribute("omm_entity");
			if (userName != null) {
				type = "email";
				remoteAddr = userName;
			}
		}

		if (request.getUserPrincipal() != null) {
			remoteAddr = request.getUserPrincipal().getName();
			type = "http_auth";
		}

		OMMEntity e = new OMMEntity(type, remoteAddr,
				ISO8601.getISO8601String(new Date()));

		return e;
	}

	/** Changes an object memory's header. 
	 * 
	 * @param editPath Entry and type of the header.
	 * @param query New value. 
	 * @param odd Table class of the response (true for "odd", else "even"). Unused.
	 * @param omm The {@link OMM} whose header is changed. 
	 * @param request A {@link HttpServletRequest} to retrieve accepted encodings. 
	 * @param response Corresponding {@link HttpServletResponse}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void editOMMHeaderEntry(String editPath[], Map query,
			boolean odd, OMM omm, final HttpServletRequest request,
			final HttpServletResponse response) {
		
		Map<String, List<String>> _query = query;
		String value = _query.get("value").get(0);
		switch (editPath[1].split("_")[1]) {
		case "Primary ID":

			omm.getHeader().setPrimaryID(OMMXMLConverter.getTypedValue(editPath[2].split("_")[1], value));
			final TypedValue pid = omm.getHeader().getPrimaryID();
			Tools.transferBufferWithCompression(

					HTMLData.getKeyValueAsTable(
							pid.getType(),
							OMM_HTML_Converter.convertToHtml(pid.getValue() .toString()), 
							"odd",
							EnumSet.of(EditStatus.Remove, EditStatus.Edit))
						.getBytes(), response, request);

			// OMMFactory.saveOMM(omm, false);
			break;
		case "AdditionalBlocks":
			omm.getHeader().setAdditionalBlocks(OMMXMLConverter.getTypedValue(editPath[2].split("_")[1], value));
			final TypedValue ab = omm.getHeader().getAdditionalBlocks();
			Tools.transferBufferWithCompression(

					HTMLData.getKeyValueAsTable(
							ab.getType(),
							OMM_HTML_Converter.convertToHtml(ab.getValue()
									.toString()), "odd",
							EnumSet.of(EditStatus.Remove, EditStatus.Edit))
							.getBytes(), response, request);

			// OMMFactory.saveOMM(omm, false);
		default:
			break;
		}
	}

	/** Changes an OMM entry. 
	 * 
	 * @param editPath Full path of the entry to change. 
	 * @param query New value. 
	 * @param block The {@link OMMBlock} to edit.
	 * @param odd Table class of the response (true for "odd", else "even").
	 * @param omm The {@link OMM} to edit. 
	 * @param request A {@link HttpServletRequest} to retrieve accepted encodings or a changing entity.
	 * @param response Corresponding {@link HttpServletResponse} to fill with results if specified.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void editOMMEntry(String editPath[], Map query,
			OMMBlock block, boolean odd, OMM omm,
			final HttpServletRequest request, final HttpServletResponse response) {
		
		Map<String, List<String>> _query = query;
		String value, entitytype, date, time, key, startdate, starttime, enddate, endtime, relation, target, subjecttype;
		System.out.println(editPath[1].split("_")[0]);
		switch (editPath[1].split("_")[0]) {
		case "Title":
			value = _query.get("value").get(0);
			Tools.editTitleEntry(editPath[2].split("_")[1], editPath[3].split("_")[1], block, value, request);

			if (editPath[4] == "odd")
				odd = true;

			// OMMFactory.saveOMM(omm, false);
			for (String s : editPath) {
				System.out.println("editPath: " + s);
			}
			Map.Entry<?, ?> entry = Tools
					.getTitleEntry(editPath[2].split("_")[1], block);
			System.out.println("key: " + entry.getKey() + " value: "
					+ entry.getValue());
			System.out.println(OMM_HTML_Converter.convertToHtml(entry, odd));
			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(entry, odd)).getBytes(),
					response, request);
			break;
		case "Creator":
			LOGGER.debug("Creator!");
			value = _query.get("value").get(0);

			entitytype = _query.get("entitytype").get(0);
			date = _query.get("date").get(0);
			time = _query.get("time").get(0);

			System.out.println(value + entitytype + date + time);
			DateFormat format = null;
			date = date.trim();
			time = time.trim();
			if (date.split("/")[2].length() == 4) {
				date = date.substring(0, date.length() - 4)
						+ date.substring(date.length() - 2);
				System.out.print(date);
			}
			if (time.split(":").length == 3) {
				time = time.substring(0, time.length() - 6)
						+ time.substring(time.length() - 3);
			}

			format = new SimpleDateFormat("MM/dd/yy hh:mm aa");
			Date tempdate = null;
			try {
				tempdate = format.parse(date + " " + time);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			String iso8601time = ISO8601.getISO8601String(tempdate);

			// String iso8601time = date.trim() + "T" + time.trim() + "+02:00";
			OMMEntity newCreator = Tools.editCreatorEntry(
					editPath[3].split("_")[1], block, entitytype, value,
					iso8601time);

			System.out.println("date :" + newCreator.getDateAsISO8601());
			if (editPath[4] == "odd")
				odd = true;

			// OMMFactory.saveOMM(omm, false);
			Tools.transferBufferWithCompression(OMM_HTML_Converter
					.convertToHtml(newCreator).getBytes(), response, request);
			break;

		case "Namespace":
			LOGGER.debug("hahaNamespace!");
			value = _query.get("value").get(0);
			try {
				Tools.editNamespaceEntry(block, value, request);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (editPath[3] == "odd") {
				odd = true;
			}
			// OMMFactory.saveOMM(omm, false);
			String res = HTMLData
					.getKeyValueAsTable(
							"Namespace",
							OMM_HTML_Converter.convertToHtml(block
									.getNamespace()) + "", (odd ? "odd"
									: "even"), block.getID(), "", EnumSet.of(
									EditStatus.Remove, EditStatus.Edit));
			Tools.transferBufferWithCompression(res.getBytes(), response,
					request);
			break;

		case "Type":
			value = _query.get("value").get(0);
			try {
				Tools.editTypeEntry(block, value, request);
			} catch (MalformedURLException e) {
				
				e.printStackTrace();
			}
			if (editPath[3] == "odd") {
				odd = true;
			}
			// OMMFactory.saveOMM(omm, false);
			String resp = HTMLData.getKeyValueAsTable("Type",
					OMM_HTML_Converter.convertToHtml(block.getType()) + "",
					(odd ? "odd" : "even"), block.getID(), "",
					EnumSet.of(EditStatus.Remove, EditStatus.Edit));
			Tools.transferBufferWithCompression(resp.getBytes(), response,
					request);
			break;
		case "Contributors":
			LOGGER.debug("Contributors!");
			value = _query.get("value").get(0);
			entitytype = _query.get("entitytype").get(0);
			date = _query.get("date").get(0);
			time = _query.get("time").get(0);

			if (date.split("/")[2].length() == 4) {
				date = date.substring(0, date.length() - 4)
						+ date.substring(date.length() - 2);
				System.out.print(date);
			}
			if (time.split(":").length == 3) {
				time = time.substring(0, time.length() - 6)
						+ time.substring(time.length() - 3);
			}

			DateFormat _format = new SimpleDateFormat("MM/dd/yy hh:mm aa");
			Date _tempdate = null;
			try {
				_tempdate = _format.parse(date + " " + time);
			} catch (ParseException e1) {
				
				e1.printStackTrace();
			}
			String isotime = ISO8601.getISO8601String(_tempdate);
			// String isotime = date.trim() + "T" + time.trim() + "+02:00";
			OMMEntity newContributor = Tools.editContributorsEntry(
					editPath[3].split("_")[1], block, entitytype, value,
					isotime);
			if (editPath[4] == "odd") {
				odd = true;
			}
			// OMMFactory.saveOMM(omm, false);
			if (newContributor != null) {
				Tools.transferBufferWithCompression(OMM_HTML_Converter
						.convertToHtml(newContributor).getBytes(), response,
						request);
			}
			break;
		case "Format":
			LOGGER.debug("Format");
			String schema = null;
			String encryption = null;
			String minetype = _query.get("minetype").get(0);
			OMMFormat newFormat = null;
			if (minetype.equals("application/xml")) {
				schema = null;
				encryption = null;
				if (_query.get("schema") != null)
					schema = _query.get("schema").get(0);
				if (_query.get("encryption") != null)
					encryption = _query.get("encryption").get(0);
			}
			try {
				newFormat = Tools.editFormatEntry(block, minetype, schema,
						encryption, request);
			} catch (MalformedURLException e) {
				
				e.printStackTrace();
			}
			String trans = OMM_HTML_Converter.convertFormatToHtml(newFormat);
			LOGGER.debug(trans);
			// OMMFactory.saveOMM(omm, false);
			Tools.transferBufferWithCompression(OMM_HTML_Converter
					.convertFormatToHtml(newFormat).getBytes(), response,
					request);

			break;
		case "Description":
			value = _query.get("value").get(0);

			Map.Entry<?, ?> ret = Tools.editDescriptionEntry(
					editPath[2].split("_")[1], editPath[3].split("_")[1],
					block, value, request);

			if (editPath[4] == "odd")
				odd = true;

			// OMMFactory.saveOMM(omm, false);
			LOGGER.debug(OMM_HTML_Converter.convertToHtml(ret, odd));
			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(ret, odd)).getBytes(),
					response, request);
			break;
		case "Subject":
			// value = _query.get("value").get(0);
			subjecttype = _query.get("subjecttype").get(0);
			List<String> valuechain = _query.get("value");
			// LOGGER.debug(_query.get("value").size());
			if (editPath[4] == "odd")
				odd = true;
			OMMSubjectTag newst = Tools.editSubjectEntry(request, block,
					subjecttype, valuechain, editPath[3].split("_")[1]);
			// OMMFactory.saveOMM(omm, false);
			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(newst)).getBytes(),
					response, request);
			break;
		case "Payload":
			if (block instanceof OMMIdentifierBlock) {
				LOGGER.debug("IndentifierBlock!");
				value = _query.get("value").get(0);
				key = _query.get("key").get(0);
				TypedValue tv = null;
				try {
					tv = Tools.editIdBlockPayloadEntry(block, key, value, editPath[3].split("_")[1]);
				} catch (MalformedURLException e) {
					
					e.printStackTrace();
				}
				if (editPath[4] == "odd")
					odd = true;
				String temp = HTMLData.getKeyValueAsTableWithHash(tv.getType(),
						OMM_HTML_Converter.convertToHtml(tv.getValue()),
						(odd ? "odd" : "even"), EnumSet.of(EditStatus.Remove,
								EditStatus.Edit), Tools.Hash(
								tv.getType() + tv.getValue(), ""));
				Tools.transferBufferWithCompression(temp.getBytes(), response,
						request);
				LOGGER.debug(((OMMIdentifierBlock) block).getIdentifier()
						.size());
				// OMMFactory.saveOMM(omm, false);
			} else if (block instanceof OMMStructureBlock) {
				LOGGER.debug("trying to edit a payload of an OMMStructureBlock!");
				startdate = _query.get("startdate").get(0);
				starttime = _query.get("starttime").get(0);
				enddate = _query.get("enddate").get(0);
				endtime = _query.get("endtime").get(0);
				relation = _query.get("relation").get(0);
				target = _query.get("target").get(0);
				if (starttime.trim().length() == 5) {
					starttime = starttime + ":00";
				}
				if (endtime.trim().length() == 5) {
					endtime = endtime + ":00";
				}
				String isotimestart = startdate.trim() + "T" + starttime.trim();
				// + "+02:00";
				String isotimeend = enddate.trim() + "T" + endtime.trim();
				// + "+02:00";
				OMMStructureInfo newInfo = null;
				if (editPath[4] == "odd")
					odd = true;
				try {
					newInfo = (OMMStructureInfo) Tools
							.editStructureInfoBlockPayloadEntry(block,
									isotimestart, isotimeend, relation, target,
									editPath[3].split("_")[1]);
					Tools.transferBufferWithCompression(OMM_HTML_Converter
							.convertToHtml(newInfo, odd).getBytes(), response,
							request);
					// OMMFactory.saveOMM(omm, false);

				} catch (MalformedURLException e) {
					
					e.printStackTrace();
				}

			} else {
				LOGGER.debug("Trying to modify a payload upload!");
				Tools.updateLink(block, request, response);
				Tools.transferBufferWithCompression(
						"{success: true}".getBytes(), response, request);
				// OMMFactory.saveOMM(omm, false);
			}
			/*
			 * value = _query.get("value");
			 * 
			 * entitytype = _query.get("entitytype"); date = _query.get("date");
			 * time = _query.get("time");
			 * 
			 * System.out.println(value + entitytype + date + time);
			 * 
			 * String iso8601time = date.trim() + "T" + time.trim() + "+02:00";
			 * Tools.editCreatorEntry(editPath[3].split("_")[1], block,
			 * entitytype, value, iso8601time); System.out .println("date :" +
			 * block.getCreator().getDateAsISO8601()); if (editPath[4] == "odd")
			 * odd = true;
			 * 
			 * OMMFactory.saveOMM(omm, false);
			 * Tools.transferBufferWithCompression(OMM_HTML_Converter
			 * .convertToHtml(block.getCreator()).getBytes(), response,
			 * request);
			 */
			break;
		case "Link":
			LOGGER.debug("modify link!");
			value = _query.get("value").get(0);
			if (editPath[3] == "odd") {
				odd = true;
			}
			try {
				TypedValue newLink;
				newLink = Tools.updateLink(block, value);
				String Response = HTMLData.getKeyValueAsTable(
						"Link",
						OMM_HTML_Converter.convertLink(block.getFormat(),
								newLink, null) + "", (odd ? "odd" : "even"),
						block.getID(), "",
						EnumSet.of(EditStatus.Remove, EditStatus.Edit));
				Tools.transferBufferWithCompression(Response.getBytes(), response, request);
				// OMMFactory.saveOMM(omm, false);
			} catch (MalformedURLException e) {
				
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}

	/** Adds an entry to an OMM or block. 
	 * 
	 * @param addPath Full path of the entry to add. 
	 * @param query New value. 
	 * @param block The {@link OMMBlock} to which to add.
	 * @param odd Table class of the response (true for "odd", else "even").
	 * @param omm The {@link OMM}  to which to add. 
	 * @param request A {@link HttpServletRequest} to retrieve accepted encodings or a changing entity.
	 * @param response Corresponding {@link HttpServletResponse} to fill with results if specified.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addOMMEntry(String addPath[], Map query, OMMBlock block,
			boolean odd, OMM omm, final HttpServletRequest request,
			final HttpServletResponse response) {
		
		Map<String, List<String>> _query = query;
		String value, languageName, entitytype, date, time;
		System.out.println(addPath[1].split("_")[0]);
		switch (addPath[1].split("_")[0]) {
		case "Title":
			LOGGER.debug("add a title!");
			languageName = _query.get("multilangkey").get(0);
			value = _query.get("value").get(0);
			Tools.addTitleEntry(languageName, block, value, request);
			if (addPath[2] == "odd")
				odd = true;
			// OMMFactory.saveOMM(omm, false);
			Map.Entry<?, ?> entry = Tools.getTitleEntry(languageName, block);
			System.out.println("key: " + entry.getKey() + " value: "
					+ entry.getValue());
			System.out.println(OMM_HTML_Converter.convertToHtml(entry, odd));
			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(entry, odd)).getBytes(),
					response, request);
			break;
		case "Contributors":
			LOGGER.debug("add a Contributor!");
			entitytype = _query.get("entitytype").get(0);
			value = _query.get("value").get(0);
			date = _query.get("date").get(0);
			time = _query.get("time").get(0);

			System.out.println(value + entitytype + date + time);
			DateFormat format = null;
			date = date.trim();
			time = time.trim();
			date = date.replaceAll("-", "/");
			System.out.println("new date: " + date);
			if (date.split("/")[2].length() == 4) {
				date = date.substring(0, date.length() - 4)
						+ date.substring(date.length() - 2);
				System.out.print(date);
			}
			if (time.split(":").length == 3) {
				time = time.substring(0, time.length() - 6)
						+ time.substring(time.length() - 3);
			}

			if (time.endsWith("AM") || time.endsWith("PM"))
				format = new SimpleDateFormat("MM/dd/yy hh:mm aa");
			else
				format = new SimpleDateFormat("MM/dd/yy hh:mm");
			Date tempdate = null;
			try {

				tempdate = format.parse(date + " " + time);
			} catch (ParseException e1) {
				
				date = date.substring(5) + "/" + date.substring(2, 4);
				System.out.println(date);
				// e1.printStackTrace();
				try {
					tempdate = format.parse(date + " " + time);
					break;
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
			}
			System.out.println(tempdate);
			String iso8601time = ISO8601.getISO8601String(tempdate);

			// String iso8601time = date.trim() + "T" + time.trim() + "+02:00";
			OMMEntity newContributor = Tools.addContributorsEntry(entitytype,
					block, value, iso8601time);
			if (addPath[2] == "odd")
				odd = true;
			// OMMFactory.saveOMM(omm, false);
			LOGGER.debug(OMM_HTML_Converter.convertToHtml(newContributor));
			Tools.transferBufferWithCompression(OMM_HTML_Converter
					.convertToHtml(newContributor).getBytes(), response,
					request);

			break;
		case "Description":
			LOGGER.debug("add a description");
			languageName = _query.get("multilangkey").get(0);
			value = _query.get("value").get(0);

			if (addPath[2] == "odd")
				odd = true;

			Map.Entry<?, ?> des = Tools.addDescriptionEntry(languageName,
					block, value, request);

			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(des, odd)).getBytes(),
					response, request);
			// OMMFactory.saveOMM(omm, false);
			break;

		case "Subject":
			LOGGER.debug("add a subject");
			String subjecttype = _query.get("subjecttype").get(0);
			List<String> valuechain = _query.get("value");
			OMMSubjectTag newst = Tools.addSubjectEntry(block, subjecttype,
					valuechain, request);
			Tools.transferBufferWithCompression(
					(OMM_HTML_Converter.convertToHtml(newst)).getBytes(),
					response, request);
			// OMMFactory.saveOMM(omm, false);
			break;
		case "Type":
			LOGGER.debug("add a Type!");
			value = _query.get("value").get(0);
			try {
				URL type = Tools.addTypeEntry(block, value, request);
				Tools.transferBufferWithCompression(
						HTMLData.getKeyValueAsTable("Format",
								OMM_HTML_Converter.convertToHtml(type) + "",
								(odd ? "odd" : "even"), block.getID(), "",
								EnumSet.of(EditStatus.Remove)).getBytes(),
						response, request);
				// OMMFactory.saveOMM(omm, false);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		case "Link":
			value = _query.get("value").get(0);
			LOGGER.debug("add a Link: " + value);
			try {
				// addLinkEntry(block, value, odd);
				Tools.transferBufferWithCompression(
						addLinkEntry(block, value, odd).getBytes(), response,
						request);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
}
