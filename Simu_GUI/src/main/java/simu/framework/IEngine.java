package simu.framework;

/**
 * Interface for controlling the simulation engine.
 * Used by the controller to interact with the engine.
 * 
 * @author Group 8
 * @version 1.0
 */
public interface IEngine { // NEW
	/**
	 * Sets the simulation end time.
	 * 
	 * @param time The simulation time when the simulation should stop
	 */
	public void setSimulationTime(double time);
	
	/**
	 * Sets the delay between simulation steps.
	 * 
	 * @param time Delay in milliseconds
	 */
	public void setDelay(long time);
	
	/**
	 * Gets the current delay between simulation steps.
	 * 
	 * @return Delay in milliseconds
	 */
	public long getDelay();
	
	/**
	 * Pauses the simulation.
	 */
	public void pause();
	
	/**
	 * Resumes a paused simulation.
	 */
	public void resumeSimulation();
	
	/**
	 * Checks if the simulation is currently paused.
	 * 
	 * @return true if paused, false otherwise
	 */
	public boolean isPaused();
}
