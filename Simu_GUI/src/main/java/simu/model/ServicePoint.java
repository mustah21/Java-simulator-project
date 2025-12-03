package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
    private LinkedList<Customer> jono = new LinkedList<Customer>(); // Data Structure used
    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    //Queuestrategy strategy; // option: ordering of the customer
    private boolean reserved = false;
    private boolean enabled = true;
    private String name;

    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
        this.eventList = tapahtumalista;
        this.generator = generator;
        this.eventTypeScheduled = tyyppi;
        this.enabled = true;
    }

    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi, String name){
        this(generator, tapahtumalista, tyyppi);
        this.name = name;
    }

    public void addQueue(Customer a){   // First customer at the queue is always on the service
        if (!enabled) {
            throw new IllegalStateException("Cannot add customer to disabled service point: " + name);
        }
        jono.add(a);
    }

    public Customer removeQueue(){		// Remove serviced customer
        reserved = false;
        return jono.poll();
    }

    public void beginService() {  		// Begins a new service, customer is on the queue during the service
        if (!enabled) {
            return;
        }
        reserved = true;
        double serviceTime = generator.sample();
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
    }

    public boolean isReserved(){
        return reserved;
    }

    public boolean isOnQueue(){
        return jono.size() != 0;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getQueueLength() {
        return jono.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasQueueCapacity(int maxCapacity) {
        if (maxCapacity == Integer.MAX_VALUE) {
            return true;
        }
        return jono.size() < maxCapacity;
    }
}
