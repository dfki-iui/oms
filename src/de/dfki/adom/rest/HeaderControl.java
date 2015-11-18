package de.dfki.adom.rest;

import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/** Responsible for adding access control information to the HTTP header of a response. */
public class HeaderControl {
	
	private static final Header m_AccessControlAllowOrigin = new Header("Access-Control-Allow-Origin", "*");

	public static void AddAccessControlAllowOrigin(Response response)
	{
		@SuppressWarnings("unchecked")
		Series<Header> responseHeaders = (Series<Header>)response.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
		
		if (responseHeaders == null) 
		{
			responseHeaders = new Series<Header>(Header.class);
			response.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, responseHeaders);
		}
		responseHeaders.add(m_AccessControlAllowOrigin);
	}	
}
