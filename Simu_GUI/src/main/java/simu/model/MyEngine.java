package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;



public class MyEngine extends Engine {
    private ArrivalProcess arrivalProcess;

    // Created by servicePointFactory
    private ServicePoint[] servicePoints; // created by factory
    private ServicePoint grillStation;
    private ServicePoint veganStation;
    private ServicePoint normalStation;
    private ServicePoint cashierStation;
    private ServicePoint selfServiceStation;
    private ServicePoint coffeeStation;


    private boolean selfServiceEnabled = false;
    private boolean coffeeEnabled = false;

    public MyEngine(IControllerMtoV controller) {
        super(controller);

        // Create all service points through the factory in one place.
        // Pass realistic mean times and flags; tweak numbers or expose them to GUI later.
        servicePoints = ServicePointFactory.createServicePoints(
                /*grillTime*/ 45, /*veganTime*/ 40, /*normalTime*/ 30,
                /*cashierTime*/ 20, /*selfServiceTime*/ 12, /*coffeeTime*/ 10,
                /*variabilityEnabled*/ true,
                /*selfServiceEnabled*/ selfServiceEnabled,
                /*coffeeEnabled*/ coffeeEnabled,
                /*eventList*/ eventList
        );

        grillStation = servicePoints[ServicePointFactory.GRILL_STATION];
        veganStation = servicePoints[ServicePointFactory.VEGAN_STATION];
        normalStation = servicePoints[ServicePointFactory.NORMAL_STATION];
        cashierStation = servicePoints[ServicePointFactory.CASHIER_STATION];
        selfServiceStation = servicePoints[ServicePointFactory.SELF_SERVICE_STATION];
        coffeeStation = servicePoints[ServicePointFactory.COFFEE_STATION];

        arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARR1);
    }

    @Override
    protected void initialization() {
        arrivalProcess.generateNext();     // First arrival in the system
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer customer;

        switch ((EventType) t.getType()) {
            case ARR1: {
                Customer c = new Customer();
                switch (c.getMealType()) {
                    case GRILL: grillStation.addQueue(c); break;
                    case VEGAN: veganStation.addQueue(c); break;
                    case NORMAL: normalStation.addQueue(c); break;
                }
                arrivalProcess.generateNext();
                controller.visualiseCustomer();
                break;
            }

            case MEAL_GRILL_DEP: {
                Customer c = grillStation.removeQueue();
                routeToPayment(c);
                break;
            }
            case MEAL_VEGAN_DEP: {
                Customer c = veganStation.removeQueue();
                routeToPayment(c);
                break;
            }
            case MEAL_NORMAL_DEP: {
                Customer c = normalStation.removeQueue();
                routeToPayment(c);
                break;
            }

            case PAYMENT_CASHIER_DEP: {
                Customer c = cashierStation.removeQueue();
                routeAfterPayment(c);
                break;
            }
            case PAYMENT_SELF_DEP: {
                Customer c = selfServiceStation.removeQueue();
                routeAfterPayment(c);
                break;
            }

            case COFFEE_DEP: {
                Customer c = coffeeStation.removeQueue();
                c.setRemovalTime(Clock.getInstance().getTime());
                c.reportResults();
                break;
            }
        }
    }

    protected void routeToPayment(Customer customer) {
        switch (customer.getPaymentType()) {
            case SELF_SERVICE:
                if (selfServiceStation.isEnabled()) selfServiceStation.addQueue(customer);
                else cashierStation.addQueue(customer);
                break;
            case CASHIER:
                cashierStation.addQueue(customer);
                break;
        }
    }

    private void routeAfterPayment(Customer customer) {
        if (ServicePointFactory.shouldVisitCoffeeStation(servicePoints, customer.isWantsCoffee())) {
            coffeeStation.addQueue(customer);
        } else {
            customer.setRemovalTime(Clock.getInstance().getTime());
            customer.reportResults();
        }
    }

    @Override
    protected void results() {
        controller.showEndTime(Clock.getInstance().getTime());
    }
}