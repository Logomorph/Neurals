package aco_entities;
public class Item {

	private boolean toMigrate;	
	private int[] resourceDemand;
	
	public Item() {
		resourceDemand = new int[Resource.values().length];
		setToMigrate(false);
	}

	public int[] getResourceDemand() {
		return resourceDemand;
	}

	public void setResourceDemand(int[] resourceDemand) {
		this.resourceDemand = resourceDemand;
	}

	public int[] getValueSet() {
		int[] demandVector = new int[5];
		for (Resource r : Resource.values()) {
			demandVector[r.getIndex()] = resourceDemand[r.getIndex()];
		}
		return demandVector;
	}

	public boolean isToMigrate() {
		return toMigrate;
	}

	public void setToMigrate(boolean toMigrate) {
		this.toMigrate = toMigrate;
	}
}
