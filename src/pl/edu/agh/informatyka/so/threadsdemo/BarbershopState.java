package pl.edu.agh.informatyka.so.threadsdemo;

import java.util.ArrayList;
import java.util.List;

public class BarbershopState {
	private int chairCnt = 0;
	private int customerCnt = 0;	
	private int[] isWaiting = null;
	private int[] isSitting = null;
	private int currentCustomer = -1;	
	
	public BarbershopState(int chairCnt, int customerCnt) {
		this.chairCnt = chairCnt;
		this.customerCnt = customerCnt;
		this.isWaiting = new int[customerCnt];
		this.isSitting = new int[customerCnt];
		this.currentCustomer = -1;
	}
	
	public BarbershopState(int chairCnt, int customerCnt, int[] isWaiting, int[] isSitting,
			int currentCustomer) {		
		this.chairCnt = chairCnt;
		this.customerCnt = customerCnt;
		this.isWaiting = isWaiting;
		this.isSitting = isSitting;
		this.currentCustomer = currentCustomer;
	}

	public boolean anybodyGettingAHaircut() {
		return currentCustomer >= 0;
	}
	
	public boolean isWaiting(int i) {
		return isWaiting[i] != 0;
	}
	
	public int getCurrentCustomer() {
		return currentCustomer;
	}
	
	public List<Integer> getSittingCustomers() {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < customerCnt; i++)
			if (isSitting(i))
				res.add(i);
		return res;
	}
	
	public boolean isSitting(int i) {
		return isSitting[i] != 0;
	}

	public int getCustomerCount() {
		return customerCnt;
	}

	public int getChairCount() {		
		return chairCnt;
	}

	public int getSittingCount() {		
		return getSittingCustomers().size();
	}	
	
}
