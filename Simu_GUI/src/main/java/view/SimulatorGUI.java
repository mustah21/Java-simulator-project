package view;

import java.io.IOException;
import java.text.DecimalFormat;
import controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import simu.framework.Trace;
import simu.framework.Trace.Level;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.text.DecimalFormat;

public class SimulatorGUI extends Application implements ISimulatorUI {

	// Controller object (UI needs)
	private IControllerVtoM controller;

	// UI Components:
	FXMLLoader loader;


	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// UI creation
        try{

            loader = new FXMLLoader(getClass().getResource("/simulatorUI.fxml"));
        loader.setController(controller);

        Scene scene = new Scene(loader.load());
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
		 DecimalFormat formatter = new DecimalFormat("#0.00");
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
