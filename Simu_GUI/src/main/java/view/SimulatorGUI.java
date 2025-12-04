package view;

import java.io.IOException;
import controller.Controller;
import controller.IControllerVtoM;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simu.framework.Trace;
import simu.framework.Trace.Level;

public class SimulatorGUI extends Application implements ISimulatorUI {

	// Controller object (UI needs)
	private IControllerVtoM controller;

	// UI Components:
	FXMLLoader loader;


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
            ((Controller) controller).setUI(this);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Simulator");
            primaryStage.setResizable(false);
            primaryStage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
	}

	/* UI interface methods (controller calls) */
	@Override
	public double getTime(){
//		return Double.parseDouble(time.getText());
        //TODO: adapt to new UI
        return 0;
	}

	@Override
	public long getDelay(){
//		return Long.parseLong(delay.getText());
//        TODO: Adapt to new UI
        return 0;
	}

	@Override
	public void setEndingTime(double time) {
//		 this.results.setText(formatter.format(time));
//        TODO: Adapt to new UI
	}

	@Override
	public IVisualisation getVisualisation() {
//		 return display;
//        TODO: Adapt to new UI
        return null;
	}

	/* JavaFX-application (UI) start-up */
	public static void main(String[] args) {
		launch(args);
	}
}
