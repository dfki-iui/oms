package de.dfki.oms.main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.dfki.adom.ssdp.SSDP;
import de.dfki.adom.ssdp.SSDPPackageReceivedInterface;
import de.dfki.adom.ssdp.SSDPPackageType;
import de.dfki.oms.history.OMMVersionManager;

/** Provides an Universal Plug and Play handler to broadcast memories via SSDP. */
public class SSDP_OMS_Handler 
{
	
	/** Adds a handler to a server address.
	 * @param hostName URL of the object memory server (default: localhost).
	 * @param adomePort Port of the ADOMe (default: 10082). 
	 */
	public static void addHandler(final String hostName, final int adomePort)
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
                    for(String memoryName : OMMVersionManager.getAvailableMemories())
                    {
                    	sendResponse(hostName, adomePort, memoryName, source, port);
                    }
                } 
			}
		});
	}	
	
	/** Creates and sends a response to a search request via SSDP. The response contains the OMM namespace, the full address of an object memory and the current time, among other things. 
	 *  
	 * @param hostName URL of the object memory server (default: localhost).
	 * @param adomePort Port of the ADOMe (default: 10082).
	 * @param memoryName Name of the object memory whose server address is to be given.
	 * @param source Source of the request (thus target of the response). 
	 * @param sourcePort Source port of the request (thus target port of the response). 
	 */
	private static void sendResponse(String hostName, int adomePort, String memoryName, InetAddress source, int sourcePort)
	{
		SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        
        String response = "HTTP/1.1 200 OK\r\n" +
                "CACHE-CONTROL: max-age = 30\r\n" +
                "DATE: "+dt.format(new Date())+"\r\n" +
                "EXT:\r\n" +
                "LOCATION: "+hostName+":"+adomePort+"/rest/"+memoryName+"\r\n" +
                "SERVER: Windows/6.1 UPnP/1.1 ADOMe/1.0\r\n" +
                "ST: "+SSDP.OMM_NAMESPACE+"\r\n" + //ssdp:all		 
                "USN: uuid:76203660-ebd9-11e3-ac10-0800200c9a66::upnp:rootdevice\r\n" +
                "BOOTID.UPNP.ORG: 1\r\n" +
                //"CONFIGID.UPNP.ORG: 1\r\n" +
                //"SEARCHPORT.UPNP.ORG: number identifies port on which device responds to unicast M-SEARCH\r\n" +
                "\r\n";


        try {
            byte[] message = response.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(message, message.length, source, sourcePort);

            // Create a datagram socket, send the packet through it, close it.
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
