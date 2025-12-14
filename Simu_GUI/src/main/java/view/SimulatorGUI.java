package view;

import java.io.IOException;
import controller.Controller;
import controller.IControllerVtoM;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import simu.framework.Trace;
import simu.framework.Trace.Level;

/**
 * Main JavaFX application class for the cafeteria simulator GUI.
 * Initializes the UI, loads FXML, and sets up the visualization canvas.
 * 
 * @author Group 8
 * @version 1.0
 */
public class SimulatorGUI extends Application implements ISimulatorUI {

	/** Controller for handling UI interactions */
	private IControllerVtoM controller;

	/** FXML loader for loading the UI layout */
	FXMLLoader loader;
	/** Visualization component for displaying customer animations */
	private IVisualisation visualisation;

	/**
	 * Initializes the application before the UI is shown.
	 * Sets up trace level and creates the controller.
	 */
	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller();
	}

	/**
	 * Starts the JavaFX application and sets up the main window.
	 * Loads FXML, creates the scene, and sets up visualization.
	 * 
	 * @param primaryStage The primary stage for this application
	 * @throws IOException if FXML file cannot be loaded
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		// UI creation
        try{
            loader = new FXMLLoader(getClass().getResource("/SimulatorUI.fxml"));
            loader.setController(controller);

            Scene scene = new Scene(loader.load());

            // Set the UI reference in the controller after loading
            Controller ctrl = (Controller) controller;
            ctrl.setUI(this);
            
            // Set up visualization canvas
            setupVisualisation(ctrl);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Simulator");
            primaryStage.setResizable(false);
            primaryStage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	/**
	 * Sets up the visualization canvas on the simulation pane.
	 * Creates a Visualisation instance and binds it to the pane size.
	 * 
	 * @param ctrl The controller instance
	 */
	private void setupVisualisation(Controller ctrl) {
		// Get the simulation canvas pane from the controller
		Pane simulationCanvas = ctrl.getSimulationCanvas();
		if (simulationCanvas != null) {
			// Create visualization canvas
			Visualisation vis = new Visualisation((int)simulationCanvas.getWidth(), (int)simulationCanvas.getHeight());
			visualisation = vis;
			
			// Bind canvas size to pane size
			vis.widthProperty().bind(simulationCanvas.widthProperty());
			vis.heightProperty().bind(simulationCanvas.heightProperty());
			
			// Add visualization to the pane
			simulationCanvas.getChildren().add(0, vis);
		}
	}

	/**
	 * Gets the simulation time (deprecated - handled in Controller).
	 * 
	 * @return Always returns 0 (functionality moved to Controller)
	 */
	@Override
	public double getTime(){
        // This is now handled directly in Controller.startSimulation()
        return 0;
	}

	/**
	 * Gets the simulation delay (deprecated - handled in Controller).
	 * 
	 * @return Default delay of 100ms
	 */
	@Override
	public long getDelay(){
        // Default delay is set in Controller.startSimulation()
        return 100;
	}

	/**
	 * Sets the ending time (deprecated - handled in Controller).
	 * 
	 * @param time The simulation end time
	 */
	@Override
	public void setEndingTime(double time) {
        // Update simTimeLabel with formatted time through controller
        // This can be enhanced later to update the label directly
	}

	/**
	 * Gets the visualization component.
	 * 
	 * @return The IVisualisation instance
	 */
	@Override
	public IVisualisation getVisualisation() {
        return visualisation;
	}

	/**
	 * Main entry point for the JavaFX application.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
