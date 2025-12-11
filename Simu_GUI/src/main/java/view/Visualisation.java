package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simu.model.MealType;
import simu.model.PaymentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Canvas component for visualizing customer movements in the cafeteria simulation.
 * Displays animated customers moving between service points using JavaFX Canvas.
 * 
 * @author Group 8
 * @version 1.0
 */
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
	private static final double CASHIER_X = 288.0;       // layoutX=216 + width/2 (first cashier)
	private static final double CASHIER_Y = 267.0;       // layoutY=238 + height/2
	private static final double CASHIER2_X = 448.0;      // layoutX=378 + width/2 (second cashier, width=140)
	private static final double CASHIER2_Y = 267.0;      // layoutY=238 + height/2
	
	// Coffee station (bottom row, y ~ 404)
	private static final double COFFEE_X = 275.0;        // layoutX=204 + width/2
	private static final double COFFEE_Y = 404.0;       // layoutY=376 + height/2
	
	// Customer representation
	private static final double CUSTOMER_RADIUS = 8;
	
	/** List of customers currently being animated */
	private List<CustomerAnimation> activeCustomers = new ArrayList<>();
	/** Animation timer for updating customer positions */
	private AnimationTimer animationTimer;
	
	/**
	 * Constructs a new Visualisation canvas with the specified dimensions.
	 * 
	 * @param w Width of the canvas
	 * @param h Height of the canvas
	 */
	public Visualisation(int w, int h) {
		super(w, h);
		gc = this.getGraphicsContext2D();
		clearDisplay();
		setupAnimation();
		drawStaticElements();
	}
	
	/**
	 * Sets up the animation timer to continuously update and draw customers.
	 */
	private void setupAnimation() {
		animationTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				updateAndDraw();
			}
		};
		animationTimer.start();
	}
	
	/**
	 * Draws static elements (not used - static elements are in FXML).
	 */
	private void drawStaticElements() {
		// No static elements to draw - the Arc and service point images are already in FXML
		// We only draw customers on top of the existing UI elements
	}
	
	/**
	 * Updates customer positions and redraws the canvas.
	 * Called continuously by the animation timer.
	 */
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
	
	/**
	 * Gets the screen coordinates for a meal service point.
	 * 
	 * @param mealType The type of meal station
	 * @return Array with [x, y] coordinates
	 */
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

	/**
	 * Clears the visualization display and removes all customer animations.
	 */
	public void clearDisplay() {
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		activeCustomers.clear();
	}
	
	/**
	 * Creates a new customer animation from entry point to meal station.
	 * 
	 * @param mealType The type of meal station the customer is going to
	 */
	public void newCustomer(MealType mealType) {
		double[] targetPos = getServicePointPosition(mealType);
		CustomerAnimation customer = new CustomerAnimation(
			ENTRY_X, ENTRY_Y, targetPos[0], targetPos[1], mealType
		);
		activeCustomers.add(customer);
	}
	
	/**
	 * Creates a customer animation from meal station to payment station.
	 * 
	 * @param mealType The customer's meal type
	 * @param paymentType The payment method (SELF_SERVICE or CASHIER)
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	public void customerToPayment(MealType mealType, PaymentType paymentType, int cashierStationNumber) {
		double[] startPos = getServicePointPosition(mealType);
		double[] targetPos = getPaymentPosition(paymentType, cashierStationNumber);
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], targetPos[0], targetPos[1], mealType
		);
		activeCustomers.add(customer);
	}
	
	/**
	 * Creates a customer animation from payment station to coffee station.
	 * 
	 * @param paymentType The payment method used
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	public void customerToCoffee(PaymentType paymentType, int cashierStationNumber) {
		double[] startPos = getPaymentPosition(paymentType, cashierStationNumber);
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], COFFEE_X, COFFEE_Y, null
		);
		activeCustomers.add(customer);
	}
	
	/**
	 * Creates a customer animation exiting from coffee station to exit point.
	 */
	public void customerExitFromCoffee() {
		// Exit from coffee station - move to exit circle at bottom
		CustomerAnimation customer = new CustomerAnimation(
			COFFEE_X, COFFEE_Y, EXIT_X, EXIT_Y, null
		);
		activeCustomers.add(customer);
	}
	
	/**
	 * Creates a customer animation exiting from payment station to exit point.
	 * 
	 * @param paymentType The payment method used
	 * @param cashierStationNumber The cashier station number (0=self-service, 1=cashier1, 2=cashier2)
	 */
	public void customerExitFromPayment(PaymentType paymentType, int cashierStationNumber) {
		double[] startPos = getPaymentPosition(paymentType, cashierStationNumber);
		// Exit point - move to exit circle at bottom
		CustomerAnimation customer = new CustomerAnimation(
			startPos[0], startPos[1], EXIT_X, EXIT_Y, null
		);
		activeCustomers.add(customer);
	}
	
	/**
	 * Gets the screen coordinates for a payment service point.
	 * 
	 * @param paymentType The payment method (SELF_SERVICE or CASHIER)
	 * @param cashierStationNumber The cashier station number (1 or 2)
	 * @return Array with [x, y] coordinates
	 */
	private double[] getPaymentPosition(PaymentType paymentType, int cashierStationNumber) {
		if (paymentType == PaymentType.SELF_SERVICE) {
			return new double[]{SELF_SERVICE_X, SELF_SERVICE_Y};
		} else {
			// Route to the correct cashier station (1 or 2)
			if (cashierStationNumber == 2) {
				return new double[]{CASHIER2_X, CASHIER2_Y};
			} else {
				return new double[]{CASHIER_X, CASHIER_Y};
			}
		}
	}
	
	/**
	 * Inner class representing a single customer animation.
	 * Handles movement from start to target position with color coding by meal type.
	 */
	private class CustomerAnimation {
		/** Current X position */
		private double x;
		/** Current Y position */
		private double y;
		/** Target X position */
		private double targetX;
		/** Target Y position */
		private double targetY;
		/** Movement speed in pixels per frame */
		private double speed = 2.0;
		/** Color of the customer circle (based on meal type) */
		private Color customerColor;
		
		/**
		 * Constructs a new customer animation.
		 * 
		 * @param startX Starting X coordinate
		 * @param startY Starting Y coordinate
		 * @param targetX Target X coordinate
		 * @param targetY Target Y coordinate
		 * @param mealType Meal type for color coding (null for default color)
		 */
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
		
		/**
		 * Updates the customer's position towards the target.
		 * Moves at a constant speed towards the target coordinates.
		 */
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
		
		/**
		 * Draws the customer as a colored circle at current position.
		 * 
		 * @param gc The GraphicsContext to draw on
		 */
		public void draw(GraphicsContext gc) {
			gc.setFill(customerColor);
			gc.fillOval(x - CUSTOMER_RADIUS, y - CUSTOMER_RADIUS,
					   CUSTOMER_RADIUS * 2, CUSTOMER_RADIUS * 2);
		}
		
		/**
		 * Checks if the animation has reached its target.
		 * 
		 * @return true if within 1 pixel of target, false otherwise
		 */
		public boolean isComplete() {
			return Math.abs(x - targetX) < 1 && Math.abs(y - targetY) < 1;
		}
	}
}
