package view;

import simu.model.MealType;
import simu.model.PaymentType;

public interface IVisualisation {
	public void clearDisplay();
	public void newCustomer(MealType mealType);
	public void customerToPayment(MealType mealType, PaymentType paymentType);
	public void customerToCoffee(PaymentType paymentType);
	public void customerExitFromCoffee();
	public void customerExitFromPayment(PaymentType paymentType);
}

