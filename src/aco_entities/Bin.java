package aco_entities;

public class Bin {

	public static final int IS_OFF = 0;
	public static final int IS_ON = 1;

	private int[] resourceCapacity;
	private int[] binLoadVector;
	
	private int status;

	public Bin() {
		resourceCapacity = new int[Resource.values().length];
		binLoadVector = new int[Resource.values().length];
	}

	public int[] getResourceCapacity() {
		return resourceCapacity;
	}

	public void setResourceCapacity(int[] resourceCapacity) {
		this.resourceCapacity = resourceCapacity;
	}

	public int[] getValueSet() {
		int[] demandVector = new int[5];
		for (Resource r : Resource.values()) {
			demandVector[r.getIndex()] = resourceCapacity[r.getIndex()];
		}
		return demandVector;
	}

	public void turnOff() {
		this.setStatus(IS_OFF);
	}
	
	public void turnON() {
		this.setStatus(IS_ON);
	}
	
	public int[] getBinLoadVector() {
		return binLoadVector;
	}

	public void setBinLoadVector(int[] binLoadVector) {
		this.binLoadVector = binLoadVector;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
