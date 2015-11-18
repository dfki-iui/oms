package de.dfki.adom.client;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/** Using a set timer, activates all registered {@link Client}s to run regularly. 
 * 
 * @author Christian Hauck
 * @organization: DFKI
 *
 */
public class HeartBeat extends TimerTask {

	Timer             m_timer  = null;
	ArrayList<Client> m_clientList = null;
	

	/** Constructor. 
	 * @param timer The {@link Timer} to use to determine when clients are to be activated. 
	 */
	public HeartBeat(Timer timer) {
		m_timer      = timer;
		m_clientList = new ArrayList<Client>();
	}

	@Override
	public void run() {
		for (Client client : m_clientList) client.run();
//		m_timer.cancel();
	}
	
	/** Adds a client that is to be activated regularly. 
	 * @param client The new {@link Client}.
	 * @return True, if client has been added successfully. 
	 */
	public boolean addClient(Client client) {
		boolean ok = m_clientList.add(client);
		return ok;
	}
	
	/** Removes a client from the notification list. 
	 * @param client The {@link Client} to remove.
	 * @return True, if client has been removed successfully. 
	 */
	public boolean removeClient(Client client) {
		return m_clientList.remove(client);
	}
}
