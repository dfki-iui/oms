package de.dfki.adom.rest;


/** An API which is observable, providing methods to add and remove {@link APIObserver}s to watch over it. */
public interface APIObservable { // TODO de.dfki.adom.rest.API uses this interface but no Observers
	
	/** Adds an observer to an observable API that can notified about changes.
	 * @param obs The {@link APIObserver} to watch over the API. 
	 */
	public void addObserver(APIObserver obs);
	
	/** Removes a given observer from an API.
	 * @param obs The {@link APIObserver} to remove from the API. 
	 */
	public void removeObserver(APIObserver obs);
}
