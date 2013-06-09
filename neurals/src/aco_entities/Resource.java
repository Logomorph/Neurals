package aco_entities;

public enum Resource {
	MIPS(0), CORES(1), RAM(2), STORAGE(3), BANDWIDTH(4), RUN_TIME(5);

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
