package de.dfki.adom.test;

import java.util.Random;

import de.dfki.adom.rest.API;

/** Simulator for rising temperatures. */
public class TemperatureSimulator implements Runnable { // TODO This class is not used anywhere
	
	private API target;
	private int temp = 30;
	private int temp2 = 30;
	private Random rng = new Random();
	
	/** Constructor.
	 * @param newTarget The {@link API} to which temperature changes are posted. 
	 */
	public TemperatureSimulator(API newTarget) {
		this.target = newTarget;
	}
	
	/** Increases two temperature values and posts the updated values to the {@link API}. */
	@Override
	public void run() {
		this.temp 	+= rng.nextInt(10);
		this.temp2 	+= rng.nextInt(10);
		this.target.postPayload("hello", Integer.toString(temp));
		this.target.postPayload("5", Integer.toString(temp2));
	}
}
