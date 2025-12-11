package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import simu.framework.IEngine;
import simu.model.MyEngine;
import view.ISimulatorUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller class for the cafeteria simulation GUI.
 * Implements both IControllerVtoM (view-to-model) and IControllerMtoV (model-to-view) interfaces.
 * Manages UI interactions, simulation control, and updates visualizations and statistics.
 * 
 * @author Group 8
 * @version 1.0
 */
public class Controller implements IControllerVtoM, IControllerMtoV, Initializable {
    private static final int MIN_DELAY = 1;
    private static final long MAX_DELAY = 2000;  // 2 seconds
    private IEngine engine;
	private ISimulatorUI ui;
	
	// FXML UI Elements
	@FXML private TextField openingHoursField;
	@FXML private Slider arrivalSlider;
	@FXML private Label arrivalValue;
	@FXML private TextField grillTime;
	@FXML private TextField veganTime;
	@FXML private TextField normalTime;
	@FXML private TextField cashierTime;
	@FXML private TextField selfServiceTime;
	@FXML private CheckBox enableSelfService;
	@FXML private TextField coffeeTime;
	@FXML private CheckBox coffeeOptional;
	@FXML private ChoiceBox<String> queueCapacity;
	@FXML private ToggleButton variabilityToggle;
	@FXML private Button runBtn2;
	@FXML private Button resetBtn;
	@FXML private Button resumeBtn;
	@FXML private Button pauseBtn;
	@FXML private Label throughputLabel;
	@FXML private Label avgWaitLabel;
	@FXML private Label peakQueueLabel;
	@FXML private Label simTimeLabel;
	@FXML private Pane simulationCanvas;
	@FXML private Button btnIncreaseSpeed;
    @FXML private Button btnDecreaseSpeed;


	// Charts
	@FXML private LineChart<Number, Number> queueChart;
	@FXML private BarChart<String, Number> utilChart;
	
	// Chart data tracking
	private XYChart.Series<Number, Number> totalQueueSeries;
	private List<ChartDataPoint> queueHistory = new ArrayList<>();
	private int[] latestQueueData = new int[6]; // Store latest queue data for utilization chart
	private double lastCollectionTime = -1.0; // Track last collection time to avoid duplicates
	
	// Track utilization data (time when each station was busy)
	private List<UtilizationDataPoint> utilizationHistory = new ArrayList<>();
	
	/**
	 * Helper class to track utilization data points over time.
	 */
	private static class UtilizationDataPoint {
		/** Simulation time for this data point */
		double time;
		/** Queue lengths for all 6 stations at this time */
		int[] queueLengths; // Queue lengths for all 6 stations
		
		/**
		 * Constructs a new UtilizationDataPoint.
		 * 
		 * @param time Simulation time
		 * @param queueLengths Array of queue lengths for all stations
		 */
		UtilizationDataPoint(double time, int[] queueLengths) {
			this.time = time;
			this.queueLengths = queueLengths.clone();
		}
	}
	
	/**
	 * Helper class to track chart data points for queue length over time.
	 */
	private static class ChartDataPoint {
		/** Simulation time for this data point */
		double time;
		/** Total queue length at this time */
		int queueLength;
		
		/**
		 * Constructs a new ChartDataPoint.
		 * 
		 * @param time Simulation time
		 * @param queueLength Total queue length
		 */
		ChartDataPoint(double time, int queueLength) {
			this.time = time;
			this.queueLength = queueLength;
		}
	}
	
	// Queue progress bars
	@FXML private javafx.scene.control.ProgressBar veganQueueProgress;
	@FXML private javafx.scene.control.ProgressBar normalQueueProgress;
	@FXML private javafx.scene.control.ProgressBar grillQueueProgress;
	@FXML private javafx.scene.control.ProgressBar selfServiceQueueProgress;
	@FXML private javafx.scene.control.ProgressBar cashierQueueProgress;
	@FXML private javafx.scene.control.ProgressBar cashierQueueProgress2;
	@FXML private javafx.scene.control.ProgressBar coffeeQueueProgress;
	
