package controller;

import simu.model.MealType;
import simu.model.PaymentType;

/* interface for the engine */
public interface IControllerMtoV {
		public void showEndTime(double time);
		public void visualiseCustomer(MealType mealType);
	public void visualiseCustomerToPayment(MealType mealType, PaymentType paymentType, int cashierStationNumber);
	public void visualiseCustomerToCoffee(PaymentType paymentType, int cashierStationNumber);
	public void visualiseCustomerExitFromCoffee();
	public void visualiseCustomerExitFromPayment(PaymentType paymentType, int cashierStationNumber);
		public void updateQueueDisplays(int grillQueue, int veganQueue, int normalQueue,
		                                int cashierQueue, int cashierQueue2, int selfServiceQueue, int coffeeQueue);
	public void updateStatistics(double throughput, double avgWaitTime, int peakQueue, double simTime);
}
