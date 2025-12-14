package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

import java.io.IOException;

/**
 * Main simulation engine for the cafeteria simulation.
 * Extends the base Engine class and implements the specific event handling
 * logic for customer arrivals, meal preparation, payment processing, and coffee service.
 *
 * @author Group 8
 * @version 1.0
 */
public class MyEngine extends Engine {
    private ArrivalProcess arrivalProcess;

    // Service points are stored in the protected servicePoints array from Engine
    // Individual references for easier access
    private ServicePoint grillStation;
    private ServicePoint veganStation;
    private ServicePoint normalStation;
    private ServicePoint cashierStation;
    private ServicePoint cashierStation2;
    private ServicePoint selfServiceStation;
    private ServicePoint coffeeStation;

    /** Flag indicating if the simulation is paused */
    private volatile boolean paused = false;
    /** Lock object for synchronizing pause/resume operations */
    private final Object pauseLock = new Object();

    /** Maximum queue capacity for service points */
    private int maxQueueCapacity;
    /** Flag indicating if new customer arrivals are temporarily stopped */
    private boolean arrivalsStopped = false;

    // Statistics tracking
    /** Total number of customers served during the simulation */
    private int customersServed = 0;
    /** Total wait time accumulated across all customers */
    private double totalWaitTime = 0.0;
    /** Peak queue length observed during the simulation */
    private int peakQueueLength = 0;
    private int customersRejected = 0;

    /**
     * Constructs a new MyEngine instance with the specified simulation parameters.
     *
     * @param controller The controller interface for model-to-view communication
     * @param grillTime Mean service time for grill station (seconds)
     * @param veganTime Mean service time for vegan station (seconds)
     * @param normalTime Mean service time for normal station (seconds)
     * @param cashierTime Mean service time for cashier station (seconds)
     * @param selfServiceTime Mean service time for self-service station (seconds)
     * @param coffeeTime Mean service time for coffee station (seconds)
     * @param variabilityEnabled Whether to enable service time variability (normal distribution vs fixed)
     * @param selfServiceEnabled Whether the self-service station is enabled
     * @param coffeeEnabled Whether the coffee station is enabled
     * @param arrivalRate Customer arrival rate (students per hour)
     * @param maxQueueCapacity Maximum queue capacity for service points
     */
    public MyEngine(IControllerMtoV controller,
                    double grillTime, double veganTime, double normalTime,
                    double cashierTime, double selfServiceTime, double coffeeTime,
                    boolean variabilityEnabled, boolean selfServiceEnabled, boolean coffeeEnabled,
                    double arrivalRate, int maxQueueCapacity) {
        super(controller);



        this.maxQueueCapacity = maxQueueCapacity;

        // Create all service points through the factory with user-provided values
        servicePoints = ServicePointFactory.createServicePoints(
                grillTime, veganTime, normalTime,
                cashierTime, selfServiceTime, coffeeTime,
                variabilityEnabled,
                selfServiceEnabled,
                coffeeEnabled,
                eventList
        );

        grillStation = servicePoints[ServicePointFactory.GRILL_STATION];
        veganStation = servicePoints[ServicePointFactory.VEGAN_STATION];
        normalStation = servicePoints[ServicePointFactory.NORMAL_STATION];
        cashierStation = servicePoints[ServicePointFactory.CASHIER_STATION];
        cashierStation2 = servicePoints[ServicePointFactory.CASHIER_STATION_2];
        selfServiceStation = servicePoints[ServicePointFactory.SELF_SERVICE_STATION];
        coffeeStation = servicePoints[ServicePointFactory.COFFEE_STATION];

        // Convert arrival rate (students/hour) to inter-arrival time (seconds)
        // If arrivalRate is students/hour, mean inter-arrival time = 3600/arrivalRate seconds
        // Example: 120 students/hour = 3600/120 = 30 seconds between arrivals
        // Negexp constructor: Negexp(mean, seed) - mean is in seconds
        double meanInterArrivalTime = arrivalRate > 0 ? 3600.0 / arrivalRate : 30.0;
        arrivalProcess = new ArrivalProcess(new Negexp(meanInterArrivalTime, 5), eventList, EventType.ARR1);
    }

