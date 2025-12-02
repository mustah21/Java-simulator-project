# Cafeteria Engine Implementation Backlog

## Overview

This backlog outlines the implementation tasks for transforming the generic 3-stage simulator into a cafeteria simulation system. The cafeteria flow: **Arrival → Meal Station → Payment → Coffee (optional) → Exit**

---

## Epic 1: Core Infrastructure & Event System

**Priority: HIGH** | **Status: Not Started**

### 1.1 Extend Event Types 

- [ Minh] **Task**: Add new event types to `EventType.java`
  - `ARRIVAL` - Customer arrives at cafeteria
  - `MEAL_GRILL_DEP` - Departure from Grill station
  - `MEAL_VEGAN_DEP` - Departure from Vegan station
  - `MEAL_NORMAL_DEP` - Departure from Normal station
  - `PAYMENT_CASHIER_DEP` - Departure from Cashier
  - `PAYMENT_SELF_DEP` - Departure from Self-service
  - `COFFEE_DEP` - Departure from Coffee station
  - `EXIT` - Customer leaves cafeteria
  - **Acceptance Criteria**: All event types defined and compile without errors

### 1.2 Create CafeteriaEngine Class

- [ Minh] **Task**: Create `CafeteriaEngine.java` extending `Engine`
  - Replace `MyEngine` with cafeteria-specific implementation
  - Initialize all service points (3 meal stations, 2 payment options, 1 coffee)
  - Configure arrival process with configurable arrival rate
  - **Acceptance Criteria**: Engine compiles and can be instantiated

### 1.3 Implement Opening Hours Logic

- [Minh ] **Task**: Add opening hours constraint to simulation
  - Read opening hours from UI (convert to simulation time units)
  - Stop generating arrivals after opening hours end
  - Allow simulation to continue until all customers are served
  - **Acceptance Criteria**: Simulation respects opening hours, no new arrivals after closing

---

## Epic 2: Customer Model & Behavior

**Priority: HIGH** | **Status: Not Started**

### 2.1 Enhance Customer Class

- [ Mustafa ] **Task**: Extend `Customer.java` with cafeteria-specific attributes
  - `mealChoice`: Enum (GRILL, VEGAN, NORMAL) - randomly assigned on arrival
  - `paymentChoice`: Enum (CASHIER, SELF_SERVICE) - based on availability/preference
  - `wantsCoffee`: boolean - randomly assigned (e.g., 30% probability)
  - `waitTimes`: Map tracking wait time at each station
  - `serviceStartTimes`: Map tracking when service started at each station
  - `serviceEndTimes`: Map tracking when service ended at each station
  - **Acceptance Criteria**: Customer tracks all journey data through cafeteria

### 2.2 Implement Customer Decision Logic

- [ Mustafa ] **Task**: Add methods for customer choices
  - `chooseMealStation()`: Random selection weighted by preferences
  - `choosePaymentMethod()`: Prefer self-service if enabled and shorter queue, else cashier
  - `decideCoffee()`: Random decision based on probability
  - **Acceptance Criteria**: Customers make realistic choices based on system state

### 2.3 Customer Routing Logic

- [ Mustafa ] **Task**: Implement customer flow through cafeteria
  - Route customer to chosen meal station
  - After meal, route to payment (cashier or self-service based on choice/availability)
  - After payment, route to coffee if wanted and enabled
  - After coffee (or skip), customer exits
  - **Acceptance Criteria**: Customers follow correct path through all stations

---

## Epic 3: Service Points Implementation

**Priority: HIGH** | **Status: Not Started**

### 3.1 Meal Station Service Points

- [ Tanvir ] **Task**: Create/configure meal station service points
  - **Grill Station**: Configurable service time (default 45s)
  - **Vegan Station**: Configurable service time (default 40s)
  - **Normal Station**: Configurable service time (default 30s)
  - Each station has independent queue
  - Service time can be fixed or variable based on toggle
  - **Acceptance Criteria**: Three meal stations operate independently with correct service times

### 3.2 Payment Service Points

- [Tanvir ] **Task**: Implement payment stations
  - **Cashier Station**: Configurable service time (default 20s), always available
  - **Self-Service Station**: Configurable service time (default 12s), can be enabled/disabled
  - Customers choose based on queue length and availability
  - If self-service disabled, all customers use cashier
  - **Acceptance Criteria**: Payment stations handle customers correctly, self-service toggle works

### 3.3 Coffee Station Service Point

- [ Tanvir] **Task**: Implement optional coffee station
  - Configurable service time (default 10s)
  - Can be enabled/disabled via UI
  - Only customers who want coffee visit this station
  - If disabled, customers skip coffee step
  - **Acceptance Criteria**: Coffee station only serves customers who want coffee, respects enable/disable

### 3.4 Queue Capacity Management

