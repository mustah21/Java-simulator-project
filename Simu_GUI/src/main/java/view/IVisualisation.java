package view;

import simu.model.MealType;
import simu.model.PaymentType;

public interface IVisualisation {
	public void clearDisplay();
	public void newCustomer(MealType mealType);
	public void customerToPayment(MealType mealType, PaymentType paymentType, int cashierStationNumber);
	public void customerToCoffee(PaymentType paymentType, int cashierStationNumber);
	public void customerExitFromCoffee();
	public void customerExitFromPayment(PaymentType paymentType, int cashierStationNumber);
}

