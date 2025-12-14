package simu.model;

public class SimulationStatistics {
    /** Throughput metric (customers per hour) */
    protected int throughput;
    /** Average wait time in seconds */
    protected double avgWaitTime;
    /** Peak queue length observed during simulation */
    protected int peakQueueLength;
    /** Current simulation time in seconds */
    protected double currentTime;

    /** Total number of customers served */
    private int customersServed;
    /** Total wait time accumulated across all customers */
    private double totalWaitTime;

    /**
     * Constructs a new SimulationStatistics object with the specified values.
     *
     * @param customersServed Total number of customers served
     * @param throughput Throughput in customers per hour
     * @param avgWaitTime Average wait time in seconds
     * @param peakQueueLength Peak queue length observed
     * @param currentTime Current simulation time in seconds
     */

    protected int customersRejected;

    public SimulationStatistics(int customersServed, double throughput, double avgWaitTime, int peakQueueLength, double currentTime) {
        this.customersServed = customersServed;
        this.avgWaitTime = avgWaitTime;
        this.peakQueueLength = peakQueueLength;
        this.currentTime = currentTime;
        this.customersRejected = 0;
    }

    public SimulationStatistics(int customersServed, double throughput, double avgWaitTime, int peakQueueLength, double currentTime, int customersRejected) {
        this.customersServed = customersServed;
        this.avgWaitTime = avgWaitTime;
        this.peakQueueLength = peakQueueLength;
        this.currentTime = currentTime;
        this.customersRejected = customersRejected;
    }

    /**
     * Records a customer being served and updates statistics.
     *
     * @param exitTime Time when customer exited the system
     * @param arrivalTime Time when customer arrived
     */
    public void customerServed(double exitTime, double arrivalTime) {
        customersServed++;
        totalWaitTime += (exitTime - arrivalTime);
    }

    /**
     * Gets the total number of customers served.
     *
     * @return Number of customers served
     */
    public int getCustomersServed() {
        return customersServed;
    }

    /**
     * Calculates and returns the average wait time.
     *
     * @return Average wait time in seconds, or 0 if no customers served
     */
    public double getAverageWait() {
        return customersServed > 0
                ? totalWaitTime / customersServed
                : 0;
    }

    public int getCustomersRejected() {
        return customersRejected;
    }
}
