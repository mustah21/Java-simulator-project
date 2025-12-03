package simu.model;

import simu.framework.IEventType;

// TODO:
// Event types are defined by the requirements of the simulation model
public enum EventType implements IEventType {
    ARR1, DEP1, DEP2, DEP3,
    ARRIVAL, MEAL_GRILL_DEP, MEAL_VEGAN_DEP, MEAL_NORMAL_DEP,
    PAYMENT_CASHIER_DEP, PAYMENT_SELF_DEP, COFFEE_DEP, EXIT;
}
