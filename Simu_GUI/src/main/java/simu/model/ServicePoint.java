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
    
    private int totalCustomersServed = 0;
    private double totalBusyTime = 0.0;
    private double lastServiceStartTime = -1.0;
    private double simulationStartTime = 0.0;
    
    private double totalQueueLengthTime = 0.0;
    private double lastQueueLengthChangeTime = 0.0;
    private int lastQueueLength = 0;
    private int peakQueueLength = 0;
    
    private double totalWaitTime = 0.0;
    private double totalServiceTime = 0.0;
    
    private java.util.Map<Customer, Double> customerArrivalTimes = new java.util.HashMap<>();

    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
        this.eventList = tapahtumalista;
        this.generator = generator;
        this.eventTypeScheduled = tyyppi;
        this.enabled = true;
        this.simulationStartTime = Clock.getInstance().getTime();
        this.lastQueueLengthChangeTime = simulationStartTime;
    }

    public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi, String name){
        this(generator, tapahtumalista, tyyppi);
        this.name = name;
    }

    public void addQueue(Customer a){   // First customer at the queue is always on the service
        if (!enabled) {
            throw new IllegalStateException("Cannot add customer to disabled service point: " + name);
        }
        double currentTime = Clock.getInstance().getTime();
        
        updateQueueLengthStatistics(currentTime);
        
        customerArrivalTimes.put(a, currentTime);
        
        jono.add(a);
        
        if (jono.size() > peakQueueLength) {
            peakQueueLength = jono.size();
        }
    }

    public Customer removeQueue(){		// Remove serviced customer
        double currentTime = Clock.getInstance().getTime();
        
        if (lastServiceStartTime >= 0) {
            totalBusyTime += (currentTime - lastServiceStartTime);
            lastServiceStartTime = -1.0;
        }
        
        updateQueueLengthStatistics(currentTime);
        
        Customer customer = jono.poll();
        
        if (customer != null) {
            totalCustomersServed++;
            customerArrivalTimes.remove(customer);
        }
        
        reserved = false;
        return customer;
    }

    public void beginService() {  		// Begins a new service, customer is on the queue during the service
        if (!enabled) {
            return;
        }
        double currentTime = Clock.getInstance().getTime();
        
        lastServiceStartTime = currentTime;
        
        reserved = true;
        double serviceTime = generator.sample();
        
        totalServiceTime += serviceTime;
        
        Customer customerBeingServed = jono.peek();
        if (customerBeingServed != null) {
            Double arrivalTime = customerArrivalTimes.get(customerBeingServed);
            if (arrivalTime != null) {
                double waitTime = currentTime - arrivalTime;
                totalWaitTime += waitTime;
            }
        }
        
        eventList.add(new Event(eventTypeScheduled, currentTime + serviceTime));
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
    
    private void updateQueueLengthStatistics(double currentTime) {
        if (lastQueueLengthChangeTime >= 0) {
            double timeDelta = currentTime - lastQueueLengthChangeTime;
            totalQueueLengthTime += lastQueueLength * timeDelta;
        }
        lastQueueLength = jono.size();
        lastQueueLengthChangeTime = currentTime;
    }
    
    public void finalizeStatistics() {
        double currentTime = Clock.getInstance().getTime();
        updateQueueLengthStatistics(currentTime);
        
        if (lastServiceStartTime >= 0) {
            totalBusyTime += (currentTime - lastServiceStartTime);
            lastServiceStartTime = -1.0;
        }
    }
    
    public int getTotalCustomersServed() {
        return totalCustomersServed;
    }
    
    public double getTotalBusyTime() {
        return totalBusyTime;
    }
    
    public double getUtilization(double simulationTime) {
        if (simulationTime <= 0) return 0.0;
        return (totalBusyTime / simulationTime) * 100.0;
    }
    
    public double getAverageQueueLength(double simulationTime) {
        if (simulationTime <= 0) return 0.0;
        return totalQueueLengthTime / simulationTime;
    }
    
    public int getPeakQueueLength() {
        return peakQueueLength;
    }
    
    public double getAverageWaitTime() {
        if (totalCustomersServed == 0) return 0.0;
        return totalWaitTime / totalCustomersServed;
    }
    
    public double getAverageServiceTime() {
        if (totalCustomersServed == 0) return 0.0;
        return totalServiceTime / totalCustomersServed;
    }
    
    public void resetStatistics() {
        totalCustomersServed = 0;
        totalBusyTime = 0.0;
        lastServiceStartTime = -1.0;
        simulationStartTime = Clock.getInstance().getTime();
        totalQueueLengthTime = 0.0;
        lastQueueLengthChangeTime = simulationStartTime;
        lastQueueLength = 0;
        peakQueueLength = 0;
        totalWaitTime = 0.0;
        totalServiceTime = 0.0;
        customerArrivalTimes.clear();
    }
}
