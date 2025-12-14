package simu.framework;

import controller.IControllerMtoV;
import simu.model.ServicePoint;

/**
 * Abstract base class for discrete event simulation engines.
 * Implements the main simulation loop and provides hooks for subclasses
 * to implement specific event handling logic.
 * 
 * @author Group 8
 * @version 1.0
 */
public abstract class Engine extends Thread implements IEngine {  // NEW DEFINITIONS
	/** Time when the simulation will be stopped */
	private double simulationTime = 0;	// time when the simulation will be stopped
	/** Delay between simulation steps in milliseconds */
	private long delay = 0;
	/** Reference to the simulation clock */
	private Clock clock;				// in order to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
	/** Flag indicating if the simulation is paused */
	private volatile boolean paused = false;  // Pause flag
	
	/** List of scheduled events */
	protected EventList eventList;
	/** Array of service points in the simulation */
	protected ServicePoint[] servicePoints;
	/** Controller interface for model-to-view communication */
	protected IControllerMtoV controller; // NEW

	/**
	 * Constructs a new Engine instance.
	 * 
	 * @param controller The controller interface for model-to-view communication
	 */
	public Engine(IControllerMtoV controller) {	// NEW
		this.controller = controller;  			// NEW
		clock = Clock.getInstance();
		eventList = new EventList();
		/* Service Points are created in simu.model-package's class who is inheriting the Engine class */
	}

	/**
	 * Sets the simulation end time.
	 * 
	 * @param time The simulation time when the simulation should stop
	 */
	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}
	
	/**
	 * Sets the delay between simulation steps.
	 * 
	 * @param time Delay in milliseconds
	 */
	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}
	
	/**
	 * Gets the current delay between simulation steps.
	 * 
	 * @return Delay in milliseconds
	 */
	@Override // NEW
	public long getDelay() {
		return delay;
	}
	
	/**
	 * Main simulation loop.
	 * Runs initialization, then processes events until simulation time is reached
	 * or the thread is interrupted.
	 */
	@Override
	public void run() {
		initialization(); // creating, e.g., the first event

		while (simulate() && !Thread.currentThread().isInterrupted()){
			// Wait if paused
			while (paused && !Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(100); // Check pause state every 100ms
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			
			delay(); // NEW
			clock.setTime(currentTime());
			runBEvents();
			tryCEvents();
			updateDisplays(); // Update UI displays after each simulation step
		}

		results();
	}
	
	/**
	 * Pauses the simulation.
	 */
	@Override
	public void pause() {
		paused = true;
	}
	
	/**
	 * Resumes a paused simulation.
	 */
	@Override
	public void resumeSimulation() {
		paused = false;
	}
	
	/**
	 * Checks if the simulation is currently paused.
	 * 
	 * @return true if paused, false otherwise
	 */
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Processes all B-phase events scheduled for the current simulation time.
	 * B-phase events are events that occur at a specific time (e.g., arrivals, departures).
	 */
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

	/**
	 * Processes C-phase events (conditional events).
	 * Checks all service points and starts service for any that are not reserved
	 * but have customers waiting.
	 */
	private void tryCEvents() {    // define protected, if you want to overwrite
		for (ServicePoint p: servicePoints){
			if (!p.isReserved() && p.isOnQueue()){
				p.beginService();
			}
		}
	}

	/**
	 * Gets the time of the next scheduled event.
	 * 
	 * @return The simulation time of the next event
	 */
	private double currentTime(){
		return eventList.getNextTime();
	}
	
	/**
	 * Checks if the simulation should continue running.
	 * 
	 * @return true if current time is less than simulation end time, false otherwise
	 */
	private boolean simulate() {
		Trace.out(Trace.Level.INFO, "Time is: " + clock.getTime());
		return clock.getTime() < simulationTime;
	}

	/**
	 * Introduces a delay between simulation steps to control simulation speed.
	 * Uses Thread.sleep() to pause execution.
	 */
	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the simulation.
	 * Subclasses must implement this to set up initial events (e.g., first arrival).
	 */
	protected abstract void initialization(); 	// Defined in simu.model-package's class who is inheriting the Engine class
	
	/**
	 * Processes a single event.
	 * Subclasses must implement this to handle specific event types.
	 * 
	 * @param t The event to process
	 */
	protected abstract void runEvent(Event t);	// Defined in simu.model-package's class who is inheriting the Engine class
	
	/**
	 * Called when the simulation completes.
	 * Subclasses should implement this to generate final results and reports.
	 */
	protected abstract void results(); 			// Defined in simu.model-package's class who is inheriting the Engine class
	
	/**
	 * Updates UI displays during simulation.
	 * Can be overridden in subclasses to update visualizations and statistics.
	 */
	protected void updateDisplays() {			// Override in subclasses to update UI displays
		// Default implementation does nothing
	}
}