package aco_entities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import util.Globals;

public class Item {

	private boolean toMigrate;
	private int[] resourceDemand;

	public static final int MIPS_MAX = 7000;
	public static final int CORES_MAX = 10;
	public static final int RAM_MAX = 15;
	public static final int STORAGE_MAX = 500;
	public static final int BANDWIDTH_MAX = 4;
	public static final int RUN_TIME_MAX = 10000;

	private Timer endRunTime;
	private Bin deploymentBin;
	private String identifier;
	public boolean startedRunning;

	public Item() {
		startedRunning = false;
		resourceDemand = new int[Resource.values().length];
		setToMigrate(false);
		identifier = "Item" + Globals.getVMID();
	}
	
	public Item(Item item) {
		super();
		setResourceDemand(item.getResourceDemand());
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public int[] getResourceDemand() {
		return resourceDemand;
	}

	public void setResourceDemand(int[] resourceDemand) {
		this.resourceDemand = resourceDemand;
	}

	public int[] getValueSet() {
		int[] demandVector = new int[5];
		for (int r = 0; r < Resource.values().length - 1; r++) {
			demandVector[r] = resourceDemand[r];
		}
		return demandVector;
	}

	public boolean isToMigrate() {
		return toMigrate;
	}

	public void setToMigrate(boolean toMigrate) {
		this.toMigrate = toMigrate;
	}

	public void start() {
		startedRunning = true;
		// endRunTime = new Timer();

		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// destroy VM;
//				System.out.println("Item demand: "
//						+ resourceDemand[Resource.MIPS.getIndex()]);
//				System.out.println("Bin load before:  "
//						+ deploymentBin.getBinLoadVector()[Resource.MIPS
//								.getIndex()]);
				deploymentBin.setBinLoadVector(deploymentBin
						.removeItemLoad(resourceDemand));
//				System.out.println("Item done after "
//						+ resourceDemand[Resource.RUN_TIME.getIndex()] + "!");
//				System.out.println("Bin load after:  "
//						+ deploymentBin.getBinLoadVector()[Resource.MIPS
//								.getIndex()]);
				endRunTime.stop();
			}
		};
		endRunTime = new Timer(resourceDemand[Resource.RUN_TIME.getIndex()], taskPerformer);
		endRunTime.setRepeats(false);
		endRunTime.start();

	}

	/**
	 * @return the deploymentBin
	 */
	public Bin getDeploymentBin() {
		return deploymentBin;
	}

	/**
	 * @param deploymentBin
	 *            the deploymentBin to set
	 */
	public void setDeploymentBin(Bin deploymentBin) {
		this.deploymentBin = deploymentBin;
	}

	/**
	 * @return the endRunTime
	 */
	public Timer getEndRunTime() {
		return endRunTime;
	}

	/**
	 * @param endRunTime the endRunTime to set
	 */
	public void setEndRunTime(Timer endRunTime) {
		this.endRunTime = endRunTime;
	}

	/**
	 * @return the startedRunning
	 */
	public boolean isStartedRunning() {
		return startedRunning;
	}

	/**
	 * @param startedRunning the startedRunning to set
	 */
	public void setStartedRunning(boolean startedRunning) {
		this.startedRunning = startedRunning;
	}
}
