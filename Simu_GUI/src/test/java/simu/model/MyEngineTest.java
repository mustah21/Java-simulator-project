package simu.model;

import controller.IControllerMtoV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simu.framework.Trace;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MyEngine.
 * Uses JUnit 5.
 */
class MyEngineTest {

    private MyEngine engine;

    /**
     * Dummy implementation of IControllerMtoV so that
     * MyEngine can be constructed without a real GUI.
     */
    private static class DummyController implements IControllerMtoV {

        @Override
        public void showEndTime(double time) {}

        @Override
        public void visualiseCustomer(MealType mealType) {}

        @Override
        public void visualiseCustomerToPayment(MealType mealType,
                                               PaymentType paymentType,
                                               int cashierStationNumber) {}

        @Override
        public void visualiseCustomerToCoffee(PaymentType paymentType,
                                              int cashierStationNumber) {}

        @Override
        public void visualiseCustomerExitFromCoffee() {}

        @Override
        public void visualiseCustomerExitFromPayment(PaymentType paymentType,
                                                     int cashierStationNumber) {}

        @Override
        public void updateQueueDisplays(int grillQueue, int veganQueue, int normalQueue,
                                        int cashierQueue, int cashierQueue2,
                                        int selfServiceQueue, int coffeeQueue) {}

        @Override
        public void updateStatistics(double throughput, double avgWaitTime,
                                     int peakQueue, double simTime) {}
    }

    @BeforeEach
    void setUp() {
        // Prevent NullPointerException inside Trace.out(...) used in Customer constructor.
        // If your Trace class has a different API, adjust this line accordingly.
        Trace.setTraceLevel(Trace.Level.INFO);

        // maxQueueCapacity = 1 so queues become "full" after 1 customer.
        engine = new MyEngine(
                new DummyController(),
                10, 10, 10,   // grillTime, veganTime, normalTime
                10, 10, 10,   // cashierTime, selfServiceTime, coffeeTime
                false,        // variabilityEnabled
                true,         // selfServiceEnabled
                true,         // coffeeEnabled
                0,            // arrivalRate (not used in these unit tests)
                1             // maxQueueCapacity
        );
    }

    // ----------------------------------------------------------------------
    // 1. redirectToCashier: prefer cashier 1 → then cashier 2 → then -1
    // ----------------------------------------------------------------------
    @Test
    void redirectToCashier_prefersCashier1ThenCashier2ThenMinusOne() throws Exception {

        // Access the two cashier service points using reflection
        Field f1 = MyEngine.class.getDeclaredField("cashierStation");
        f1.setAccessible(true);
        ServicePoint cashier1 = (ServicePoint) f1.get(engine);

        Field f2 = MyEngine.class.getDeclaredField("cashierStation2");
        f2.setAccessible(true);
        ServicePoint cashier2 = (ServicePoint) f2.get(engine);

        // First customer should go to cashier 1
        Customer c1 = new Customer();
        c1.setPaymentType(PaymentType.CASHIER);
        int s1 = engine.redirectToCashier(c1);
        assertEquals(1, s1);
        assertEquals(1, cashier1.getQueueLength());
        assertEquals(0, cashier2.getQueueLength());

        // Second customer should go to cashier 2 (because cashier 1 is full)
        Customer c2 = new Customer();
        c2.setPaymentType(PaymentType.CASHIER);
        int s2 = engine.redirectToCashier(c2);
        assertEquals(2, s2);
        assertEquals(1, cashier1.getQueueLength());
        assertEquals(1, cashier2.getQueueLength());

        // Third customer should be rejected (both cashiers are full)
        Customer c3 = new Customer();
        c3.setPaymentType(PaymentType.CASHIER);
        int s3 = engine.redirectToCashier(c3);
        assertEquals(-1, s3);
    }

    // ----------------------------------------------------------------------
    // 2. routeToPayment: SELF_SERVICE should use self-service station when enabled and not full
    // ----------------------------------------------------------------------
    @Test
    void routeToPayment_selfServiceGoesToSelfServiceWhenEnabledAndFree() throws Exception {

        // Access self-service station by reflection to inspect its queue length
        Field fs = MyEngine.class.getDeclaredField("selfServiceStation");
        fs.setAccessible(true);
        ServicePoint selfService = (ServicePoint) fs.get(engine);

        Customer c = new Customer();
        c.setPaymentType(PaymentType.SELF_SERVICE);

        int station = engine.routeToPayment(c);

        assertEquals(0, station, "SELF_SERVICE payment should go to self-service station");
        assertEquals(1, selfService.getQueueLength(), "Self-service queue should contain one customer");
    }

    // ----------------------------------------------------------------------
    // 3. shouldSendToPayment: should return false when all payment-related queues are full
    // ----------------------------------------------------------------------
    @Test
    void shouldSendToPayment_falseWhenAllQueuesFull() throws Exception {

        // Fill self-service queue
        Customer s = new Customer();
        s.setPaymentType(PaymentType.SELF_SERVICE);
        engine.routeToPayment(s);

        // Fill cashier 1
        Customer c1 = new Customer();
        c1.setPaymentType(PaymentType.CASHIER);
        engine.routeToPayment(c1);

        // Fill cashier 2
        Customer c2 = new Customer();
        c2.setPaymentType(PaymentType.CASHIER);
        engine.routeToPayment(c2);

        // Fill coffee station directly
        Field fc = MyEngine.class.getDeclaredField("coffeeStation");
        fc.setAccessible(true);
        ServicePoint coffee = (ServicePoint) fc.get(engine);
        coffee.addQueue(new Customer());

        // Now self-service, cashier1, cashier2, and coffee queues are all at capacity
        assertFalse(engine.shouldSendToPayment(),
                "shouldSendToPayment should be false when all payment/coffee queues are full");
    }
}
