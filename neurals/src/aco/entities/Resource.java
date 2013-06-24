package aco.entities;

public enum Resource {
	CPU(0), RAM(1), STORAGE(2), NETWORK_TRANSFER_SPEED(3), RUN_TIME(4);

	private int index;

	private Resource(int c) {
		setIndex(c);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
