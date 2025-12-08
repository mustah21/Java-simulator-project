package simu.model;

public class SimulationStatistics {
    protected int throughput;
    protected double avgWaitTime;
    protected int peakQueueLength;
    protected double currentTime;


    private int customersServed;
    private double totalWaitTime;


    public SimulationStatistics(int customersServed, double throughput, double avgWaitTime, int peakQueueLength, double currentTime) {
        this.customersServed = customersServed;
        this.avgWaitTime = avgWaitTime;
        this.peakQueueLength = peakQueueLength;
        this.currentTime = currentTime;
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
}
