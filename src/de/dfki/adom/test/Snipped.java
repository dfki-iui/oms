package de.dfki.adom.test;

import de.dfki.adom.rest.Tools;

/** Provides the contents of various test snippet files.
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
@Deprecated
public class Snipped {

	/** The file path to the snippet files. */
	private static String Path = "resources/test/";
	
	private static String BlockSnipped 			= "block.snp";
	private static String ContributionSnipped 	= "contribution.snp";
	private static String CreationSnipped 		= "creation.snp";
	private static String SubjectSnipped 		= "subject.snp";
	private static String LinkSnipped 			= "link.snp";
	private static String PayloadSnipped 		= "payload.snp";
	
	/** Empty constructor. */
	public Snipped() {
	}

	/** Retrieves the content of the block snippet file. 
	 * @return Block snippet as String. 
	 */
	public static String block() {
		return Tools.Read(Path, BlockSnipped);
	}
	
	/** Retrieves the content of the contribution snippet file. 
	 * @return Contribution snippet as String. 
	 */
	public static String contribution() {
		return Tools.Read(Path, ContributionSnipped);
	}
	
	/** Retrieves the content of the creation snippet file. 
	 * @return Creation snippet as String. 
	 */
	public static String creation() {
		return Tools.Read(Path, CreationSnipped);
	}
	
	/** Retrieves the content of the subject snippet file. 
	 * @return Subject snippet as String. 
	 */
	public static String subject() {
		return Tools.Read(Path, SubjectSnipped);
	}
	
	/** Retrieves the content of the link snippet file. 
	 * @return Link snippet as String. 
	 */
	public static String link() {
		return Tools.Read(Path, LinkSnipped);
	}
	
	/** Retrieves the content of the payload snippet file. 
	 * @return Payload snippet as String. 
	 */
	public static String payload() {
		return Tools.Read(Path, PayloadSnipped);
	}
}
