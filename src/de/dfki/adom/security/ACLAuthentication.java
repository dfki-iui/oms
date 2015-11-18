package de.dfki.adom.security;

import java.util.Vector;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

/** Responsible for OMS security measurements on the REST interface, such as verifying access, authentication and authorization. */
public abstract class ACLAuthentication {
	
	/** A list of alternative addresses of the OMS. */
	public static Vector<String> serverUrls = new Vector<String>();
	
	/**
	 * Creates a new challenge authenticator for a given context, places it before a Restlet
	 * and sets it to use the specified Verifier.
	 * 
	 * @param context The {@link Context} for the challenge authenticator.
	 * @param next The {@link Restlet} to be placed after the challenge authenticator.
	 * @param verifier The {@link Verifier} to use for authentication.
	 * 
	 * @return The new {@link ChallengeAuthenticator} as a {@link Restlet}.
	 */
	public static Restlet getAuthenticator(Context context, Restlet next, Verifier verifier)
	{
		return next; // no authentication, just reroute to next node
	}
	
	/**
	 * Creates a new verifier to be used with the ACL security mode's challenge authenticator.
	 * 
	 * @return The {@link Verifier}.
	 */
	public static Verifier getVerifier()
	{
		// dummy verifier accepting any request
		return new Verifier() {  
			@Override
			public int verify(Request request, Response response) {
				return RESULT_VALID;
			}
		};
	}	

}