	// Queue labels
	@FXML private Label veganQueueLabel;
	@FXML private Label normalQueueLabel;
	@FXML private Label grillQueueLabel;
	@FXML private Label selfServiceQueueLabel;
	@FXML private Label cashierQueueLabel1;
	@FXML private Label cashierQueueLabel2;
	@FXML private Label coffeeQueueLabel;
	
	/**
	 * Default constructor required for FXML loading.
	 */
	public Controller() {
		// No-arg constructor required for FXML
	}
	
	/**
	 * Sets the UI reference for this controller.
	 * 
	 * @param ui The ISimulatorUI instance
	 */
	public void setUI(ISimulatorUI ui) {
		this.ui = ui;
	}
	
	/**
	 * Gets the simulation canvas pane.
	 * 
	 * @return The Pane used for simulation visualization
	 */
	public Pane getSimulationCanvas() {
		return simulationCanvas;
	}
	
	/**
	 * Initializes the controller after FXML loading.
	 * Sets up event handlers for UI controls and initializes charts.
	 * 
	 * @param location The location used to resolve relative paths for the root object
	 * @param resources The resources used to localize the root object
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Wire up arrival slider to update label
		if (arrivalSlider != null && arrivalValue != null) {
			arrivalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
				arrivalValue.setText(String.valueOf(newVal.intValue()));
			});
		}
		
		// Wire up Run button to start simulation
		if (runBtn2 != null) {
			runBtn2.setOnAction(e -> startSimulation());
		}
		
		// Wire up Reset button
		if (resetBtn != null) {
			resetBtn.setOnAction(e -> resetSimulation());
		}
		
		// Wire up Pause button
		if (pauseBtn != null) {
			pauseBtn.setOnAction(e -> pauseSimulation());
		}
		
		// Wire up Resume button
		if (resumeBtn != null) {
			resumeBtn.setOnAction(e -> resumeSimulation());
		}
        if (btnIncreaseSpeed != null) {
            btnIncreaseSpeed.setOnAction(e -> increaseSpeed());
        }

        if (btnDecreaseSpeed != null) {
            btnDecreaseSpeed.setOnAction(e -> decreaseSpeed());
        }

        // Initialize charts
		initializeCharts();
	}

	/**
	 * Initializes the queue length and utilization charts.
	 * Sets up axes, titles, and data series.
	 */
	private void initializeCharts() {
		// Initialize queue chart
		if (queueChart != null) {
			queueChart.setTitle("Total Queue Length Over Time");
			queueChart.setAnimated(false); // Disable animation for better performance
			queueChart.setCreateSymbols(false); // Don't show symbols for cleaner look
			
			// Set axis labels
			NumberAxis xAxis = (NumberAxis) queueChart.getXAxis();
			xAxis.setLabel("Time (seconds)");
			NumberAxis yAxis = (NumberAxis) queueChart.getYAxis();
			yAxis.setLabel("Queue Length");
			
			totalQueueSeries = new XYChart.Series<>();
			totalQueueSeries.setName("Total Queue Length");
			queueChart.getData().add(totalQueueSeries);
		}
		
		// Initialize utilization chart
		if (utilChart != null) {
			utilChart.setTitle("Station Utilization");
			utilChart.setAnimated(false);
			utilChart.setLegendVisible(false);
			
			// Set axis labels
			CategoryAxis xAxis = (CategoryAxis) utilChart.getXAxis();
			xAxis.setLabel("Station");
			NumberAxis yAxis = (NumberAxis) utilChart.getYAxis();
			yAxis.setLabel("Utilization (%)");
		}
	}
	
