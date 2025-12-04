package simu.framework;

import controller.IControllerMtoV;
import simu.model.ServicePoint;

public abstract class Engine extends Thread implements IEngine {  // NEW DEFINITIONS
	private double simulationTime = 0;	// time when the simulation will be stopped
	private long delay = 0;
	private Clock clock;				// in order to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
	private volatile boolean paused = false;  // Pause flag
	
	protected EventList eventList;
	protected ServicePoint[] servicePoints;
	protected IControllerMtoV controller; // NEW

	public Engine(IControllerMtoV controller) {	// NEW
		this.controller = controller;  			// NEW
		clock = Clock.getInstance();
		eventList = new EventList();
		/* Service Points are created in simu.model-package's class who is inheriting the Engine class */
	}

	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}
	
	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}
	
	@Override // NEW
	public long getDelay() {
		return delay;
	}
	
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
	
	@Override
	public void pause() {
		paused = true;
	}
	
	@Override
	public void resumeSimulation() {
		paused = false;
	}
	
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

	private void tryCEvents() {    // define protected, if you want to overwrite
		for (ServicePoint p: servicePoints){
			if (!p.isReserved() && p.isOnQueue()){
				p.beginService();
			}
		}
	}

	private double currentTime(){
		return eventList.getNextTime();
	}
	
	private boolean simulate() {
		Trace.out(Trace.Level.INFO, "Time is: " + clock.getTime());
		return clock.getTime() < simulationTime;
	}

	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected abstract void initialization(); 	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void runEvent(Event t);	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void results(); 			// Defined in simu.model-package's class who is inheriting the Engine class
	protected void updateDisplays() {			// Override in subclasses to update UI displays
		// Default implementation does nothing
	}
}