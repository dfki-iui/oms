package de.dfki.adom.ssdp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;

/** Responsible for the SSDP communication of an ADOM. */
public class SSDP
{
	
    /** The namespace of the OMM: {@value}. */
    public static final String OMM_NAMESPACE = "urn:adome:rest:ver:1";
    /** The Universally Unique Identifier of the OMM: {@value}. */
    public static final String OMM_UUID = "76203660-ebd9-11e3-ac10-0800200c9a66";

    /** A pre-composed SSDP discovery message requesting answers from all listening devices. */
	private final static String DISCOVER_MESSAGE_ROOTDEVICE =
		    "M-SEARCH * HTTP/1.1\r\n" +
		    "HOST: 239.255.255.250:1900\r\n" +
		    "MAN: \"ssdp:discover\"\r\n" +
		    "MX: 3\r\n" +
		    "ST: "+OMM_NAMESPACE+"\r\n" + //ssdp:all
		    //"USER-AGENT: Windows/6.1 UPnP/1.1 ADOMe/1.0\r\n" +
		    "\r\n";

    private static InetAddress multicastAddress;
	private static final int port = 1900; // standard port for SSDP
	private static MulticastSocket m_multicastSocket = null;
	private static MulticastSocket m_sendSocket = null;
	private static HashSet<SSDPPackageReceivedInterface> m_handler = new HashSet<SSDPPackageReceivedInterface>();
    private static Thread unicastListener = null, multicastListener = null;

	static 
	{
		try
		{
			multicastAddress = InetAddress.getByName("239.255.255.250");
		}
		catch(Exception _) {}
	}
	
	/** Adds a handler to the set of handlers that process received packages. If the handler is already in the set, it is not added. 
	 * @param handler The {@link SSDPPackageReceivedInterface} to add. 
	 */
	public static void addHandler(SSDPPackageReceivedInterface handler)
	{
		if (!m_handler.contains(handler)) m_handler.add(handler);
	}
	
	/** Removes a handler from the set of handlers that process received packages (if it is in it). 
	 * @param handler The {@link SSDPPackageReceivedInterface} to remove. 
	 */
	public static void removeHandler(SSDPPackageReceivedInterface handler)
	{
		if (m_handler.contains(handler)) m_handler.remove(handler);
	}
	
	/** Retrieves the multicast socket, creating a new one if it does not exist.
	 * @return The {@link MulticastSocket}. 
	 * @throws IOException
	 */
	private static MulticastSocket getMulticastSocket() throws IOException
	{		 
		if (m_multicastSocket == null)
		{
			m_multicastSocket = new MulticastSocket(port); 
			m_multicastSocket.setReuseAddress(true);
	      	//socket.setSoTimeout(15000);
			m_multicastSocket.joinGroup(multicastAddress);
		}
      	return m_multicastSocket;
	}
	
	/** Retrieves the multicast socket used to send messages, creating a new one if it does not exist.
	 * @return The {@link MulticastSocket} used to send messages. 
	 * @throws IOException
	 */
	private static MulticastSocket getSendSocket() throws IOException
	{		 
		if (m_sendSocket == null)
		{
			m_sendSocket = new MulticastSocket();
            m_sendSocket.setReuseAddress(true);
		}
      	return m_sendSocket;
	}
	
	/** Sends the {@link #DISCOVER_MESSAGE_ROOTDEVICE} to the multicast address in order to find all other UPnP capable devices.
	 * @throws IOException
	 */
	public static void sendDiscoveryMessage() throws IOException
	{		
		// send discover
        byte[] txbuf = DISCOVER_MESSAGE_ROOTDEVICE.getBytes("UTF-8");
        DatagramPacket hi = new DatagramPacket(txbuf, txbuf.length, multicastAddress, port);
        getSendSocket().send(hi);
        System.out.println("SSDP discover sent");
	}