	/**
	 * Prints current GUI settings to the console for debugging.
	 * Displays all simulation parameters and current metrics.
	 */
	private void printGUIData() {
		System.out.println("=== Simulation Settings ===");
		System.out.println("Opening Hours: " + (openingHoursField != null ? openingHoursField.getText() : "N/A"));
		System.out.println("Arrival Rate: " + (arrivalSlider != null ? arrivalSlider.getValue() : "N/A") + " students/hr");
		System.out.println("\n=== Meal Station Timings ===");
		System.out.println("Grill: " + (grillTime != null ? grillTime.getText() : "N/A") + " s");
		System.out.println("Vegan: " + (veganTime != null ? veganTime.getText() : "N/A") + " s");
		System.out.println("Normal: " + (normalTime != null ? normalTime.getText() : "N/A") + " s");
		System.out.println("\n=== Payment ===");
		System.out.println("Cashier: " + (cashierTime != null ? cashierTime.getText() : "N/A") + " s");
		System.out.println("Self-service: " + (selfServiceTime != null ? selfServiceTime.getText() : "N/A") + " s");
		System.out.println("Enable Self-service: " + (enableSelfService != null ? enableSelfService.isSelected() : "N/A"));
		System.out.println("\n=== Coffee ===");
		System.out.println("Pickup: " + (coffeeTime != null ? coffeeTime.getText() : "N/A") + " s");
		System.out.println("Enable Coffee Station: " + (coffeeOptional != null ? coffeeOptional.isSelected() : "N/A"));
		System.out.println("\n=== Advanced ===");
		System.out.println("Queue Capacity: " + (queueCapacity != null ? queueCapacity.getValue() : "N/A"));
		System.out.println("Service Variability: " + (variabilityToggle != null ? (variabilityToggle.isSelected() ? "On" : "Off") : "N/A"));
		System.out.println("\n=== Current Metrics ===");
		System.out.println("Throughput: " + (throughputLabel != null ? throughputLabel.getText() : "N/A"));
		System.out.println("Avg Wait: " + (avgWaitLabel != null ? avgWaitLabel.getText() : "N/A"));
		System.out.println("Peak Queue: " + (peakQueueLabel != null ? peakQueueLabel.getText() : "N/A"));
		System.out.println("Sim Time: " + (simTimeLabel != null ? simTimeLabel.getText() : "N/A"));
		System.out.println("========================\n");
	}

	/**
	 * Starts a new simulation with parameters from the UI.
	 * Stops any existing simulation, reads UI values, creates a new engine,
	 * and starts the simulation thread.
	 */
	@Override
	public void startSimulation() {
		// Stop existing simulation if running
		if (engine != null && ((Thread) engine).isAlive()) {
			((Thread) engine).interrupt();
		}
		
		// Read values from UI controls
		double openingHours = parseDouble(openingHoursField, 3.0);
		double arrivalRate = arrivalSlider != null ? arrivalSlider.getValue() : 120.0;
		
		double grillTime = parseDouble(this.grillTime, 45.0);
		double veganTime = parseDouble(this.veganTime, 40.0);
		double normalTime = parseDouble(this.normalTime, 30.0);
		
		double cashierTime = parseDouble(this.cashierTime, 20.0);
		double selfServiceTime = parseDouble(this.selfServiceTime, 12.0);
		boolean selfServiceEnabled = enableSelfService != null && enableSelfService.isSelected();
		
		double coffeeTime = parseDouble(this.coffeeTime, 10.0);
		boolean coffeeEnabled = coffeeOptional != null && coffeeOptional.isSelected();
		
		boolean variabilityEnabled = variabilityToggle != null && variabilityToggle.isSelected();
		
		// Parse queue capacity from UI
		int maxQueueCapacity = Integer.MAX_VALUE; // Default to unlimited
		if (queueCapacity != null && queueCapacity.getValue() != null) {
			String capacityValue = queueCapacity.getValue();
			if (!capacityValue.equals("Unlimited")) {
				try {
					maxQueueCapacity = Integer.parseInt(capacityValue);
				} catch (NumberFormatException e) {
					maxQueueCapacity = Integer.MAX_VALUE; // Fallback to unlimited
				}
			}
		}
		
		// Convert opening hours to simulation time (convert hours -> seconds)
		// Service times are in seconds, so simulation time should also be in seconds
		double simulationTime = openingHours * 3600.0; // Convert hours to seconds
		
		// Create engine with user-provided parameters
		engine = new MyEngine(this, 
			grillTime, veganTime, normalTime,
			cashierTime, selfServiceTime, coffeeTime,
			variabilityEnabled, selfServiceEnabled, coffeeEnabled,
			arrivalRate, maxQueueCapacity
		);
		
		engine.setSimulationTime(simulationTime);
		engine.setDelay(250); // Default delay in milliseconds
		
		// Clear visualization
		if (ui != null && ui.getVisualisation() != null) {
			ui.getVisualisation().clearDisplay();
		}
		
		// Start simulation thread
		((Thread) engine).start();
	}
	
