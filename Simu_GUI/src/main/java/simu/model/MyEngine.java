package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

import java.io.IOException;


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

    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private int maxQueueCapacity;
    private boolean arrivalsStopped = false;

    // Statistics tracking
    private int customersServed = 0;
    private double totalWaitTime = 0.0;
    private int peakQueueLength = 0;

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

    @Override
    protected void initialization() {
        arrivalProcess.generateNext();     // First arrival in the system
    }

    private void helperSleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        switch ((EventType) t.getType()) {
            case ARR1: {
                Customer c = new Customer();
                MealType mealType = c.getMealType();
                checkPaused();
                helperSleep();

                // Check if the target station has capacity before adding customer
                ServicePoint targetStation = switch (mealType) {
                    case GRILL -> grillStation;
                    case VEGAN -> veganStation;
                    case NORMAL -> normalStation;
                };

                // Only add customer if the target station has capacity
                if (targetStation != null && targetStation.hasQueueCapacity(maxQueueCapacity)) {
                    targetStation.addQueue(c);
                    controller.visualiseCustomer(mealType);
                    arrivalsStopped = false; // Reset flag since we successfully added a customer
                }
                // If target station is full, customer is rejected (not added)

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
                Customer c = grillStation.removeQueue();
                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
                // Resume arrivals if they were stopped and now capacity is available
                if (arrivalsStopped && grillStation.hasQueueCapacity(maxQueueCapacity)) {
                    checkAndResumeArrivals();
                }
                updateQueueDisplays();
                break;
            }
            case MEAL_VEGAN_DEP: {
                if(!shouldSendToPayment())
                    return;
                Customer c = veganStation.removeQueue();
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
                Customer c = normalStation.removeQueue();
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
                // Check both cashier stations and remove from whichever is currently serving (reserved)
                // If both or neither are reserved, check which has customers
                Customer c = null;
                int cashierStationNumber = 1; // Default to first cashier
                if (cashierStation.isReserved() && cashierStation.isOnQueue()) {
                    c = cashierStation.removeQueue();
                    cashierStationNumber = 1;
                } else if (cashierStation2.isReserved() && cashierStation2.isOnQueue()) {
                    c = cashierStation2.removeQueue();
                    cashierStationNumber = 2;
                } else if (cashierStation.isOnQueue()) {
                    c = cashierStation.removeQueue();
                    cashierStationNumber = 1;
                } else if (cashierStation2.isOnQueue()) {
                    c = cashierStation2.removeQueue();
                    cashierStationNumber = 2;
                }
                if (c != null) {
                    routeAfterPayment(c, cashierStationNumber);
                }
                updateQueueDisplays();
                break;
            }
            case PAYMENT_SELF_DEP: {
                Customer c = selfServiceStation.removeQueue();
                routeAfterPayment(c, 0); // 0 indicates self-service
                updateQueueDisplays();
                break;
            }

            case COFFEE_DEP: {
                Customer c = coffeeStation.removeQueue();
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

    protected int routeToPayment(Customer customer) {
        int cashierStationNumber = 0; // 0 = self-service, 1 = cashier1, 2 = cashier2
        switch (customer.getPaymentType()) {
            case SELF_SERVICE:
                if (selfServiceStation.isEnabled() && selfServiceStation.getQueueLength() < maxQueueCapacity) {
                    selfServiceStation.addQueue(customer);
                    cashierStationNumber = 0;
                } else {
                    cashierStationNumber = redirectToCashier(customer);
                }
                break;
            case CASHIER:
                cashierStationNumber = redirectToCashier(customer);
                break;
        }

        updateQueueDisplays();
        return cashierStationNumber;
    }
    protected int redirectToCashier(Customer customer) {
            if(cashierStation.getQueueLength()<maxQueueCapacity){
                cashierStation.addQueue(customer);
                customer.setPaymentType(PaymentType.CASHIER);
                return 1;
        }
            else if (cashierStation2.getQueueLength()<maxQueueCapacity){
                cashierStation2.addQueue(customer);
                customer.setPaymentType(PaymentType.CASHIER);
                return 2;
            }
            else {
                return -1; // Default to first cashier if both are full
            }

    }
    protected void endSimulation() {

    }

    private void routeAfterPayment(Customer customer, int cashierStationNumber) {
        if (ServicePointFactory.shouldVisitCoffeeStation(servicePoints, customer.isWantsCoffee())) {
            controller.visualiseCustomerToCoffee(customer.getPaymentType(), cashierStationNumber);
            coffeeStation.addQueue(customer);
            updateQueueDisplays();
        } else {
            controller.visualiseCustomerExitFromPayment(customer.getPaymentType(), cashierStationNumber);
            customer.setRemovalTime(Clock.getInstance().getTime());

            // Update statistics for customers exiting without coffee
            customersServed++;
            double totalTimeInSystem = customer.getRemovalTime() - customer.getArrivalTime();
            totalWaitTime += totalTimeInSystem;

            customer.reportResults();
        }
    }

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

    private void updateStatistics() {
        double currentTime = Clock.getInstance().getTime();
        double simulationHours = currentTime / 3600.0; // Convert seconds to hours

        // Calculate throughput (customers per hour)
        double throughput = simulationHours > 0 ? customersServed / simulationHours : 0.0;

        // Calculate average wait time (average time in system)
        double avgWaitTime = customersServed > 0 ? totalWaitTime / customersServed : 0.0;

        // Update statistics display
        controller.updateStatistics(throughput, avgWaitTime, peakQueueLength, currentTime);

    }
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
                currentTime
        );
    }



    @Override
    protected void updateDisplays() {
        // Update queue displays periodically during simulation
        updateQueueDisplays();
    }
    public void pause() {
        paused = true;
    }

    public void resumeSimulation() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
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
    @Override
    protected void results() {
        try {
            CsvExporter.export(getStatistics(), "SimulationResults.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        controller.showEndTime(Clock.getInstance().getTime());
    }

}