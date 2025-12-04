package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import simu.model.MealType;

import java.util.ArrayList;
import java.util.List;

public class Visualisation extends Canvas implements IVisualisation {
	private GraphicsContext gc;
	
	// Entry point (blue circle)
	private static final double ENTRY_X = 50;
	private static final double ENTRY_Y = 200;
	private static final double ENTRY_RADIUS = 20;
	
	// Service point positions
	private static final double SERVICE_POINT_RADIUS = 25;
	private static final double SERVICE_Y = 150;
	private static final double GRILL_X = 200;
	private static final double VEGAN_X = 300;
	private static final double NORMAL_X = 400;
	private static final double CASHIER_X = 500;
	private static final double SELF_SERVICE_X = 600;
	private static final double COFFEE_X = 700;
	
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
		// Draw entry point (blue circle)
		gc.setFill(Color.BLUE);
		gc.fillOval(ENTRY_X - ENTRY_RADIUS, ENTRY_Y - ENTRY_RADIUS, 
				   ENTRY_RADIUS * 2, ENTRY_RADIUS * 2);
		gc.setFill(Color.WHITE);
		gc.setFont(new Font(12));
		gc.fillText("Entry", ENTRY_X - 20, ENTRY_Y + ENTRY_RADIUS + 15);
		
		// Draw service points
		drawServicePoint(GRILL_X, SERVICE_Y, Color.ORANGE, "Grill");
		drawServicePoint(VEGAN_X, SERVICE_Y, Color.GREEN, "Vegan");
		drawServicePoint(NORMAL_X, SERVICE_Y, Color.PURPLE, "Normal");
		drawServicePoint(CASHIER_X, SERVICE_Y, Color.RED, "Cashier");
		drawServicePoint(SELF_SERVICE_X, SERVICE_Y, Color.CYAN, "Self-Service");
		drawServicePoint(COFFEE_X, SERVICE_Y, Color.BROWN, "Coffee");
	}
	
	private void drawServicePoint(double x, double y, Color color, String label) {
		gc.setFill(color);
		gc.fillOval(x - SERVICE_POINT_RADIUS, y - SERVICE_POINT_RADIUS,
				   SERVICE_POINT_RADIUS * 2, SERVICE_POINT_RADIUS * 2);
		gc.setFill(Color.BLACK);
		gc.setFont(new Font(10));
		gc.fillText(label, x - label.length() * 3, y + SERVICE_POINT_RADIUS + 15);
	}
	
	private void updateAndDraw() {
		// Clear canvas
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		// Redraw static elements
		drawStaticElements();
		
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
	
	private double getServicePointX(MealType mealType) {
		switch (mealType) {
			case GRILL:
				return GRILL_X;
			case VEGAN:
				return VEGAN_X;
			case NORMAL:
				return NORMAL_X;
			default:
				return NORMAL_X;
		}
	}

	public void clearDisplay() {
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		activeCustomers.clear();
		drawStaticElements();
	}
	
	public void newCustomer(MealType mealType) {
		double targetX = getServicePointX(mealType);
		CustomerAnimation customer = new CustomerAnimation(
			ENTRY_X, ENTRY_Y, targetX, SERVICE_Y, mealType
		);
		activeCustomers.add(customer);
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
			
			// Assign color based on meal type
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