	/**
	 * Parses a double value from a TextField.
	 * Returns the default value if parsing fails or field is empty.
	 * 
	 * @param field The TextField to parse
	 * @param defaultValue The default value to return if parsing fails
	 * @return The parsed double value or default value
	 */
	private double parseDouble(TextField field, double defaultValue) {
		if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(field.getText().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Resets the simulation to initial state.
	 * Stops current simulation, resets clock and customer counters,
	 * clears visualizations, and resets all UI displays and charts.
	 */
	private void resetSimulation() {
		// Stop current simulation if running
		if (engine != null && ((Thread) engine).isAlive()) {
			((Thread) engine).interrupt();
			try {
				((Thread) engine).join(1000); // Wait up to 1 second for thread to finish
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		engine = null;
		
		// Reset Clock to 0
		simu.framework.Clock.getInstance().reset();
		
		// Reset Customer static counters
		simu.model.Customer.reset();
		
		// Clear visualization
		if (ui != null && ui.getVisualisation() != null) {
			ui.getVisualisation().clearDisplay();
		}
		
		// Reset metrics labels
		if (throughputLabel != null) throughputLabel.setText("0 students/hr");
		if (avgWaitLabel != null) avgWaitLabel.setText("0 s");
		if (peakQueueLabel != null) peakQueueLabel.setText("0");
		if (simTimeLabel != null) simTimeLabel.setText("00:00");
		
		// Reset all progress bars to 0
		updateQueueDisplays(0, 0, 0, 0, 0, 0, 0);
		
		// Clear charts and reset data collection
		if (queueChart != null && totalQueueSeries != null) {
			totalQueueSeries.getData().clear();
		}
		queueHistory.clear();
		utilizationHistory.clear();
		latestQueueData = new int[6];
		lastCollectionTime = -1.0;
		
		if (utilChart != null) {
			utilChart.getData().clear();
		}
	}
	
	/**
	 * Pauses the running simulation.
	 */
	private void pauseSimulation() {
		if (engine != null && ((Thread) engine).isAlive()) {
			engine.pause();
		}
	}
	
	/**
	 * Resumes a paused simulation.
	 */
	private void resumeSimulation() {
		if (engine != null && ((Thread) engine).isAlive()) {
			engine.resumeSimulation();
		}
	}

    /**
     * Decreases simulation speed by doubling the delay.
     * Maximum delay is capped at MAX_DELAY.
     */
    @Override
    public void decreaseSpeed() {
        if (engine != null) {
            long newDelay = Math.min(MAX_DELAY, engine.getDelay() * 2);
            engine.setDelay(newDelay);
//            System.out.println("Speed decreased, delay  " + newDelay + " ms");
        }
    }


    /**
     * Increases simulation speed by halving the delay.
     * Minimum delay is capped at MIN_DELAY.
     */
    @Override
    public void increaseSpeed() {
        if (engine != null) {
            long newDelay = Math.max(MIN_DELAY, engine.getDelay() / 2);
            engine.setDelay(newDelay);
//            System.out.println("Speed increased, delay  " + newDelay + " ms");
        }
    }



	/**
	 * Shows the simulation end time and displays completion popup.
	 * Updates charts with final data. Must be called on JavaFX thread.
	 * 
	 * @param time The simulation end time in seconds
	 */
	@Override
	public void showEndTime(double time) {
		if (ui != null) {
			Platform.runLater(() -> {
				ui.setEndingTime(time);
				// Update charts with final data
				updateChartsAtEnd();
				showSimulationEndPopup(time);
			});
		}
	}
	
	/**
	 * Displays a popup dialog when simulation completes.
	 * 
	 * @param endTime The simulation end time in seconds
	 */
	private void showSimulationEndPopup(double endTime) {
		// Format time as hours, minutes, and seconds (endTime is in seconds)
		int totalSeconds = (int)endTime;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		String timeString = String.format("%d:%02d:%02d", hours, minutes, seconds);
		
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Simulation Complete");
		alert.setHeaderText("Simulation has ended");
		alert.setContentText("The simulation has completed.\n\nEnd Time: " + timeString + " (hours:minutes:seconds)");
		
		alert.showAndWait();
	}

	/**
	 * Visualizes a new customer arriving at a meal station.
	 * 
	 * @param mealType The type of meal the customer wants
	 */
	@Override
	public void visualiseCustomer(simu.model.MealType mealType) {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().newCustomer(mealType));
		}
	}
	
	/**
	 * Visualizes a customer moving from meal station to payment station.
	 * 
	 * @param mealType The customer's meal type
	 * @param paymentType The payment method (SELF_SERVICE or CASHIER)
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	@Override
	public void visualiseCustomerToPayment(simu.model.MealType mealType, simu.model.PaymentType paymentType, int cashierStationNumber) {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().customerToPayment(mealType, paymentType, cashierStationNumber));
		}
	}
	
	/**
	 * Visualizes a customer moving from payment station to coffee station.
	 * 
	 * @param paymentType The payment method used
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	@Override
	public void visualiseCustomerToCoffee(simu.model.PaymentType paymentType, int cashierStationNumber) {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().customerToCoffee(paymentType, cashierStationNumber));
		}
	}
	
	/**
	 * Visualizes a customer exiting from the coffee station.
	 */
	@Override
	public void visualiseCustomerExitFromCoffee() {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().customerExitFromCoffee());
		}
	}
	
