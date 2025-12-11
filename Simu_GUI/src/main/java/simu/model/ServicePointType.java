package simu.model;

import simu.framework.IEventType;

/**
 * Enumeration of service point types in the cafeteria.
 * Used for categorizing and tracking different types of service points.
 * 
 * @author Group 8
 * @version 1.0
 */
public enum ServicePointType implements IEventType {
    /** Meal preparation service point */
    MEAL,
    /** Cashier payment service point */
    CASHIER,
    /** Self-service payment point */
    SELF_SERVICE,
    /** Coffee service point */
    COFFEE
}