- [ ] **Task**: Add queue capacity limits to ServicePoint
  - Extend `ServicePoint` to support max queue capacity
  - Read capacity from UI (Unlimited, 50, 100, 200)
  - Reject customers if queue is full (or route to alternative)
  - Track rejected customers statistics
  - **Acceptance Criteria**: Queues respect capacity limits, rejected customers are tracked

### 3.5 Service Variability Implementation

- [ ] **Task**: Implement service time variability toggle
  - When ON: Use Normal distribution with mean and variance
  - When OFF: Use fixed service times (no randomness)
  - Apply to all service points consistently
  - **Acceptance Criteria**: Toggle changes service time behavior correctly

---

## Epic 4: Statistics & Metrics Collection

**Priority: MEDIUM** | **Status: Not Started**

### 4.1 Service Point Statistics

- [ ] **Task**: Add statistics tracking to `ServicePoint.java`
  - Total customers served
  - Total busy time (utilization)
  - Average queue length
  - Peak queue length
  - Average wait time
  - Average service time
  - **Acceptance Criteria**: All statistics calculated and accessible

### 4.2 Customer-Level Statistics

- [ ] **Task**: Track customer journey statistics
  - Total time in system (arrival to exit)
  - Time at each station (wait + service)
  - Number of customers served
  - Number of customers rejected (if queue full)
  - **Acceptance Criteria**: Statistics available for reporting

### 4.3 System-Level Metrics

- [ ] **Task**: Calculate overall system metrics
  - **Throughput**: Customers per hour (customers served / simulation hours)
  - **Average Wait Time**: Average time customers wait across all stations
  - **Peak Queue**: Maximum queue length across all stations
  - **Station Utilization**: Percentage of time each station is busy
  - **Acceptance Criteria**: All metrics calculated correctly

### 4.4 Real-Time Statistics Updates

- [ ] **Task**: Update statistics during simulation
  - Calculate metrics incrementally as events occur
  - Update UI labels in real-time (throughputLabel, avgWaitLabel, etc.)
  - Update charts (queueChart, utilChart) periodically
  - **Acceptance Criteria**: UI updates show live statistics during simulation

---

## Epic 5: UI Integration

**Priority: MEDIUM** | **Status: In Progress**

### 5.1 Connect UI Inputs to Engine

- [ ] **Task**: Read all UI parameters in Controller
  - Opening hours from `openingHoursField`
  - Arrival rate from `arrivalSlider` (convert students/hr to inter-arrival time)
  - Meal station times from `grillTime`, `veganTime`, `normalTime`
  - Payment times from `cashierTime`, `selfServiceTime`
  - Coffee time from `coffeeTime`
  - Self-service enabled from `enableSelfService`
  - Coffee enabled from `coffeeOptional`
  - Queue capacity from `queueCapacity`
  - Service variability from `variabilityToggle`
  - **Acceptance Criteria**: All UI inputs read and passed to engine

### 5.2 Update Run Button Handler

- [ ] **Task**: Connect Run button to start simulation
  - Read all parameters from UI
  - Create `CafeteriaEngine` with parameters
  - Start simulation thread
  - **Acceptance Criteria**: Clicking Run starts simulation with correct parameters

### 5.3 Implement Reset Button

- [ ] **Task**: Add reset functionality
  - Stop current simulation if running
  - Reset all statistics
  - Clear visualization
  - Reset UI to default values
  - **Acceptance Criteria**: Reset button clears everything and stops simulation

### 5.4 Implement Pause/Resume Functionality

- [ ] **Task**: Add pause and resume controls
  - Pause button stops simulation execution
  - Resume button continues from paused state
  - Track pause state in engine
  - **Acceptance Criteria**: Pause/resume works correctly without losing state

### 5.5 Update Metrics Display

- [ ] **Task**: Connect statistics to UI labels
  - Update `throughputLabel` with calculated throughput
  - Update `avgWaitLabel` with average wait time
  - Update `peakQueueLabel` with peak queue length
  - Update `simTimeLabel` with formatted simulation time (HH:MM)
  - **Acceptance Criteria**: All metric labels update during simulation

### 5.6 Implement Queue Length Chart

- [ ] **Task**: Populate `queueChart` with data
  - Track queue lengths over time for each station
  - Add data points periodically (e.g., every minute of sim time)
  - Display multiple series (one per station)
  - **Acceptance Criteria**: Chart shows queue length trends over time

### 5.7 Implement Utilization Chart

- [ ] **Task**: Populate `utilChart` with data
  - Calculate utilization percentage for each station
  - Display as bar chart with station names on x-axis
  - Update at end of simulation (or periodically)
  - **Acceptance Criteria**: Chart shows utilization percentages for all stations

---

## Epic 6: Visualization

**Priority: LOW** | **Status: Not Started**

### 6.1 Basic Canvas Visualization

- [ ] **Task**: Create visual representation on `simulationCanvas`
  - Draw cafeteria layout (stations, queues)
  - Show customers as moving entities
  - Show queue lengths visually
  - Update in real-time during simulation
  - **Acceptance Criteria**: Visual representation shows cafeteria state