	/**
	 * Visualizes a customer exiting from a payment station (without coffee).
	 * 
	 * @param paymentType The payment method used
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	@Override
	public void visualiseCustomerExitFromPayment(simu.model.PaymentType paymentType, int cashierStationNumber) {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().customerExitFromPayment(paymentType, cashierStationNumber));
		}
	}
	
	/**
	 * Updates queue length displays for all service points.
	 * Updates progress bars, labels, and collects data for charts.
	 * 
	 * @param grillQueue Queue length at grill station
	 * @param veganQueue Queue length at vegan station
	 * @param normalQueue Queue length at normal station
	 * @param cashierQueue Queue length at first cashier station
	 * @param cashierQueue2 Queue length at second cashier station
	 * @param selfServiceQueue Queue length at self-service station
	 * @param coffeeQueue Queue length at coffee station
	 */
	@Override
	public void updateQueueDisplays(int grillQueue, int veganQueue, int normalQueue,
	                                int cashierQueue, int cashierQueue2, int selfServiceQueue, int coffeeQueue) {
		Platform.runLater(() -> {
			// Update progress bars (assuming max capacity of 20 for visualization)
			// Progress bars are rotated -90 degrees, so they fill from bottom to top
			double maxCapacity = 20.0;
			
			updateProgressBar(grillQueueProgress, grillQueue, maxCapacity);
			updateProgressBar(veganQueueProgress, veganQueue, maxCapacity);
			updateProgressBar(normalQueueProgress, normalQueue, maxCapacity);
			updateProgressBar(cashierQueueProgress, cashierQueue, maxCapacity);
			updateProgressBar(cashierQueueProgress2, cashierQueue2, maxCapacity);
			updateProgressBar(selfServiceQueueProgress, selfServiceQueue, maxCapacity);
			updateProgressBar(coffeeQueueProgress, coffeeQueue, maxCapacity);
			
			// Update labels
			if (grillQueueLabel != null) {
				grillQueueLabel.setText("Queue: " + grillQueue);
			}
			if (veganQueueLabel != null) {
				veganQueueLabel.setText("Queue: " + veganQueue);
			}
			if (normalQueueLabel != null) {
				normalQueueLabel.setText("Queue: " + normalQueue);
			}
			if (cashierQueueLabel1 != null) {
				cashierQueueLabel1.setText("Queue: " + cashierQueue);
			}
			if (cashierQueueLabel2 != null) {
				cashierQueueLabel2.setText("Queue: " + cashierQueue2);
			}
			if (selfServiceQueueLabel != null) {
				selfServiceQueueLabel.setText("Queue: " + selfServiceQueue);
			}
			if (coffeeQueueLabel != null) {
				coffeeQueueLabel.setText("Queue: " + coffeeQueue);
			}
			
			// Collect data for charts (but don't update display until simulation ends)
			collectChartData(grillQueue, veganQueue, normalQueue, cashierQueue, cashierQueue2, selfServiceQueue, coffeeQueue);
		});
	}
	