    /**
     * Initializes the simulation by generating the first customer arrival event.
     * This method is called once at the start of the simulation.
     */
    @Override
    protected void initialization() {
        arrivalProcess.generateNext();     // First arrival in the system
    }

    /**
     * Helper method to introduce a small delay in the simulation thread.
     * Used to prevent the simulation from running too fast and to allow
     * UI updates to be processed.
     */
    private void helperSleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Processes simulation events (B-phase events).
     * Handles all event types including customer arrivals, meal departures,
     * payment completions, and coffee service completions.
     *
     * @param t The event to process
     */
    @Override
    protected void runEvent(Event t) {  // B phase events
        switch ((EventType) t.getType()) {
            case ARR1: {
                Customer c = new Customer();
                MealType mealType = c.getMealType();
                helperSleep();

                // Check if the target station has capacity before adding customer
                ServicePoint targetStation = switch (mealType) {
                    case GRILL -> grillStation;
                    case VEGAN -> veganStation;
                    case NORMAL -> normalStation;
                };

                // Only add customer if the target station has capacity
                if (targetStation != null && targetStation.hasQueueCapacity(maxQueueCapacity)) {
                    double arrivalTime = Clock.getInstance().getTime();
                    targetStation.addQueue(c);

                    c.markServiceStart(ServicePointType.MEAL, arrivalTime);

                    if (!targetStation.isReserved() && targetStation.isOnQueue()) {
                        targetStation.beginService();
                    }

                    controller.visualiseCustomer(mealType);
                    arrivalsStopped = false;
                } else {
                    customersRejected++;
                }

                // Check if all first-row SPs are at max capacity
                boolean allFull = !grillStation.hasQueueCapacity(maxQueueCapacity) &&
                                 !veganStation.hasQueueCapacity(maxQueueCapacity) &&
                                 !normalStation.hasQueueCapacity(maxQueueCapacity);

                // Only generate next arrival if not all stations are full
                if (!allFull) {
                    arrivalProcess.generateNext();
                    arrivalsStopped = false;
                } else {
                    arrivalsStopped = true;
                }

                updateQueueDisplays();
                break;
            }

            case MEAL_GRILL_DEP: {
                if(!shouldSendToPayment())
                    return;
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = grillStation.removeQueue();

                c.markServiceEnd(ServicePointType.MEAL, serviceEndTime);

                if (grillStation.isOnQueue() && !grillStation.isReserved()) {
                    grillStation.beginService();
                }

                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
                if (arrivalsStopped && grillStation.hasQueueCapacity(maxQueueCapacity)) {
                    checkAndResumeArrivals();
                }
                updateQueueDisplays();
                break;
            }
            case MEAL_VEGAN_DEP: {
                if(!shouldSendToPayment())
                    return;
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = veganStation.removeQueue();

                c.markServiceEnd(ServicePointType.MEAL, serviceEndTime);

                if (veganStation.isOnQueue() && !veganStation.isReserved()) {
                    veganStation.beginService();
                }

                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
                // Resume arrivals if they were stopped and now capacity is available
                if (arrivalsStopped && veganStation.hasQueueCapacity(maxQueueCapacity)) {
                    checkAndResumeArrivals();
                }
                updateQueueDisplays();
                break;
            }
            case MEAL_NORMAL_DEP: {
                if(!shouldSendToPayment())
                    return;
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = normalStation.removeQueue();

                c.markServiceEnd(ServicePointType.MEAL, serviceEndTime);

                if (normalStation.isOnQueue() && !normalStation.isReserved()) {
                    normalStation.beginService();
                }

                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
                // Resume arrivals if they were stopped and now capacity is available
                if (arrivalsStopped && normalStation.hasQueueCapacity(maxQueueCapacity)) {
                    checkAndResumeArrivals();
                }
                updateQueueDisplays();
                break;
            }

            case PAYMENT_CASHIER_DEP: {
                Customer c = null;
                int cashierStationNumber = 1;
                ServicePoint completedStation = null;
                double serviceEndTime = Clock.getInstance().getTime();

                if (cashierStation.isReserved() && cashierStation.isOnQueue()) {
                    c = cashierStation.removeQueue();
                    completedStation = cashierStation;
                    cashierStationNumber = 1;
                } else if (cashierStation2.isReserved() && cashierStation2.isOnQueue()) {
                    c = cashierStation2.removeQueue();
                    completedStation = cashierStation2;
                    cashierStationNumber = 2;
                } else if (cashierStation.isOnQueue()) {
                    c = cashierStation.removeQueue();
                    completedStation = cashierStation;
                    cashierStationNumber = 1;
                } else if (cashierStation2.isOnQueue()) {
                    c = cashierStation2.removeQueue();
                    completedStation = cashierStation2;
                    cashierStationNumber = 2;
                }

                if (c != null && completedStation != null) {
                    c.markServiceEnd(ServicePointType.CASHIER, serviceEndTime);

                    if (completedStation.isOnQueue() && !completedStation.isReserved()) {
                        completedStation.beginService();
                    }

                    routeAfterPayment(c, cashierStationNumber);
                }
                updateQueueDisplays();
                break;
            }
            case PAYMENT_SELF_DEP: {
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = selfServiceStation.removeQueue();

                c.markServiceEnd(ServicePointType.SELF_SERVICE, serviceEndTime);

                if (selfServiceStation.isOnQueue() && !selfServiceStation.isReserved()) {
                    selfServiceStation.beginService();
                }

                routeAfterPayment(c, 0);
                updateQueueDisplays();
                break;
            }

            case COFFEE_DEP: {
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = coffeeStation.removeQueue();

                c.markServiceEnd(ServicePointType.COFFEE, serviceEndTime);

                if (coffeeStation.isOnQueue() && !coffeeStation.isReserved()) {
                    coffeeStation.beginService();
                }

                controller.visualiseCustomerExitFromCoffee();
                c.setRemovalTime(Clock.getInstance().getTime());

                // Update statistics
                customersServed++;
                double totalTimeInSystem = c.getRemovalTime() - c.getArrivalTime();
                totalWaitTime += totalTimeInSystem;

                c.reportResults();
                updateQueueDisplays();
                break;
            }
        }

    }
    public boolean shouldSendToPayment(){
        if(cashierStation.getQueueLength() == maxQueueCapacity && cashierStation2.getQueueLength() == maxQueueCapacity && selfServiceStation.getQueueLength() == maxQueueCapacity && coffeeStation.getQueueLength() == maxQueueCapacity) {
            return false;
        }
        return true;
    }

