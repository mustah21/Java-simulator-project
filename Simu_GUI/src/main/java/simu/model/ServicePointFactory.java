package simu.model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.FixedTimeGenerator;
import eduni.distributions.Normal;
import simu.framework.EventList;

public class ServicePointFactory {

    public static final int GRILL_STATION = 0;
    public static final int VEGAN_STATION = 1;
    public static final int NORMAL_STATION = 2;
    public static final int CASHIER_STATION = 3;
    public static final int SELF_SERVICE_STATION = 4;
    public static final int COFFEE_STATION = 5;

    public static ServicePoint[] createServicePoints(
            double grillTime, double veganTime, double normalTime,
            double cashierTime, double selfServiceTime, double coffeeTime,
            boolean variabilityEnabled, boolean selfServiceEnabled, boolean coffeeEnabled,
            EventList eventList) {

        ServicePoint[] servicePoints = new ServicePoint[6];

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

    private static ContinuousGenerator createGenerator(double meanTime, boolean variabilityEnabled) {
        if (variabilityEnabled) {
            return new Normal(meanTime, meanTime * 0.1);
        } else {
            return new FixedTimeGenerator(meanTime);
        }
    }

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

    public static boolean shouldVisitCoffeeStation(ServicePoint[] servicePoints, boolean customerWantsCoffee) {
        if (!servicePoints[COFFEE_STATION].isEnabled()) {
            return false;
        }
        return customerWantsCoffee;
    }
}