    /** Sends the {@link #DISCOVER_MESSAGE_ROOTDEVICE} asynchronically in order to find all other UPnP capable devices.
     * @param delayInMilliSeconds Milliseconds to wait before sending the discovery message. 
     */
    public static void sendDiscoveryMessageAsync(final int delayInMilliSeconds)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run() { try { Thread.sleep(delayInMilliSeconds); sendDiscoveryMessage(); } catch(Exception e){} }
        }).start();
    }

	/** Listens to messages sent via the multicast address. Received messages are parsed and sent to all handlers.
	 * @throws IOException
	 */
	public static void listenToMulticastResponses() throws IOException
	{
		MulticastSocket socket = getMulticastSocket();
		System.out.println("Listening to multicast responses...");
        
		try
        {        	
	        do 
	        {
	        	System.out.println("Reading multicast...");
	            byte[] rxbuf = new byte[8192];
	            DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
	            socket.receive(packet);
	            dumpPacket(packet, true);
	          
	        } while (true); // should leave loop by SocketTimeoutException
        } catch (SocketTimeoutException e) {
          System.out.println("Timeout");
        }
	}
	
	/** Starts a new Thread that listens to messages sent via the multicast address. Received messages are parsed and sent to all handlers. */
	public static void listenToMulticastResponsesAsync()
    {
        if (multicastListener == null) {
        	multicastListener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                    	listenToMulticastResponses();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        	multicastListener.start();
        }
    }
	
	/** Listens to messages sent via the uniicast address. Received messages are parsed and sent to all handlers.
	 * @throws IOException
	 */
	public static void listenToUnicastResponses() throws IOException
	{
		DatagramSocket socket = getSendSocket();//new DatagramSocket(getSendSocket().getLocalPort());
		System.out.println("Listening to unicast responses on port '"+getSendSocket().getLocalPort()+"'...");
        
		try
        {        	
	        do 
	        {
	        	System.out.println("Reading unicast...");
	            byte[] rxbuf = new byte[8192];
	            DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
	            socket.receive(packet);
	            dumpPacket(packet, false);
	          
	        } while (true); // should leave loop by SocketTimeoutException
        } catch (SocketTimeoutException e) {
          System.out.println("Timeout");
        }
	}

	/** Starts a new Thread that listens to messages sent via the unicast address. Received messages are parsed and sent to all handlers. */
    public static void listenToUnicastResponsesAsync()
    {
        if (unicastListener == null) {
            unicastListener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        listenToUnicastResponses();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            unicastListener.start();
        }
    }

	/** Disassembles a received SSDP package into its entries, so they can be parsed and sent to all handlers. 
	 * 
	 * @param packet The {@link DatagramPacket}. 
	 * @param multicast True, if the message was received via multicast. 
	 * @throws IOException
	 */
	private static void dumpPacket(DatagramPacket packet, boolean multicast) throws IOException {
	    InetAddress addr = packet.getAddress();
	    System.out.println("Response from (via "+(multicast ? "multicast" : "unicast")+"): " + addr);
	    ByteArrayInputStream in = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    
	    HashMap<String, String> lines = new HashMap<String, String>();
	    String line = null;
	    boolean header = true;
	    while((line = br.readLine()) != null)
	    {
	    	if (header) 
	    	{
	    		lines.put("HEADER", line);
	    		header = false;	    		
	    	}
	    	else
	    	{
	    		int pos = line.indexOf(":");
	    		if (pos < 0) continue;
		    	lines.put(line.substring(0, pos).trim(), line.substring(pos + 1, line.length()).trim());
	    	}	    	
	    }
	    parseInput(lines, addr, packet.getPort());
	    
	    /*in.reset();	    
	    copyStream(in, System.out);*/
	  }
	
	/** Parses a disassembled SSDP package and sends its contents to all handlers.
	 * 
	 * @param lines The packages's content disassembled in lines.
	 * @param addr The {@link InetAddress} from which the package came. 
	 * @param port Port number used by the package.
	 */
	private static void parseInput(HashMap<String, String> lines, InetAddress addr, int port)
	{
		SSDPPackageType type = SSDPPackageType.DEFAULT;
		if (lines.containsKey("HEADER") && lines.get("HEADER").startsWith("M-SEARCH * HTTP/1.1"))
		{
			lines.remove("HEADER");
			type = SSDPPackageType.M_SEARCH;
		}
		else if (lines.containsKey("HEADER") && lines.get("HEADER").startsWith("NOTIFY * HTTP/1.1"))
		{
			lines.remove("HEADER");
			type = SSDPPackageType.NOTIFY;
		}
		sendToHandler(addr, port, type, lines);
	}
	
	/** Passes a received SSDP package to every known handler ({@link SSDPPackageReceivedInterface}s). 
	 * 
	 * @param source The {@link InetAddress} from which the package came. 
	 * @param port Port number used by the package.
	 * @param type The {@link SSDPPackageType} of the package. 
	 * @param lines A {@link HashMap} of attributes and their values contained in the package. 
	 */
	private static void sendToHandler(InetAddress source, int port, SSDPPackageType type, HashMap<String, String> lines)
	{
		for(SSDPPackageReceivedInterface interf : m_handler)
		{
			interf.SSDPPackageReceived(source, port, type, lines);
		}
	}

//	  private static void copyStream(InputStream in, OutputStream out) throws IOException {
//	    BufferedInputStream bin = new BufferedInputStream(in);
//	    BufferedOutputStream bout = new BufferedOutputStream(out);
//	    int c = bin.read();
//	    while (c != -1) {
//	      out.write((char) c);
//	      c = bin.read();
//	    }
//	    bout.flush();
//	  }
	
}
