package aco.entities;

public class Bin {

	public static final int IS_OFF = 0;
	public static final int IS_ON = 1;

	private int[] resourceCapacity;
	private int[] binLoadVector;
	private int id;
	private boolean migrateTrigger;
	private int naturalId;
	
	private int status;

	public Bin() {
		resourceCapacity = new int[Resource.values().length];
		binLoadVector = new int[Resource.values().length];
		migrateTrigger = false;
	}
	
	public int[] removeItemLoad(int[] resourceDemand) {
		for (int r = 0 ; r < Resource.values().length - 1; r++) {
			binLoadVector[r] = binLoadVector[r] - resourceDemand[r];
		}
		return binLoadVector;
	}

	public int[] getResourceCapacity() {
		return resourceCapacity;
	}

	public void setResourceCapacity(int[] resourceCapacity) {
		this.resourceCapacity = resourceCapacity;
	}

	public int[] getValueSet() {
		int[] demandVector = new int[Resource.values().length - 1];
		for (int r = 0 ; r < Resource.values().length - 1; r++) {
			demandVector[r] = resourceCapacity[r];
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

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the migrateTrigger
	 */
	public boolean isMigrateTrigger() {
		return migrateTrigger;
	}

	/**
	 * @param migrateTrigger the migrateTrigger to set
	 */
	public void setMigrateTrigger(boolean migrateTrigger) {
		this.migrateTrigger = migrateTrigger;
	}

	public int getNaturalId() {
		return naturalId;
	}

	public void setNaturalId(int naturalId) {
		this.naturalId = naturalId;
	}
}