### 6.2 Customer Animation

- [ ] **Task**: Animate customer movement
  - Show customers moving between stations
  - Indicate which station customer is at
  - Show waiting customers in queues
  - **Acceptance Criteria**: Customers visually move through cafeteria

### 6.3 Station Status Indicators

- [ ] **Task**: Visual indicators for station status
  - Color code stations (busy/available)
  - Show current customer being served
  - Display queue length visually
  - **Acceptance Criteria**: Station status clearly visible

---

## Epic 7: Advanced Features

**Priority: LOW** | **Status: Not Started**

### 7.1 Multiple Cashiers Support

- [ ] **Task**: Support multiple cashier stations
  - Allow configuration of number of cashiers
  - Distribute customers across available cashiers
  - Track statistics per cashier
  - **Acceptance Criteria**: Multiple cashiers work correctly

### 7.2 Customer Preferences

- [ ] **Task**: Add customer preference system
  - Some customers prefer specific meal types
  - Some customers always choose self-service
  - Some customers always want coffee
  - **Acceptance Criteria**: Customer preferences affect routing

### 7.3 Peak Hours Simulation

- [ ] **Task**: Implement variable arrival rates
  - Different arrival rates for different times of day
  - Simulate lunch rush hour
  - **Acceptance Criteria**: Arrival rate varies over time

### 7.4 Export Results

- [ ] **Task**: Add results export functionality
  - Export statistics to CSV
  - Export customer journey data
  - Generate summary report
  - **Acceptance Criteria**: Results can be exported to file

---

## Epic 8: Testing & Validation

**Priority: MEDIUM** | **Status: Not Started**

### 8.1 Unit Tests - Service Points

- [ ] **Task**: Write unit tests for ServicePoint
  - Test queue operations
  - Test service time generation
  - Test capacity limits
  - **Acceptance Criteria**: All ServicePoint methods tested

### 8.2 Unit Tests - Customer

- [ ] **Task**: Write unit tests for Customer
  - Test customer creation
  - Test choice methods
  - Test statistics tracking
  - **Acceptance Criteria**: All Customer methods tested

### 8.3 Integration Tests - Engine

- [ ] **Task**: Write integration tests for CafeteriaEngine
  - Test full customer journey
  - Test event processing
  - Test statistics calculation
  - **Acceptance Criteria**: Engine processes events correctly

### 8.4 Validation Tests

- [ ] **Task**: Validate simulation correctness
  - Compare results with expected values
  - Test edge cases (empty queues, full queues, disabled stations)
  - Test boundary conditions
  - **Acceptance Criteria**: Simulation produces correct results

---

## Implementation Order Recommendation

### Sprint 1 (Foundation)

1. Epic 1.1 - Extend Event Types
2. Epic 1.2 - Create CafeteriaEngine Class
3. Epic 2.1 - Enhance Customer Class
4. Epic 3.1 - Meal Station Service Points

### Sprint 2 (Core Flow)

5. Epic 2.2 - Customer Decision Logic
6. Epic 2.3 - Customer Routing Logic
7. Epic 3.2 - Payment Service Points
8. Epic 3.3 - Coffee Station

### Sprint 3 (Configuration & Statistics)

9. Epic 1.3 - Opening Hours Logic
10. Epic 3.4 - Queue Capacity Management
11. Epic 3.5 - Service Variability
12. Epic 4.1 - Service Point Statistics

### Sprint 4 (UI Integration)

13. Epic 5.1 - Connect UI Inputs
14. Epic 5.2 - Update Run Button
15. Epic 5.3 - Implement Reset Button
16. Epic 5.4 - Pause/Resume

### Sprint 5 (Metrics & Visualization)

17. Epic 4.2 - Customer-Level Statistics
18. Epic 4.3 - System-Level Metrics
19. Epic 5.5 - Update Metrics Display
20. Epic 5.6 - Queue Length Chart
21. Epic 5.7 - Utilization Chart

### Sprint 6 (Polish & Testing)

22. Epic 4.4 - Real-Time Statistics Updates
23. Epic 6 - Visualization (if time permits)
24. Epic 8 - Testing & Validation

---

## Notes

- **Time Units**: Decide on time units (seconds vs. minutes). UI shows seconds, but simulation might use minutes.
- **Arrival Rate Conversion**: Need to convert "students/hour" to inter-arrival time for Negative Exponential distribution.
- **Thread Safety**: Ensure statistics updates are thread-safe when updating UI from simulation thread.
- **Performance**: Consider performance implications of real-time updates and visualization.

---

## Definition of Done

Each task is considered done when:

- [ ] Code is implemented and compiles without errors
- [ ] Code follows existing code style and patterns
- [ ] Functionality works as specified in acceptance criteria
- [ ] No obvious bugs or errors
- [ ] Code is committed to version control
- ==
