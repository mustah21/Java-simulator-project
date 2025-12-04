package controller;

import simu.model.MealType;
import simu.model.PaymentType;

/* interface for the engine */
public interface IControllerMtoV {
		public void showEndTime(double time);
		public void visualiseCustomer(MealType mealType);
		public void visualiseCustomerToPayment(MealType mealType, PaymentType paymentType);
		public void visualiseCustomerToCoffee(PaymentType paymentType);
		public void visualiseCustomerExitFromCoffee();
		public void visualiseCustomerExitFromPayment(PaymentType paymentType);
		public void updateQueueDisplays(int grillQueue, int veganQueue, int normalQueue,
		                                int cashierQueue, int selfServiceQueue, int coffeeQueue);
	public void updateStatistics(double throughput, double avgWaitTime, int peakQueue, double simTime);
}
