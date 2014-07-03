package pl.edu.agh.informatyka.so.threadsdemo;

public class TableState {	
	private int[] forkOwner;
	private double[] eatingTime;
	 
	public TableState(int size) {
		forkOwner = new int[size];
		eatingTime = new double[size];
		for (int i = 0; i < size; i++) {
			forkOwner[i] = -1;
			eatingTime[i] = 0.0;
		}
	}
	
	public TableState(int[] forkOwner, double[] eatingTime) {		
		this.forkOwner = forkOwner;
		this.eatingTime = eatingTime;
	}
	
	public int getForkOwner(int i) {
		return forkOwner[i];
	}
	
	public double getEatingTime(int i) {
		return eatingTime[i];
	}
	
	public double getEatingTimePercentage(int i) {
		double sum = getEatingTimeSum();
		if (sum == 0.0)
			return 0.0;
		else
			return eatingTime[i] / sum;
	}

	private double getEatingTimeSum() {
		double s = 0.0;
		for (double t: eatingTime)
			s += t;
		return s;
	}
	
}
