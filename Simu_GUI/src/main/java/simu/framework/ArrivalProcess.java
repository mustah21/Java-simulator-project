package simu.framework;
import eduni.distributions.*;
import simu.model.EventType;

/**
 * Generates customer arrival events based on an inter-arrival time distribution.
 * 
 * @author Group 8
 * @version 1.0
 */
public class ArrivalProcess {
	/** Generator for inter-arrival times */
	private ContinuousGenerator generator;
	/** Event list where arrival events are scheduled */
	private EventList eventList;
	/** Type of arrival event to create */
	private EventType type;

	/**
	 * Constructs a new ArrivalProcess with the specified generator and event list.
	 * 
	 * @param g The continuous generator for inter-arrival times
	 * @param tl The event list where arrival events will be scheduled
	 * @param type The type of arrival event to create
	 */
	public ArrivalProcess(ContinuousGenerator g, EventList tl, EventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

	/**
	 * Generates the next arrival event.
	 * Samples an inter-arrival time from the generator and schedules
	 * an arrival event at the current time plus the inter-arrival time.
	 */
	public void generateNext() {
		Event t = new Event(type, Clock.getInstance().getTime() + generator.sample());
		eventList.add(t);
	}

}