    /**
     * Routes a customer to the appropriate payment station based on their payment type.
     * Customers with self-service preference go to self-service if enabled, otherwise to cashier.
     *
     * @param customer The customer to route
     * @return The cashier station number (0 = self-service, 1 = cashier1, 2 = cashier2)
     */
    protected int routeToPayment(Customer customer) {
        int cashierStationNumber = 0;
        double arrivalTime = Clock.getInstance().getTime();
        ServicePoint paymentStation = null;

        switch (customer.getPaymentType()) {
            case SELF_SERVICE:
                if (selfServiceStation.isEnabled() && selfServiceStation.getQueueLength() < maxQueueCapacity) {
                    selfServiceStation.addQueue(customer);
                    paymentStation = selfServiceStation;
                    cashierStationNumber = 0;
                    customer.markServiceStart(ServicePointType.SELF_SERVICE, arrivalTime);
                    if (!selfServiceStation.isReserved() && selfServiceStation.isOnQueue()) {
                        selfServiceStation.beginService();
                    }
                } else {
                    cashierStationNumber = redirectToCashier(customer);
                    cashierStation.addQueue(customer);
                    paymentStation = cashierStation;
                    cashierStationNumber = 1;
                    customer.markServiceStart(ServicePointType.CASHIER, arrivalTime);
                    if (!cashierStation.isReserved() && cashierStation.isOnQueue()) {
                        cashierStation.beginService();
                    }
                }
                break;
            case CASHIER:
                cashierStationNumber = redirectToCashier(customer);
                paymentStation = (cashierStationNumber == 1) ? cashierStation : cashierStation2;
                customer.markServiceStart(ServicePointType.CASHIER, arrivalTime);
                if (paymentStation != null && !paymentStation.isReserved() && paymentStation.isOnQueue()) {
                    paymentStation.beginService();
                }
                break;
        }

        updateQueueDisplays();
        return cashierStationNumber;
    }
    /**
     * Redirects a customer to the least busy cashier station.
     * Chooses between cashier station 1 and 2 based on queue lengths.
     *
     * @param customer The customer to redirect
     * @return The cashier station number (1 or 2), defaults to 1 if both are full
     */
    protected int redirectToCashier(Customer customer) {
        int queue1 = cashierStation.getQueueLength();
        int queue2 = cashierStation2.getQueueLength();

        ServicePoint targetStation;
        int stationNumber;

        if (queue1 < queue2 && queue1 < maxQueueCapacity) {
            targetStation = cashierStation;
            stationNumber = 1;
        } else if (queue2 < maxQueueCapacity) {
            targetStation = cashierStation2;
            stationNumber = 2;
        } else if (queue1 < maxQueueCapacity) {
            targetStation = cashierStation;
            stationNumber = 1;
        } else {
            targetStation = cashierStation;
            stationNumber = 1;
        }

        targetStation.addQueue(customer);
        if (!targetStation.isReserved() && targetStation.isOnQueue()) {
            targetStation.beginService();
        }
        return stationNumber;
    }

