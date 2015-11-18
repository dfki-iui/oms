package de.dfki.oms.webapp.handler;

/*
 Mon Feb  6 13:42:25 CET 2012 Haiyang Xu haiyang.xu@dfki.de

 OMS Web Interface Request Parameter Specification
 This document gives request example of each operation. 

 Click on Header
 -	Expand Header:	  	GET /oms/sample/st?part=header
 -	Collapse Header:	GET /oms/sample/st?part=empty_header

 Edit in Header
 -	Edit Primary ID:	POST /oms/sample/st?cmd=edit&part=header!entry_Primary%20ID!entry_url!hash_4ba68cb91b1d254e653c6d2e5fb85950e6ef5777b7c6ff2f3bc8f24c3599fde5!odd&value=http%3A%2F%2Fwww.w3.org%2F2005%2FIncubator%2Fomm%2Fsamples%2Fp1
 -	Edit Additional Blk:POST /oms/sample/st?cmd=edit&part=header!entry_AdditionalBlocks!entry_omm_http!hash_8b15fb6aa3726d71c340c0892c7d9d392ef4126f4e1c5fe2464b1f5cad2eade7!odd&value=http%3A%2F%2Fwww.w3.org%2F2005%2FIncubator%2Fomm%2Fsamples%2Fp1%2Fext

 Click on a Block
 -	Expand a Block:		GET /oms/sample/st?part=block_1
 -	Collapse a Block:	GET /oms/sample/st?part=empty_block_1

 Edit in a Block
 -	Delete an entire Block:
 GET /oms/sample/st?cmd=remove&part=block_1!

 -	Delete a meta function: (caution: Title, Namespace, Creator and Type cannot be deleted)
 GET /oms/sample/st?cmd=remove&part=block_2!Contributors_2!

 -	Delete an entry:
 GET /oms/sample/st?cmd=remove&part=block_1!Title_1!entry_French!hash_670d5c7ef50fe0095d2a330a25aab7e8189ae05c906b4b57d5073d42981dd7c7!
 GET /oms/sample/st?cmd=remove&part=block_1!Contributors_1!entry_email!hash_7f59aa8201d53b80f42802c83ee2782029957f8455da8ad913443e97b351a5b3!

 -	Modify an entry:
 POST /oms/sample/st?cmd=edit&part=block_1!Title_1!entry_English!hash_684da0445de200b912c0abec282b262e1e0f0f8e7df8fef7816e2b6f059622f9!odd&value=sample%20title
 POST /oms/sample/st?cmd=edit&part=block_1!Contributors_1!entry_email!hash_7f59aa8201d53b80f42802c83ee2782029957f8455da8ad913443e97b351a5b3!odd&entitytype=email&value=user%40dfki.de&time=6%3A12%20AM&date=1%2F31%2F11

 -	Modify a meta function:
 POST /oms/sample/st?cmd=edit&part=block_1!Type_1!hash_6100225ee3f24e0be3435f6a7c6c2afd3c7fdb7c465fb99e64ac0a1127c7595a!odd&value=http%3A%2F%2Fpurl.org%2Fdc%2Fdcmitype%2FDataset

 -	Add an entry:
 POST /oms/sample/st?cmd=add&part=block_1!Title_1!odd&multilangkey=Belarusian&value=example

 -	Add a meta function:
 POST /oms/sample/st?cmd=add&part=block_1!Subject_1!odd&subjecttype=Text&value=haha

 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Map;

import de.dfki.oms.webapp.handler.impl.AddBlockHandler;
import de.dfki.oms.webapp.handler.impl.AddHandler;
import de.dfki.oms.webapp.handler.impl.BlockFormatHandler;
import de.dfki.oms.webapp.handler.impl.BlockHandler;
import de.dfki.oms.webapp.handler.impl.ContributorEntityTypeHandler;
import de.dfki.oms.webapp.handler.impl.DesMultiLangHandler;
import de.dfki.oms.webapp.handler.impl.EditHandler;
import de.dfki.oms.webapp.handler.impl.EmptyBlockHandler;
import de.dfki.oms.webapp.handler.impl.EmptyHeaderHandler;
import de.dfki.oms.webapp.handler.impl.HeaderHandler;
import de.dfki.oms.webapp.handler.impl.MineTypeHandler;
import de.dfki.oms.webapp.handler.impl.OMMTypeHandler;
import de.dfki.oms.webapp.handler.impl.PayloadIdInfoKeysHandler;
import de.dfki.oms.webapp.handler.impl.PayloadRelationHandler;
import de.dfki.oms.webapp.handler.impl.PayloadTypeHandler;
import de.dfki.oms.webapp.handler.impl.RemoveHandler;
import de.dfki.oms.webapp.handler.impl.SubjectTypeHandler;
import de.dfki.oms.webapp.handler.impl.SubjectValueHandler;
import de.dfki.oms.webapp.handler.impl.TitleMultiLangHandler;
import de.dfki.oms.webapp.handler.impl.UpdateHandler;

/** Holds a list of all handlers and dispatches them to respond to requests as necessary.  */
public class QueryCommandDispatcher {

