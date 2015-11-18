package de.dfki.oms.adom;

import org.restlet.security.LocalVerifier;

public class UsernamePasswordVerifier extends LocalVerifier  // TODO This class is not used anywhere
{
	@Override
	public char[] getLocalSecret(String identifier) 
	{
		return null; // Also, it just denies access.
	}

}
