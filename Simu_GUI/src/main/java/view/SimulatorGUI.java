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

public class SimulatorGUI extends Application implements ISimulatorUI {

	// Controller object (UI needs)
	private IControllerVtoM controller;

	// UI Components:
	FXMLLoader loader;
	private IVisualisation visualisation;


	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller();
	}

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

	/* UI interface methods (controller calls) */
	@Override
	public double
    getTime(){
        // This is now handled directly in Controller.startSimulation()
        return 0;
	}

	@Override
	public long getDelay(){
        // Default delay is set in Controller.startSimulation()
        return 100;
	}

	@Override
	public void setEndingTime(double time) {
        // Update simTimeLabel with formatted time through controller
        // This can be enhanced later to update the label directly
	}

	@Override
	public IVisualisation getVisualisation() {
        return visualisation;
	}

	/* JavaFX-application (UI) start-up */
	public static void main(String[] args) {
		launch(args);
	}
}
