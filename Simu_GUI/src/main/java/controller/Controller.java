package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import simu.framework.IEngine;
import simu.model.MyEngine;
import view.ISimulatorUI;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements IControllerVtoM, IControllerMtoV, Initializable {
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
	
	public Controller() {
		// No-arg constructor required for FXML
	}
	
	public void setUI(ISimulatorUI ui) {
		this.ui = ui;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Wire up arrival slider to update label
		if (arrivalSlider != null && arrivalValue != null) {
			arrivalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
				arrivalValue.setText(String.valueOf(newVal.intValue()));
			});
		}
		
		// Wire up Run button to print GUI data
		if (runBtn2 != null) {
			runBtn2.setOnAction(e -> printGUIData());
		}
	}
	
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

	/* Engine control: */
	@Override
	public void startSimulation() {
		if (ui == null) return;
		engine = new MyEngine(this); // new Engine thread is created for every simulation
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());
		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}
	
	@Override
	public void decreaseSpeed() { // hidastetaan moottorisäiettä
		if (engine != null) {
			engine.setDelay((long)(engine.getDelay()*1.10));
		}
	}

	@Override
	public void increaseSpeed() { // nopeutetaan moottorisäiettä
		if (engine != null) {
			engine.setDelay((long)(engine.getDelay()*0.9));
		}
	}


	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		if (ui != null) {
			Platform.runLater(()->ui.setEndingTime(time));
		}
	}

	@Override
	public void visualiseCustomer(simu.model.MealType mealType) {
		if (ui != null) {
			Platform.runLater(() -> ui.getVisualisation().newCustomer(mealType));
		}
	}
}
