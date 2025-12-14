package simu.framework;

/**
 * Singleton class representing the simulation clock.
 * Maintains the current simulation time and provides methods to get, set, and reset it.
 * 
 * @author Group 8
 * @version 1.0
 */
public class Clock {
	/** Current simulation time */
	private double time;
	/** Singleton instance of the clock */
	private static Clock instance;
	
	/**
	 * Private constructor for singleton pattern.
	 * Initializes time to 0.
	 */
	private Clock(){
		time = 0;
	}
	
	/**
	 * Gets the singleton instance of the Clock.
	 * Creates a new instance if one doesn't exist.
	 * 
	 * @return The singleton Clock instance
	 */
	public static Clock getInstance(){
		if (instance == null){
			instance = new Clock();
		}
		return instance;
	}
	
	/**
	 * Sets the current simulation time.
	 * 
	 * @param time The new simulation time
	 */
	public void setTime(double time){
		this.time = time;
	}

	/**
	 * Gets the current simulation time.
	 * 
	 * @return The current simulation time
	 */
	public double getTime(){
		return time;
	}
	
	/**
	 * Resets the simulation time to 0.
	 * Used when starting a new simulation.
	 */
	public void reset(){
		this.time = 0;
	}
}
