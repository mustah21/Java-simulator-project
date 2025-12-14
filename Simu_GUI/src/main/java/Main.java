import view.SimulatorGUI;

/**
 * Main entry point for the Cafeteria Simulator application.
 * Delegates to SimulatorGUI to launch the JavaFX application.
 * 
 * <p>JavaFX Installation Notes (from https://openjfx.io/openjfx-docs/):
 * <ul>
 *   <li>Open Module Settings</li>
 *   <li>Libraries, add from Maven: openjfx.javafx.fxml:19.0.2</li>
 *   <li>Run Configuration Edit: Modify Options/Add VM options</li>
 *   <li>Add: --module-path "lib" --add-modules javafx.controls,javafx.fxml</li>
 * </ul>
 * 
 * @author Group 8
 * @version 1.0
 */
public class Main { // Simulator using Java FX
	/**
	 * Main method that launches the JavaFX application.
	 * 
	 * @param args Command line arguments (passed to JavaFX Application)
	 */
	public static void main(String[] args) {
		SimulatorGUI.main(args);
	}
}
