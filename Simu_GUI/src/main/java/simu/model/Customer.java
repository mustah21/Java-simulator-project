package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents a customer in the cafeteria simulation.
 * Each customer has attributes such as meal type, payment preference, and coffee preference.
 * Tracks timing information for statistics collection.
 * 
 * @author Group 8
 * @version 1.0
 */
public class Customer {
	/** Time when the customer arrived at the system */
	private double arrivalTime;
	/** Time when the customer left the system */
	private double removalTime;
	/** Unique identifier for this customer */
	private int id;
	/** Static counter for generating unique customer IDs */
	private static int i = 1;
	/** Static accumulator for total service times across all customers */
	private static long sum = 0;

    /** Random number generator for customer attribute assignment */
    private static final Random rand = new Random();
	/** Type of meal the customer wants (GRILL, VEGAN, or NORMAL) */
	private MealType mealType;
    /** Payment method preference (SELF_SERVICE or CASHIER) */
    private PaymentType paymentType;
    /** Whether the customer wants coffee */
    private boolean wantsCoffee;

    /** Map tracking wait times at each service point type */
    private Map<ServicePointType, Double> waitTimes = new EnumMap<>(ServicePointType.class);
    /** Map tracking service start times at each service point type */
    private Map<ServicePointType, Double> serviceStartTimes = new EnumMap<>(ServicePointType.class);
    /** Map tracking service end times at each service point type */
    private Map<ServicePointType, Double> serviceEndTimes = new EnumMap<>(ServicePointType.class);

    /**
     * Constructs a new Customer instance.
     * Assigns arrival time, unique ID, and randomly assigns meal type,
     * payment type, and coffee preference based on predefined probabilities.
     */
    public Customer() {
	    id = i++;
	    
		arrivalTime = Clock.getInstance().getTime();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
        mealType = assignMealType();
        paymentType = assignPaymentType();
        wantsCoffee = assignCoffeeDecision();
	}

    /**
     * Marks the start of service at a specific service point.
     * 
     * @param type The type of service point where service started
     * @param time The simulation time when service started
     */
    public void markServiceStart(ServicePointType type, double time) {
        serviceStartTimes.put(type, time);
    }
    
    /**
     * Marks the end of service at a specific service point.
     * 
     * @param type The type of service point where service ended
     * @param time The simulation time when service ended
     */
    public void markServiceEnd(ServicePointType type, double time) {
        serviceEndTimes.put(type, time);
    }
    
    /**
     * Records wait time at a specific service point.
     * 
     * @param type The type of service point where waiting occurred
     * @param time The wait time in seconds
     */
    public void addWaitTime(ServicePointType type, double time) {
        waitTimes.put(type, time);
    }

    /**
     * Randomly assigns a meal type based on probabilities:
     * 30% GRILL, 30% VEGAN, 40% NORMAL
     * 
     * @return The assigned MealType
     */
    private MealType assignMealType() {
        int r = rand.nextInt(100);
        if (r < 30) return MealType.GRILL;
        if (r < 60) return MealType.VEGAN;
        return MealType.NORMAL;
    }


    /**
     * Randomly assigns a payment type based on probabilities:
     * 60% SELF_SERVICE, 40% CASHIER
     * 
     * @return The assigned PaymentType
     */
    private PaymentType assignPaymentType() {
        int r = rand.nextInt(100);
        return r < 60 ? PaymentType.SELF_SERVICE : PaymentType.CASHIER;
    }

    /**
     * Randomly decides if the customer wants coffee.
     * 30% probability of wanting coffee.
     * 
     * @return true if customer wants coffee, false otherwise
     */
    private boolean assignCoffeeDecision() {
        return rand.nextDouble() < 0.30;
    }
    
    /**
     * Checks if the customer wants coffee.
     * If not yet determined, randomly assigns the preference.
     * 
     * @return true if customer wants coffee, false otherwise
     */
    public boolean isWantsCoffee() {
        if (assignCoffeeDecision()) {
            wantsCoffee = true;
        }
        return wantsCoffee;
    }

    /**
     * Gets the meal type for this customer.
     * 
     * @return The MealType assigned to this customer
     */
    public MealType getMealType() {
        return mealType;
    }

    /**
     * Gets the payment type for this customer.
     * 
     * @return The PaymentType assigned to this customer
     */
    public PaymentType getPaymentType() {
        return paymentType;
    }

    /**
     * Gets the removal time (exit time) for this customer.
     * 
     * @return The simulation time when the customer left the system
     */
    public double getRemovalTime() {
		return removalTime;
	}

	/**
	 * Sets the removal time (exit time) for this customer.
	 * 
	 * @param removalTime The simulation time when the customer left the system
	 */
	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	/**
	 * Gets the arrival time for this customer.
	 * 
	 * @return The simulation time when the customer arrived
	 */
	public double getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Sets the arrival time for this customer.
	 * 
	 * @param arrivalTime The simulation time when the customer arrived
	 */
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	/**
	 * Reports the results for this customer to the trace output.
	 * Calculates and displays arrival time, removal time, and total time in system.
	 * Also updates the running mean of customer service times.
	 */
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum/id;
		System.out.println("Current mean of the customer service times " + mean);
	}
	
	/**
	 * Resets the static customer counters.
	 * Used when starting a new simulation to reset customer ID generation
	 * and accumulated statistics.
	 */
	public static void reset() {
		i = 1;
		sum = 0;
	}

}
