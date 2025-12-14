package simu.model;

import simu.framework.IEventType;

/**
 * Enumeration of payment methods available in the cafeteria.
 * 
 * @author Group 8
 * @version 1.0
 */
public enum PaymentType implements IEventType {
    /** Payment through cashier */
    CASHIER,
    /** Self-service payment */
    SELF_SERVICE,
}