	private static Map<String, QueryCommandHandler> Handlers = new Hashtable<String, QueryCommandHandler>();

	/** Retrieves the specific handler responsible for a request.  
	 * 
	 * @param partArg The request's query's "part" argument from which a handler is selected.
	 * @return The responsible {@link QueryCommandHandler}. 
	 * @throws UnsupportedEncodingException
	 */
	public static QueryCommandHandler GetHandler(final String partArg) // TODO unnecessary encapsulation?
			throws UnsupportedEncodingException {
		return getHandler(partArg);
	}

	/** Retrieves the specific handler responsible for a request.  
	 * 
	 * @param partArg The request's query's "part" argument from which a handler is selected.
	 * @return The responsible {@link QueryCommandHandler}. 
	 * @throws UnsupportedEncodingException
	 */
	private static QueryCommandHandler getHandler(String partArg)
			throws UnsupportedEncodingException {	
		if (partArg.equals("header")) {
			if (Handlers.get("HeaderHandler") == null) {
				Handlers.put("HeaderHandler", new HeaderHandler());
			}
			return Handlers.get("HeaderHandler");
		} else if (partArg.equals("empty_header")) {
			if (Handlers.get("EmptyHeaderHandler") == null) {
				Handlers.put("EmptyHeaderHandler", new EmptyHeaderHandler());
			}
			return Handlers.get("EmptyHeaderHandler");
		} else if (partArg.equals("add_new_block")) {
			System.out.println("Add a new Block!");
			if (Handlers.get("AddBlockHandler") == null) {
				Handlers.put("AddBlockHandler", new AddBlockHandler());
			}
			return Handlers.get("AddBlockHandler");
		} else if (partArg.startsWith("block")) {
			String blockName = partArg.replace("block_", "");
			blockName = URLDecoder.decode(blockName, "UTF-8");
			if (Handlers.get("BlockHandler") == null) {

				Handlers.put("BlockHandler", new BlockHandler());
			}
			((BlockHandler) (Handlers.get("BlockHandler")))
					.setBlockName(blockName);
			return Handlers.get("BlockHandler");
		} else if (partArg.startsWith("empty_block")) {
			String blockName = partArg.replace("empty_block_", "");
			blockName = URLDecoder.decode(blockName, "UTF-8");
			if (Handlers.get("EmptyBlockHandler") == null) {

				Handlers.put("EmptyBlockHandler", new EmptyBlockHandler());
			}
			((EmptyBlockHandler) (Handlers.get("EmptyBlockHandler")))
					.setBlockName(blockName);
			return Handlers.get("EmptyBlockHandler");
		} else if (partArg.startsWith("remove")) {
			if (Handlers.get("RemoveHandler") == null) {

				Handlers.put("RemoveHandler", new RemoveHandler());
			}
			return Handlers.get("RemoveHandler");
		} else if (partArg.startsWith("edit")) {
			if (Handlers.get("EditHandler") == null) {
				Handlers.put("EditHandler", new EditHandler());
			}
			return Handlers.get("EditHandler");
		} else if (partArg.startsWith("add")) {
			if (Handlers.get("AddHandler") == null) {
				Handlers.put("AddHandler", new AddHandler());
			}
			return Handlers.get("AddHandler");
		} else if (partArg.startsWith("titlemultilang")) {
			if (Handlers.get("TitleMultiLangHandler") == null) {
				Handlers.put("TitleMultiLangHandler",
						new TitleMultiLangHandler());
			}
			return Handlers.get("TitleMultiLangHandler");
		} else if (partArg.startsWith("desmultilang")) {
			if (Handlers.get("DesMultiLangHandler") == null) {
				Handlers.put("DesMultiLangHandler", new DesMultiLangHandler());
			}
			return Handlers.get("DesMultiLangHandler");
		} else if (partArg.startsWith("contributorEntityType")) {
			if (Handlers.get("ContributorEntityTypeHandler") == null) {
				Handlers.put("ContributorEntityTypeHandler",
						new ContributorEntityTypeHandler());
			}
			return Handlers.get("ContributorEntityTypeHandler");
		} else if (partArg.startsWith("mimetype")) {
			if (Handlers.get("MimeTypeHandler") == null) {
				Handlers.put("MimeTypeHandler", new MineTypeHandler());
			}
			return Handlers.get("MimeTypeHandler");
		} else if (partArg.startsWith("payloadtype")) {
			if (Handlers.get("PayloadTypeHandler") == null) {
				Handlers.put("PayloadTypeHandler", new PayloadTypeHandler());
			}
			return Handlers.get("PayloadTypeHandler");
		} else if (partArg.startsWith("ommtype")) {
			if (Handlers.get("OMMTypeHandler") == null) {
				Handlers.put("OMMTypeHandler", new OMMTypeHandler());
			}
			return Handlers.get("OMMTypeHandler");
		} else if (partArg.startsWith("payloadidinfokeys")) {
			if (Handlers.get("PayloadIdInfoKeysHandler") == null) {
				Handlers.put("PayloadIdInfoKeysHandler",
						new PayloadIdInfoKeysHandler());
			}
			return Handlers.get("PayloadIdInfoKeysHandler");
		} else if (partArg.startsWith("payloadrelation")) {
			if (Handlers.get("PayloadRelationHandler") == null) {
				Handlers.put("PayloadRelationHandler",
						new PayloadRelationHandler());
			}
			return Handlers.get("PayloadRelationHandler");
		} else if (partArg.startsWith("subjecttype")) {
			if (Handlers.get("SubjectTypeHandler") == null) {
				Handlers.put("SubjectTypeHandler", new SubjectTypeHandler());
			}
			return Handlers.get("SubjectTypeHandler");
		} else if (partArg.startsWith("getblockformat")) {
			if (Handlers.get("BlockFormatHandler") == null) {
				Handlers.put("BlockFormatHandler", new BlockFormatHandler());
			}
			return Handlers.get("BlockFormatHandler");
		} else if (partArg.startsWith("subjectvalue")) {
			if (Handlers.get("SubjectValueHandler") == null) {
				Handlers.put("SubjectValueHandler", new SubjectValueHandler());
			}
			return Handlers.get("SubjectValueHandler");
		} else if (partArg.startsWith("update")) {
			if (Handlers.get("UpdateHandler") == null) {
				Handlers.put("UpdateHandler", new UpdateHandler());
			}
			return Handlers.get("UpdateHandler");
		}

		else
			return null;
	}
}
