package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simu.model.MealType;
import simu.model.PaymentType;

import java.util.ArrayList;
import java.util.List;

public class Visualisation extends Canvas implements IVisualisation {
	private GraphicsContext gc;
	
	// Entry point (Arc position from FXML - half circle at top)
	private static final double ENTRY_X = 260.0;
	private static final double ENTRY_Y = 24.0;
	
	// Exit point (Arc position from FXML - half circle at bottom left)
	private static final double EXIT_X = 50.0;
	private static final double EXIT_Y = 496.0;
	
	// Service point positions (center of HBox containers from FXML)
	// Meal stations (top row, y ~ 122)
	private static final double VEGAN_X = 100.0;   // layoutX=36 + width/2
	private static final double VEGAN_Y = 122.0;   // layoutY=94 + height/2
	private static final double NORMAL_X = 268.0;  // layoutX=204 + width/2
	private static final double NORMAL_Y = 122.0;
	private static final double GRILL_X = 442.0;   // layoutX=378 + width/2
	private static final double GRILL_Y = 122.0;
	
	// Payment stations (middle row, y ~ 260)
	private static final double SELF_SERVICE_X = 116.0;  // layoutX=36 + width/2
	private static final double SELF_SERVICE_Y = 260.0;   // layoutY=232 + height/2
	private static final double CASHIER_X = 288.0;       // layoutX=216 + width/2
	private static final double CASHIER_Y = 267.0;       // layoutY=238 + height/2
	
	// Coffee station (bottom row, y ~ 404)
	private static final double COFFEE_X = 275.0;        // layoutX=204 + width/2
	private static final double COFFEE_Y = 404.0;       // layoutY=376 + height/2
	
	// Customer representation
	private static final double CUSTOMER_RADIUS = 8;
	
	// List of active customers being animated
	private List<CustomerAnimation> activeCustomers = new ArrayList<>();
	private AnimationTimer animationTimer;
	
	public Visualisation(int w, int h) {
		super(w, h);
		gc = this.getGraphicsContext2D();
		clearDisplay();
		setupAnimation();
		drawStaticElements();
	}
	
	private void setupAnimation() {
		animationTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				updateAndDraw();
			}
		};
		animationTimer.start();
	}
	
	private void drawStaticElements() {
		// No static elements to draw - the Arc and service point images are already in FXML
		// We only draw customers on top of the existing UI elements
	}
	
	private void updateAndDraw() {
		// Clear only the area where customers are drawn (transparent clear)
		// The background images from FXML will show through
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		// Update and draw active customers
		List<CustomerAnimation> toRemove = new ArrayList<>();
		for (CustomerAnimation customer : activeCustomers) {
			customer.update();
			customer.draw(gc);
			
			if (customer.isComplete()) {
				toRemove.add(customer);
			}
		}
		
		// Remove completed animations
		activeCustomers.removeAll(toRemove);
	}
	
	private double[] getServicePointPosition(MealType mealType) {
		switch (mealType) {
			case GRILL:
				return new double[]{GRILL_X, GRILL_Y};
			case VEGAN:
				return new double[]{VEGAN_X, VEGAN_Y};
			case NORMAL:
				return new double[]{NORMAL_X, NORMAL_Y};  // redundant
			default:
				return new double[]{NORMAL_X, NORMAL_Y};
		}
	}

	public void clearDisplay() {
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		activeCustomers.clear();
	}
	
	public void newCustomer(MealType mealType) {
		double[] targetPos = getServicePointPosition(mealType);
		CustomerAnimation customer = new CustomerAnimation(
			ENTRY_X, ENTRY_Y, targetPos[0], targetPos[1], mealType
		);
		activeCustomers.add(customer);
	}
	
	public void customerToPayment(MealType mealType, PaymentType paymentType) {
		double[] startPos = getServicePointPosition(mealType);
		double[] targetPos = getPaymentPosition(paymentType);
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], targetPos[0], targetPos[1], mealType
		);
		activeCustomers.add(customer);
	}
	
	public void customerToCoffee(PaymentType paymentType) {
		double[] startPos = getPaymentPosition(paymentType);
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], COFFEE_X, COFFEE_Y, null
		);
		activeCustomers.add(customer);
	}
	
	public void customerExitFromCoffee() {
		// Exit from coffee station - move to exit circle at bottom
		CustomerAnimation customer = new CustomerAnimation(
			COFFEE_X, COFFEE_Y, EXIT_X, EXIT_Y, null
		);
		activeCustomers.add(customer);
	}
	
	public void customerExitFromPayment(PaymentType paymentType) {
		double[] startPos = getPaymentPosition(paymentType);
		// Exit point - move to exit circle at bottom
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], EXIT_X, EXIT_Y, null
		);
		activeCustomers.add(customer);
	}
	
	private double[] getPaymentPosition(PaymentType paymentType) {
		if (paymentType == PaymentType.SELF_SERVICE) {
			return new double[]{SELF_SERVICE_X, SELF_SERVICE_Y};
		} else {
			return new double[]{CASHIER_X, CASHIER_Y};
		}
	}
	
	// Inner class for customer animation
	private class CustomerAnimation {
		private double x, y;
		private double targetX, targetY;
		private double speed = 2.0; // pixels per frame
		private Color customerColor;
		
		public CustomerAnimation(double startX, double startY, double targetX, double targetY, MealType mealType) {
			this.x = startX;
			this.y = startY;
			this.targetX = targetX;
			this.targetY = targetY;
			
			// Assign color based on meal type (if null, use default color)
			if (mealType != null) {
				switch (mealType) {
					case GRILL:
						customerColor = Color.ORANGERED;
						break;
					case VEGAN:
						customerColor = Color.DARKGREEN;
						break;
					case NORMAL:
						customerColor = Color.DARKVIOLET;
						break;
					default:
						customerColor = Color.RED;
				}
			} else {
				// Default color for customers without meal type (e.g., after payment)
				customerColor = Color.DARKBLUE;
			}
		}
		
		public void update() {
			double dx = targetX - x;
			double dy = targetY - y;
			double distance = Math.sqrt(dx * dx + dy * dy);
			
			if (distance > speed) {
				x += (dx / distance) * speed;
				y += (dy / distance) * speed;
			} else {
				x = targetX;
				y = targetY;
			}
		}
		
		public void draw(GraphicsContext gc) {
			gc.setFill(customerColor);
			gc.fillOval(x - CUSTOMER_RADIUS, y - CUSTOMER_RADIUS,
					   CUSTOMER_RADIUS * 2, CUSTOMER_RADIUS * 2);
		}
		
		public boolean isComplete() {
			return Math.abs(x - targetX) < 1 && Math.abs(y - targetY) < 1;
		}
	}
}
