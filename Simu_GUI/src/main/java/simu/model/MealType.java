package simu.model;

import simu.framework.IEventType;

/**
 * Enumeration of meal types available in the cafeteria.
 * 
 * @author Group 8
 * @version 1.0
 */
public enum MealType implements IEventType {
    /** Grill meal type */
    GRILL,
    /** Vegan meal type */
    VEGAN,
    /** Normal meal type */
    NORMAL,
}