    /**
     * Routes a customer after payment completion.
     * If the customer wants coffee and the coffee station is enabled, routes to coffee station.
     * Otherwise, the customer exits the system.
     *
     * @param customer The customer who completed payment
     * @param cashierStationNumber The cashier station number (0 = self-service, 1 or 2 = cashier)
     */
    private void routeAfterPayment(Customer customer, int cashierStationNumber) {
        if (ServicePointFactory.shouldVisitCoffeeStation(servicePoints, customer.isWantsCoffee())) {
            double arrivalTime = Clock.getInstance().getTime();
            controller.visualiseCustomerToCoffee(customer.getPaymentType(), cashierStationNumber);
            coffeeStation.addQueue(customer);

            customer.markServiceStart(ServicePointType.COFFEE, arrivalTime);

            if (!coffeeStation.isReserved() && coffeeStation.isOnQueue()) {
                coffeeStation.beginService();
            }

            updateQueueDisplays();
        } else {
            controller.visualiseCustomerExitFromPayment(customer.getPaymentType(), cashierStationNumber);
            customer.setRemovalTime(Clock.getInstance().getTime());

            customersServed++;
            double totalTimeInSystem = customer.getRemovalTime() - customer.getArrivalTime();
            totalWaitTime += totalTimeInSystem;

            customer.reportResults();
        }
    }

    /**
     * Checks if arrivals should be resumed after being stopped.
     * Resumes arrivals if at least one first-row service point (grill, vegan, or normal)
     * has available queue capacity.
     */
    private void checkAndResumeArrivals() {
        // Check if at least one first-row SP has capacity
        boolean atLeastOneHasCapacity = grillStation.hasQueueCapacity(maxQueueCapacity) ||
                                       veganStation.hasQueueCapacity(maxQueueCapacity) ||
                                       normalStation.hasQueueCapacity(maxQueueCapacity);

        if (atLeastOneHasCapacity && arrivalsStopped) {
            arrivalsStopped = false;
            arrivalProcess.generateNext(); // Resume generating arrivals
        }
    }