	/**
	 * Collects chart data during simulation without updating the display.
	 * Data is stored and displayed only when simulation ends.
	 * 
	 * @param grillQueue Queue length at grill station
	 * @param veganQueue Queue length at vegan station
	 * @param normalQueue Queue length at normal station
	 * @param cashierQueue Queue length at first cashier station
	 * @param cashierQueue2 Queue length at second cashier station
	 * @param selfServiceQueue Queue length at self-service station
	 * @param coffeeQueue Queue length at coffee station
	 */
	private void collectChartData(int grillQueue, int veganQueue, int normalQueue,
	                              int cashierQueue, int cashierQueue2, int selfServiceQueue, int coffeeQueue) {
		// Get current simulation time (in seconds)
		double currentTime = simu.framework.Clock.getInstance().getTime();
		
		// Calculate total queue length (combine both cashier queues for total)
		int totalQueue = grillQueue + veganQueue + normalQueue + cashierQueue + cashierQueue2 + selfServiceQueue + coffeeQueue;
		
		// Collect data points for queue chart at every update to get full simulation data
		// Only skip if time hasn't changed (avoid duplicate points at same time)
		if (currentTime != lastCollectionTime) {
			queueHistory.add(new ChartDataPoint(currentTime, totalQueue));
			
			// Also collect utilization data (queue lengths for each station)
			// Combine both cashier queues for utilization chart (using average or sum)
			int combinedCashierQueue = cashierQueue + cashierQueue2;
			int[] queueLengths = new int[]{grillQueue, veganQueue, normalQueue, combinedCashierQueue, selfServiceQueue, coffeeQueue};
			utilizationHistory.add(new UtilizationDataPoint(currentTime, queueLengths));
			
			lastCollectionTime = currentTime;
		}
		
		// Store latest queue data for utilization chart (combine cashier queues)
		int combinedCashierQueue = cashierQueue + cashierQueue2;
		latestQueueData = new int[]{grillQueue, veganQueue, normalQueue, combinedCashierQueue, selfServiceQueue, coffeeQueue};
	}
	
