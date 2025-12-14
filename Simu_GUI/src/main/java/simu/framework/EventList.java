package simu.framework;

import java.util.PriorityQueue;

/**
 * Manages the list of scheduled events in the simulation.
 * Uses a priority queue to maintain events ordered by time.
 * 
 * @author Group 8
 * @version 1.0
 */
public class EventList {
	/** Priority queue storing events ordered by time */
	private PriorityQueue<Event> lista = new PriorityQueue<Event>();
	
	/**
	 * Constructs a new empty EventList.
	 */
	public EventList() {
	}
	
	/**
	 * Removes and returns the next event (earliest time).
	 * 
	 * @return The next event to process
	 */
	public Event remove(){
		return lista.remove();
	}
	
	/**
	 * Adds an event to the event list.
	 * 
	 * @param t The event to add
	 */
	public void add(Event t){
		lista.add(t);
	}
	
	/**
	 * Gets the time of the next event without removing it.
	 * 
	 * @return The simulation time of the next event
	 */
	public double getNextTime(){
		return lista.peek().getTime();
	}
	
	
}
