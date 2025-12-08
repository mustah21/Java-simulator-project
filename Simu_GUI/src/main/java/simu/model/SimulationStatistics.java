package simu.model;

public class SimulationStatistics {
    protected int throughput;
    protected double avgWaitTime;
    protected int peakQueueLength;
    protected double currentTime;


    private int customersServed;
    private double totalWaitTime;
    private int rejectedCustomers;


    public SimulationStatistics(int customersServed, double throughput, double avgWaitTime, int peakQueueLength, double currentTime, int rejectedCustomers) {
        this.customersServed = customersServed;
        this.avgWaitTime = avgWaitTime;
        this.peakQueueLength = peakQueueLength;
        this.currentTime = currentTime;
        this.rejectedCustomers = rejectedCustomers;
    }

    public void customerServed(double exitTime, double arrivalTime) {
        customersServed++;
        totalWaitTime += (exitTime - arrivalTime);
    }

    public int getCustomersServed() {
        return customersServed;
    }

    public double getAverageWait() {
        return customersServed > 0
                ? totalWaitTime / customersServed
                : 0;
    }

    public int getRejectedCustomers() {
        return rejectedCustomers;
    }

    public double getCurrentTime() {
        return currentTime;
    }
}
