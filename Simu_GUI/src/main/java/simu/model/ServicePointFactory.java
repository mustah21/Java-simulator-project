package simu.model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.FixedTimeGenerator;
import eduni.distributions.Normal;
import simu.framework.EventList;

/**
 * Factory class for creating service points in the cafeteria simulation.
 * Provides methods to create all service points with appropriate configurations
 * and helper methods for routing decisions.
 * 
 * @author Group 8
 * @version 1.0
 */
public class ServicePointFactory {

    /** Index for grill station in service points array */
    public static final int GRILL_STATION = 0;
    /** Index for vegan station in service points array */
    public static final int VEGAN_STATION = 1;
    /** Index for normal station in service points array */
    public static final int NORMAL_STATION = 2;
    /** Index for first cashier station in service points array */
    public static final int CASHIER_STATION = 3;
    /** Index for second cashier station in service points array */
    public static final int CASHIER_STATION_2 = 6;
    /** Index for self-service station in service points array */
    public static final int SELF_SERVICE_STATION = 4;
    /** Index for coffee station in service points array */
    public static final int COFFEE_STATION = 5;

    /**
     * Creates all service points for the simulation with the specified parameters.
     * 
     * @param grillTime Mean service time for grill station (seconds)
     * @param veganTime Mean service time for vegan station (seconds)
     * @param normalTime Mean service time for normal station (seconds)
     * @param cashierTime Mean service time for cashier stations (seconds)
     * @param selfServiceTime Mean service time for self-service station (seconds)
     * @param coffeeTime Mean service time for coffee station (seconds)
     * @param variabilityEnabled Whether to use normal distribution (true) or fixed time (false)
     * @param selfServiceEnabled Whether self-service station should be enabled
     * @param coffeeEnabled Whether coffee station should be enabled
     * @param eventList The event list for scheduling departure events
     * @return Array of ServicePoint objects in the order defined by the station constants
     */
    public static ServicePoint[] createServicePoints(
            double grillTime, double veganTime, double normalTime,
            double cashierTime, double selfServiceTime, double coffeeTime,
            boolean variabilityEnabled, boolean selfServiceEnabled, boolean coffeeEnabled,
            EventList eventList) {

        ServicePoint[] servicePoints = new ServicePoint[7];
        System.out.println("self-service enabled? " + selfServiceEnabled);
        servicePoints[GRILL_STATION] = new ServicePoint(
                createGenerator(grillTime, variabilityEnabled),
                eventList, EventType.MEAL_GRILL_DEP, "Grill Station");

        servicePoints[VEGAN_STATION] = new ServicePoint(
                createGenerator(veganTime, variabilityEnabled),
                eventList, EventType.MEAL_VEGAN_DEP, "Vegan Station");

        servicePoints[NORMAL_STATION] = new ServicePoint(
                createGenerator(normalTime, variabilityEnabled),
                eventList, EventType.MEAL_NORMAL_DEP, "Normal Station");

        servicePoints[CASHIER_STATION] = new ServicePoint(
                createGenerator(cashierTime, variabilityEnabled),
                eventList, EventType.PAYMENT_CASHIER_DEP, "Cashier");

        servicePoints[CASHIER_STATION_2] = new ServicePoint(
                createGenerator(cashierTime, variabilityEnabled),
                eventList, EventType.PAYMENT_CASHIER_DEP, "Cashier");


        servicePoints[SELF_SERVICE_STATION] = new ServicePoint(
                createGenerator(selfServiceTime, variabilityEnabled),
                eventList, EventType.PAYMENT_SELF_DEP, "Self-Service");
        servicePoints[SELF_SERVICE_STATION].setEnabled(selfServiceEnabled);

        servicePoints[COFFEE_STATION] = new ServicePoint(
                createGenerator(coffeeTime, variabilityEnabled),
                eventList, EventType.COFFEE_DEP, "Coffee Station");
        servicePoints[COFFEE_STATION].setEnabled(coffeeEnabled);

        return servicePoints;
    }

    /**
     * Creates a service time generator based on the variability setting.
     * 
     * @param meanTime Mean service time in seconds
     * @param variabilityEnabled If true, uses Normal distribution with 10% standard deviation;
     *                          if false, uses FixedTimeGenerator
     * @return A ContinuousGenerator for service times
     */
    private static ContinuousGenerator createGenerator(double meanTime, boolean variabilityEnabled) {
        if (variabilityEnabled) {
            return new Normal(meanTime, meanTime * 0.1);
        } else {
            return new FixedTimeGenerator(meanTime);
        }
    }

    /**
     * Chooses the best payment station based on queue lengths.
     * Prefers self-service if enabled and has shorter queue, otherwise uses cashier.
     * 
     * @param servicePoints Array of all service points
     * @return The ServicePoint to use for payment (self-service or cashier)
     */
    public static ServicePoint choosePaymentStation(ServicePoint[] servicePoints) {
        ServicePoint cashier = servicePoints[CASHIER_STATION];
        ServicePoint selfService = servicePoints[SELF_SERVICE_STATION];

        if (!selfService.isEnabled()) {
            return cashier;
        }

        if (selfService.getQueueLength() < cashier.getQueueLength()) {
            return selfService;
        } else {
            return cashier;
        }
    }

    /**
     * Determines if a customer should visit the coffee station.
     * 
     * @param servicePoints Array of all service points
     * @param customerWantsCoffee Whether the customer wants coffee
     * @return true if customer should visit coffee station (enabled and wants coffee), false otherwise
     */
    public static boolean shouldVisitCoffeeStation(ServicePoint[] servicePoints, boolean customerWantsCoffee) {
        if (!servicePoints[COFFEE_STATION].isEnabled()) {
            return false;
        }
        return customerWantsCoffee;
    }
}

