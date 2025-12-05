package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;


// TODO:
// Customer to be implemented according to the requirements of the simulation model (data!)
public class Customer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private static int i = 1;
	private static long sum = 0;

    private static final Random rand = new Random();
	private MealType mealType;
    private PaymentType paymentType;
    private boolean wantsCoffee;


    private Map<ServicePointType, Double> waitTimes = new EnumMap<>(ServicePointType.class);
    private Map<ServicePointType, Double> serviceStartTimes = new EnumMap<>(ServicePointType.class);
    private Map<ServicePointType, Double> serviceEndTimes = new EnumMap<>(ServicePointType.class);


    public Customer() {
	    id = i++;
	    
		arrivalTime = Clock.getInstance().getTime();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
        mealType = assignMealType();
        paymentType = assignPaymentType();
        wantsCoffee = assignCoffeeDecision();
	}

    public void markServiceStart(ServicePointType type, double time) {
        serviceStartTimes.put(type, time);
    }
    public void markServiceEnd(ServicePointType type, double time) {
        serviceEndTimes.put(type, time);
    }
    public void addWaitTime(ServicePointType type, double time) {
        waitTimes.put(type, time);
    }

    private MealType assignMealType() {
        int r = rand.nextInt(100);
        if (r < 30) return MealType.GRILL;
        if (r < 60) return MealType.VEGAN;
        return MealType.NORMAL;
    }


    private PaymentType assignPaymentType() {
        int r = rand.nextInt(100);
        return r < 60 ? PaymentType.SELF_SERVICE : PaymentType.CASHIER;
    }

    private boolean assignCoffeeDecision() {
        return rand.nextDouble() < 0.30;
    }
    public boolean isWantsCoffee() {
        if (assignCoffeeDecision()) {
            wantsCoffee = true;
        }
        return wantsCoffee = false;
    }

    public MealType getMealType() {
        return mealType;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }
    /* Unchanged methods */


    public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum/id;
		System.out.println("Current mean of the customer service times " + mean);
	}
	
	public static void reset() {
		i = 1;
		sum = 0;
	}

}