	/**
	 * Updates charts with collected data when simulation ends.
	 * Displays queue length over time and average station utilization.
	 */
	private void updateChartsAtEnd() {
		// Update queue chart with all collected data
		if (queueChart != null && totalQueueSeries != null) {
			totalQueueSeries.getData().clear();
			for (ChartDataPoint point : queueHistory) {
				totalQueueSeries.getData().add(new XYChart.Data<>(point.time, point.queueLength));
			}
		}
		
		// Update utilization chart with average utilization over entire simulation
		if (utilChart != null && !utilizationHistory.isEmpty()) {
			utilChart.getData().clear();
			
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			
			// Calculate average utilization based on average queue length over entire simulation
			// Utilization = average queue length / max capacity * 100%
			double maxCapacity = 20.0; // Same as progress bar max
			
			// Calculate average queue length for each station over entire simulation
			double[] avgQueueLengths = new double[6];
			for (UtilizationDataPoint point : utilizationHistory) {
				for (int i = 0; i < 6; i++) {
					avgQueueLengths[i] += point.queueLengths[i];
				}
			}
			
			// Calculate averages
			int dataPoints = utilizationHistory.size();
			if (dataPoints > 0) {
				for (int i = 0; i < 6; i++) {
					avgQueueLengths[i] /= dataPoints;
				}
			}
			
			// Add data to chart
			series.getData().add(new XYChart.Data<>("Grill", Math.min(100.0, (avgQueueLengths[0] / maxCapacity) * 100.0)));
			series.getData().add(new XYChart.Data<>("Vegan", Math.min(100.0, (avgQueueLengths[1] / maxCapacity) * 100.0)));
			series.getData().add(new XYChart.Data<>("Normal", Math.min(100.0, (avgQueueLengths[2] / maxCapacity) * 100.0)));
			series.getData().add(new XYChart.Data<>("Cashier", Math.min(100.0, (avgQueueLengths[3] / maxCapacity) * 100.0)));
			series.getData().add(new XYChart.Data<>("Self-Svc", Math.min(100.0, (avgQueueLengths[4] / maxCapacity) * 100.0)));
			series.getData().add(new XYChart.Data<>("Coffee", Math.min(100.0, (avgQueueLengths[5] / maxCapacity) * 100.0)));
			
			utilChart.getData().add(series);
		}
	}
	
	/**
	 * Updates statistics displays with current simulation metrics.
	 * 
	 * @param throughput Throughput in customers per hour
	 * @param avgWaitTime Average wait time in seconds
	 * @param peakQueue Peak queue length observed
	 * @param simTime Current simulation time in seconds
	 */
	@Override
	public void updateStatistics(double throughput, double avgWaitTime, int peakQueue, double simTime) {
		Platform.runLater(() -> {
			// Update throughput (customers per hour)
			if (throughputLabel != null) {
				throughputLabel.setText(String.format("%.1f students/hr", throughput));
			}
			
			// Update average wait time (in seconds)
			if (avgWaitLabel != null) {
				avgWaitLabel.setText(String.format("%.1f s", avgWaitTime));
			}
			
			// Update peak queue length
			if (peakQueueLabel != null) {
				peakQueueLabel.setText(String.valueOf(peakQueue));
			}
			
			// Update simulation time (format as HH:MM:SS)
			if (simTimeLabel != null) {
				int totalSeconds = (int)simTime;
				int hours = totalSeconds / 3600;
				int minutes = (totalSeconds % 3600) / 60;
				int seconds = totalSeconds % 60;
				simTimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
			}
		});
	}
	
	/**
	 * Updates a progress bar to reflect queue length.
	 * Sets progress value and color based on capacity percentage.
	 * 
	 * @param progressBar The progress bar to update
	 * @param queueLength Current queue length
	 * @param maxCapacity Maximum capacity for visualization
	 */
	private void updateProgressBar(javafx.scene.control.ProgressBar progressBar, int queueLength, double maxCapacity) {
		if (progressBar == null) return;
		
		// Ensure progress is calculated correctly, accounting for customers being served
		// queueLength includes customers being served (they're still in the queue)
		double progress = Math.min((double)queueLength / maxCapacity, 1.0);
		
		// Ensure minimum visible progress when there's at least one customer
		// This ensures the progress bar is visible even with just one customer being served
		if (queueLength > 0 && progress < 0.01) {
			progress = 0.01; // Minimum 1% to make it visible
		}
		
		progressBar.setProgress(progress);
		
		// Determine color based on capacity percentage
		// Blue (low): 0-33%, Yellow (medium): 34-66%, Red (high): 67-100%
		String color;
		if (progress <= 0.33) {
			color = "#2196F3"; // Blue
		} else if (progress <= 0.66) {
			color = "#FFC107"; // Yellow/Amber
		} else {
			color = "#F44336"; // Red
		}
		
		// Apply CSS style to change the progress bar color
		progressBar.setStyle("-fx-accent: " + color + ";");
	}
}
