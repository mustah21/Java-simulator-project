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

    private int maxQueueCapacity;
    private boolean arrivalsStopped = false;

    // Statistics tracking
    private int customersServed = 0;
    private double totalWaitTime = 0.0;
    private int peakQueueLength = 0;
    private int customersRejected = 0;

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
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = veganStation.removeQueue();
                
                c.markServiceEnd(ServicePointType.MEAL, serviceEndTime);
                
                if (veganStation.isOnQueue() && !veganStation.isReserved()) {
                    veganStation.beginService();
                }
                
                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
                if (arrivalsStopped && veganStation.hasQueueCapacity(maxQueueCapacity)) {
                    checkAndResumeArrivals();
                }
                updateQueueDisplays();
                break;
            }
            case MEAL_NORMAL_DEP: {
                double serviceEndTime = Clock.getInstance().getTime();
                Customer c = normalStation.removeQueue();
                
                c.markServiceEnd(ServicePointType.MEAL, serviceEndTime);
                
                if (normalStation.isOnQueue() && !normalStation.isReserved()) {
                    normalStation.beginService();
                }
                
                int cashierStation = routeToPayment(c);
                controller.visualiseCustomerToPayment(c.getMealType(), c.getPaymentType(), cashierStation);
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

                customersServed++;
                double totalTimeInSystem = c.getRemovalTime() - c.getArrivalTime();
                totalWaitTime += totalTimeInSystem;

                c.reportResults();
                updateQueueDisplays();
                break;
            }
        }

    }

    protected int routeToPayment(Customer customer) {
        int cashierStationNumber = 0;
        double arrivalTime = Clock.getInstance().getTime();
        ServicePoint paymentStation = null;
        
        switch (customer.getPaymentType()) {
            case SELF_SERVICE:
                if (selfServiceStation.isEnabled()) {
                    selfServiceStation.addQueue(customer);
                    paymentStation = selfServiceStation;
                    cashierStationNumber = 0;
                    customer.markServiceStart(ServicePointType.SELF_SERVICE, arrivalTime);
                    if (!selfServiceStation.isReserved() && selfServiceStation.isOnQueue()) {
                        selfServiceStation.beginService();
                    }
                } else {
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
    protected void endSimulation() {

    }

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



    @Override
    protected void updateDisplays() {
        // Update queue displays periodically during simulation
        updateQueueDisplays();
    }
    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resumeSimulation() {
        super.resumeSimulation();
    }
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