    /**
     * Updates the queue length displays for all service points.
     * Also tracks peak queue length and updates the controller with current queue states.
     */
    private void updateQueueDisplays() {
        int grillQueue = grillStation.getQueueLength();
        int veganQueue = veganStation.getQueueLength();
        int normalQueue = normalStation.getQueueLength();
        int cashierQueue = cashierStation.getQueueLength();
        int cashierQueue2 = cashierStation2.getQueueLength();
        int selfServiceQueue = selfServiceStation.getQueueLength();
        int coffeeQueue = coffeeStation.getQueueLength();

        // Update peak queue length
        int currentMaxQueue = Math.max(Math.max(grillQueue, veganQueue), normalQueue);
        currentMaxQueue = Math.max(Math.max(currentMaxQueue, cashierQueue), cashierQueue2);
        currentMaxQueue = Math.max(Math.max(currentMaxQueue, selfServiceQueue), coffeeQueue);
        if (currentMaxQueue > peakQueueLength) {
            peakQueueLength = currentMaxQueue;
        }

        controller.updateQueueDisplays(grillQueue, veganQueue, normalQueue,
                                      cashierQueue, cashierQueue2, selfServiceQueue, coffeeQueue);

        // Update statistics
        updateStatistics();
    }

    /**
     * Calculates and updates simulation statistics including throughput,
     * average wait time, peak queue length, and current simulation time.
     * Updates the controller with these statistics for display.
     */
    private void updateStatistics() {
        double currentTime = Clock.getInstance().getTime();
        double simulationHours = currentTime / 3600.0;

        double throughput = simulationHours > 0 ? customersServed / simulationHours : 0.0;

        double avgWaitTime = customersServed > 0 ? totalWaitTime / customersServed : 0.0;

        controller.updateStatistics(throughput, avgWaitTime, peakQueueLength, currentTime);

        double[] utilizationPercentages = new double[6];
        utilizationPercentages[0] = grillStation.getUtilization(currentTime);
        utilizationPercentages[1] = veganStation.getUtilization(currentTime);
        utilizationPercentages[2] = normalStation.getUtilization(currentTime);
        double cashierUtil1 = cashierStation.getUtilization(currentTime);
        double cashierUtil2 = cashierStation2.getUtilization(currentTime);
        utilizationPercentages[3] = (cashierUtil1 + cashierUtil2) / 2.0;
        utilizationPercentages[4] = selfServiceStation.getUtilization(currentTime);
        utilizationPercentages[5] = coffeeStation.getUtilization(currentTime);

        controller.updateUtilization(utilizationPercentages, currentTime);
    }
    /**
     * Generates a SimulationStatistics object containing current simulation metrics.
     *
     * @return SimulationStatistics object with current statistics
     */
    private SimulationStatistics getStatistics() {
        double currentTime = Clock.getInstance().getTime();
        double simulationHours = currentTime / 3600.0;

        double throughput = simulationHours > 0
                ? customersServed / simulationHours
                : 0.0;

        double avgWaitTime = customersServed > 0
                ? totalWaitTime / customersServed
                : 0.0;

        return new SimulationStatistics(
                customersServed,
                throughput,
                avgWaitTime,
                peakQueueLength,
                currentTime,
                customersRejected
        );
    }



    /**
     * Updates all displays during the simulation.
     * Called periodically by the engine to refresh UI elements.
     */
    @Override
    protected void updateDisplays() {
        // Update queue displays periodically during simulation
        updateQueueDisplays();
    }

    /**
     * Pauses the simulation by setting the paused flag to true.
     * The simulation will wait at the next checkPaused() call.
     */
    @Override
    public void pause() {
        super.pause();
    }

    /**
     * Resumes a paused simulation by clearing the paused flag and notifying
     * all waiting threads.
     */
    @Override
    public void resumeSimulation() {
        super.resumeSimulation();
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
    /**
     * Checks if the simulation is paused and blocks the thread if it is.
     * This method is called during event processing to respect pause requests.
     */
    private void checkPaused() {
        synchronized (pauseLock) {
            while (paused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    /**
     * Called when the simulation completes.
     * Exports simulation statistics to CSV and displays the end time.
     */
    @Override
    protected void results() {
        for (ServicePoint sp : servicePoints) {
            if (sp != null) {
                sp.finalizeStatistics();
            }
        }

        try {
            CsvExporter.export(getStatistics(), "SimulationResults.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        controller.showEndTime(Clock.getInstance().getTime());
    }

}