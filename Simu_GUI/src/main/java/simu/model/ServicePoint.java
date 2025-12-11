package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

/**
 * Represents a service point in the cafeteria simulation.
 * A service point manages a queue of customers and processes them using
 * a service time generator. Examples include meal stations, payment stations, and coffee station.
 * 
 * @author Group 8
 * @version 1.0
 */
public class ServicePoint {
    /** Queue of customers waiting for service (FIFO) */
    private LinkedList<Customer> jono = new LinkedList<Customer>(); // Data Structure used
    /** Generator for service time distribution */
    private ContinuousGenerator generator;
    /** Event list for scheduling departure events */
    private EventList eventList;
    /** Type of event to schedule when service completes */
    private EventType eventTypeScheduled;
    /** Flag indicating if the service point is currently serving a customer */
    private boolean reserved = false;
    /** Flag indicating if the service point is enabled (can accept customers) */
    private boolean enabled = true;
    /** Name of the service point for identification */
    private String name;

    /**
     * Constructs a new ServicePoint with the specified parameters.
     * 
     * @param generator The service time generator
     * @param tapahtumalista The event list for scheduling events
     * @param tyyppi The event type to schedule when service completes
     */
    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
        this.eventList = tapahtumalista;
        this.generator = generator;
        this.eventTypeScheduled = tyyppi;
        this.enabled = true;
    }

    /**
     * Constructs a new ServicePoint with the specified parameters and name.
     * 
     * @param generator The service time generator
     * @param tapahtumalista The event list for scheduling events
     * @param tyyppi The event type to schedule when service completes
     * @param name The name of the service point
     */
    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi, String name){
        this(generator, tapahtumalista, tyyppi);
        this.name = name;
    }

    /**
     * Adds a customer to the service point queue.
     * Throws an exception if the service point is disabled.
     * 
     * @param a The customer to add to the queue
     * @throws IllegalStateException if the service point is disabled
     */
    public void addQueue(Customer a){   // First customer at the queue is always on the service
        if (!enabled) {
            throw new IllegalStateException("Cannot add customer to disabled service point: " + name);
        }
        jono.add(a);
    }

    /**
     * Removes and returns the customer at the front of the queue (who just completed service).
     * Also clears the reserved flag.
     * 
     * @return The customer who completed service, or null if queue is empty
     */
    public Customer removeQueue(){		// Remove serviced customer
        reserved = false;
        return jono.poll();
    }

    /**
     * Begins service for the customer at the front of the queue.
     * Generates a service time and schedules a departure event.
     * Does nothing if the service point is disabled or already reserved.
     */
    public void beginService() {  		// Begins a new service, customer is on the queue during the service
        if (!enabled) {
            return;
        }
        reserved = true;
        double serviceTime = generator.sample();
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
    }

    /**
     * Checks if the service point is currently reserved (serving a customer).
     * 
     * @return true if reserved, false otherwise
     */
    public boolean isReserved(){
        return reserved;
    }

    /**
     * Checks if there are customers in the queue.
     * 
     * @return true if queue is not empty, false otherwise
     */
    public boolean isOnQueue(){
        return jono.size() != 0;
    }

    /**
     * Checks if the service point is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the service point is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the current queue length (number of customers waiting or being served).
     * 
     * @return The number of customers in the queue
     */
    public int getQueueLength() {
        return jono.size();
    }

    /**
     * Gets the name of the service point.
     * 
     * @return The service point name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the service point.
     * 
     * @param name The new name for the service point
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the service point has available queue capacity.
     * 
     * @param maxCapacity Maximum allowed queue capacity (Integer.MAX_VALUE means unlimited)
     * @return true if queue has capacity, false if at maximum capacity
     */
    public boolean hasQueueCapacity(int maxCapacity) {
        if (maxCapacity == Integer.MAX_VALUE) {
            return true;
        }
        return jono.size() < maxCapacity;
    }
}
