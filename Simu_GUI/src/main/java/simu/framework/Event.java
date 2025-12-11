package simu.framework;

/**
 * Represents an event in the discrete event simulation.
 * Events are scheduled at specific times and processed by the engine.
 * Implements Comparable to allow ordering by time in priority queues.
 * 
 * @author Group 8
 * @version 1.0
 */
public class Event implements Comparable<Event> {
	/** Type of event (e.g., arrival, departure) */
	private IEventType type;
	/** Simulation time when this event should occur */
	private double time;
	
	/**
	 * Constructs a new Event with the specified type and time.
	 * 
	 * @param type The type of event
	 * @param time The simulation time when the event should occur
	 */
	public Event(IEventType type, double time) {
		this.type = type;
		this.time = time;
	}
	
	/**
	 * Sets the event type.
	 * 
	 * @param type The new event type
	 */
	public void setType(IEventType type) {
		this.type = type;
	}
	
	/**
	 * Gets the event type.
	 * 
	 * @return The event type
	 */
	public IEventType getType() {
		return type;
	}
	
	/**
	 * Sets the event time.
	 * 
	 * @param time The simulation time when the event should occur
	 */
	public void setTime(double time) {
		this.time = time;
	}
	
	/**
	 * Gets the event time.
	 * 
	 * @return The simulation time when the event should occur
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Compares this event to another event by time.
	 * Used for ordering events in priority queues.
	 * 
	 * @param arg The event to compare to
	 * @return Negative if this event occurs earlier, positive if later, 0 if same time
	 */
	@Override
	public int compareTo(Event arg) {
		if (this.time < arg.time) return -1;
		else if (this.time > arg.time) return 1;
		return 0;
	}
}
