package de.dfki.adom.ssdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.SystemUtils;


/** Handler for receiving and answering SSDP packages sent to the ADOM. */
public class SSDP_ADOME_Handler { // TODO This class is not used anywhere
	
	/** Adds an SSDP package handler. 
	 * 
	 * @param hostName The URL of the ADOM.
	 * @param adomePort The port of the ADOM.
	 * @param memoryName The name of the memory. 
	 */
	public static void addHandler(final String hostName, final int adomePort, final String memoryName)
	{
		SSDP.addHandler(new SSDPPackageReceivedInterface() {
			
			/**
			 * Handles the reception of an SSDP search request (ignores other packages).
			 */
			@Override
			public void SSDPPackageReceived(InetAddress source, int port, SSDPPackageType type, HashMap<String, String> payload) 
			{
				if (type == SSDPPackageType.M_SEARCH) {                    

                    if (!payload.containsKey("ST")) return; // "ST" denotes the search target in an M-SEARCH header
                    
                    String st = payload.get("ST");
                    if (!SSDP.OMM_NAMESPACE.equals(st)) return;

                    
                    System.out.println("M-SEARCH from " + source + ":" + port + " --> Sending RESPONSE...");
                    
                    
                    SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                    
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "CACHE-CONTROL: max-age = 30\r\n" +
                            "DATE: "+dt.format(new Date())+"\r\n" +
                            "EXT:\r\n" +
                            "LOCATION: http://"+hostName+":"+adomePort+"/"+memoryName+"\r\n" +
                            "SERVER: "+getWindowsVersionString()+" UPnP/1.1 ADOMe/1.0\r\n" +
                            "ST: "+SSDP.OMM_NAMESPACE+"\r\n" + //ssdp:all		 
                            "USN: uuid:"+SSDP.OMM_UUID+"::upnp:rootdevice\r\n" +
                            "BOOTID.UPNP.ORG: 1\r\n" +
                            //"CONFIGID.UPNP.ORG: 1\r\n" +
                            //"SEARCHPORT.UPNP.ORG: number identifies port on which device responds to unicast M-SEARCH\r\n" +
                            "\r\n";
                    
                    try {
                        byte[] message = response.getBytes("UTF-8");
                        DatagramPacket packet = new DatagramPacket(message, message.length, source, port);

                        // Create a datagram socket, send the packet through it, close it.
                        DatagramSocket dsocket = new DatagramSocket();
                        dsocket.send(packet);
                        dsocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } 
			}
		});
	}
	
	/** Retrieves an OS description.
	 * @return A String describing the current operating system (and in case of Windows its version).
	 */
	public static String getWindowsVersionString()
	{
		String WINDOWS = "Windows/";
		
		if (SystemUtils.IS_OS_WINDOWS_8) return WINDOWS + "6.2"; 
		else if (SystemUtils.IS_OS_WINDOWS_7) return WINDOWS + "6.1";
		else if (SystemUtils.IS_OS_WINDOWS_VISTA) return WINDOWS + "6.0";
		else if (SystemUtils.IS_OS_WINDOWS_XP) return WINDOWS + "5.1";
		else if (SystemUtils.IS_OS_LINUX) return "LINUX";
		else if (SystemUtils.IS_OS_UNIX) return "UNIX";
		
		return "";
	}
}
