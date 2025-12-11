package simu.model;

import simu.framework.IEventType;

/**
 * Enumeration of all event types in the simulation.
 * Defines the different types of events that can occur during simulation.
 * 
 * @author Group 8
 * @version 1.0
 */
public enum EventType implements IEventType {
    /** Legacy arrival event type */
    ARR1,
    /** Legacy departure event type 1 */
    DEP1,
    /** Legacy departure event type 2 */
    DEP2,
    /** Legacy departure event type 3 */
    DEP3,
    /** Customer arrival event */
    ARRIVAL,
    /** Departure from grill meal station */
    MEAL_GRILL_DEP,
    /** Departure from vegan meal station */
    MEAL_VEGAN_DEP,
    /** Departure from normal meal station */
    MEAL_NORMAL_DEP,
    /** Departure from cashier payment station */
    PAYMENT_CASHIER_DEP,
    /** Departure from self-service payment station */
    PAYMENT_SELF_DEP,
    /** Departure from coffee station */
    COFFEE_DEP,
    /** Customer exit from system */
    EXIT;
}
