package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

import static simu.model.MealType.NORMAL;
import static simu.model.MealType.VEGAN;
import static simu.model.PaymentType.CASHIER;
import static simu.model.PaymentType.SELF_SERVICE;


public class MyEngine extends Engine {
    private ArrivalProcess arrivalProcess;
    private ServicePoint grillStation;
    private ServicePoint veganStation;
    private ServicePoint normalStation;
    private ServicePoint cashierStation;
    private ServicePoint selfServiceStation;
    private ServicePoint coffeeStation;
    private boolean selfServiceEnabled = false;
    private boolean coffeeEnabled = false;

    public MyEngine(IControllerMtoV controller) { // NEW
        super(controller); // NEW

        // These eventTypes are taken from chatgpt, will update them once Tanvir merges his code

        grillStation = new ServicePoint(new Normal(45, 10), eventList, EventType.MEAL_GRILL_DEP);
        veganStation = new ServicePoint(new Normal(40, 8), eventList, EventType.MEAL_VEGAN_DEP);
        normalStation = new ServicePoint(new Normal(30, 5), eventList, EventType.MEAL_NORMAL_DEP);
        cashierStation = new ServicePoint(new Normal(20, 3), eventList, EventType.PAYMENT_CASHIER_DEP);
        selfServiceStation = new ServicePoint(new Normal(12, 2), eventList, EventType.PAYMENT_SELF_DEP);
        coffeeStation = new ServicePoint(new Normal(10, 2), eventList, EventType.COFFEE_DEP);

        // Redundant: Originally were here will remove them soon
        servicePoints = new ServicePoint[3];
        servicePoints[0] = new ServicePoint(new Normal(10, 6), eventList, EventType.DEP1);
        servicePoints[1] = new ServicePoint(new Normal(10, 10), eventList, EventType.DEP2);
        servicePoints[2] = new ServicePoint(new Normal(5, 3), eventList, EventType.DEP3);

        arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARR1);
    }

    @Override
    protected void initialization() {
        arrivalProcess.generateNext();     // First arrival in the system
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer a;

        switch ((EventType) t.getType()) {
            case ARR1: {
                Customer customer = new Customer();

                switch (customer.getMealType()) {
                    case GRILL:
                        grillStation.addQueue(customer);
                        break;
                    case VEGAN:
                        veganStation.addQueue(customer);
                        break;
                    case NORMAL:
                        normalStation.addQueue(customer);
                        break;
                }
            }
            arrivalProcess.generateNext();
            controller.visualiseCustomer(); // keep this here idk chatgpt says to keep it here
            break;

            case MEAL_GRILL_DEP: {
                Customer customer = grillStation.removeQueue();
                routeToPayment(customer);
                break;
            }
            case MEAL_VEGAN_DEP: {
                Customer customer = veganStation.removeQueue();
                routeToPayment(customer);
                break;
            }
            case MEAL_NORMAL_DEP: {
                Customer customer = normalStation.removeQueue();
                routeToPayment(customer);
                break;
            }
            case PAYMENT_CASHIER_DEP: {
                Customer customer = cashierStation.removeQueue();
                routeAfterPayment(customer);
                break;
            }
            case PAYMENT_SELF_DEP: {
                Customer customer = selfServiceStation.removeQueue();
                routeAfterPayment(customer);
                break;
            }
            case COFFEE_DEP: {
                Customer customer = coffeeStation.removeQueue();
                customer.setRemovalTime(Clock.getInstance().getTime());
                customer.reportResults();
                break;
            }
        }
    }


    protected void routeToPayment(Customer customer) {
        switch (customer.getPaymentType()) {
            case SELF_SERVICE:
                if (selfServiceEnabled) {
                    selfServiceStation.addQueue(customer);
                } else {
                    cashierStation.addQueue(customer);
                }
                break;
            case CASHIER:
                cashierStation.addQueue(customer);
                break;
        }
    }

    private void routeAfterPayment(Customer customer) {
        if (coffeeEnabled && customer.isWantsCoffee()) {
            coffeeStation.addQueue(customer);
        } else {
            // Exit flow: customer leaves, record removal time and stats
            customer.setRemovalTime(Clock.getInstance().getTime());
            customer.reportResults();
        }
    }

    @Override
    protected void results() {
        // OLD text UI
        //System.out.println("Simulation ended at " + Clock.getInstance().getClock());
        //System.out.println("Results ... are currently missing");

        // NEW GUI
        controller.showEndTime(Clock.getInstance().getTime());
    }
}

