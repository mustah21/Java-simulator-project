package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import simu.model.MealType;
import simu.model.PaymentType;

public class Visualisation2 extends Canvas implements IVisualisation {
	private GraphicsContext gc;
	int customerCount = 0;

	public Visualisation2(int w, int h) {
		super(w, h);
		gc = this.getGraphicsContext2D();
		clearDisplay();
	}

	public void clearDisplay() {
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	public void newCustomer(MealType mealType) {
		customerCount++;
		
		gc.setFill(Color.YELLOW);					// first erase old text
		gc.fillRect(100,80, 130, 20);
		gc.setFill(Color.RED);						// then write new text
		gc.setFont(new Font(20));
		gc.fillText("Customer " + customerCount + " (" + mealType + ")", 100, 100);
	}
	
	@Override
	public void customerToPayment(MealType mealType, PaymentType paymentType) {
		// Stub implementation for Visualisation2
	}
	
	@Override
	public void customerToCoffee(PaymentType paymentType) {
		// Stub implementation for Visualisation2
	}
	
	@Override
	public void customerExitFromCoffee() {
		// Stub implementation for Visualisation2
	}
	
	@Override
	public void customerExitFromPayment(PaymentType paymentType) {
		// Stub implementation for Visualisation2
	}
}
