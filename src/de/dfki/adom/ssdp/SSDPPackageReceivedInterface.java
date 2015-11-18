package de.dfki.adom.ssdp;

import java.net.InetAddress;
import java.util.HashMap;

/** Provides a method to handle the reception of an SSDP package. */
public interface SSDPPackageReceivedInterface {

	/**
	 * Handles the reception of an SSDP package.
	 * 
	 * @param source The {@link InetAddress} from which the package came. 
	 * @param port Port number used by the package.
	 * @param type The {@link SSDPPackageType} of the package. 
	 * @param payload A {@link HashMap} of attributes and their values contained in the package. 
	 */
	public void SSDPPackageReceived(InetAddress source, int port, SSDPPackageType type, HashMap<String, String> payload);
	
}
