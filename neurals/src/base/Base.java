package base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.GraphCSVWriter;
import util.InputReader;
import aco.ACOAlgorithm;
import aco_entities.Bin;
import aco_entities.Item;
import aco_entities.Resource;

public class Base {
	private final static ACOAlgorithm aco = new ACOAlgorithm();
	private Timer predictionTimer;
	private Timer acoTimer;
	private final Lock lock = new ReentrantLock();
	private List<PredictionBox> pboxes;
	private int predEpoch, acoEpoch;
	private PredictionQueue itemsQueue;
	GraphCSVWriter graphCSV;

	private static int PREDICTION_INTERVAL = 5000; // ms
	private static int ACO_INTERVAL = 3000; // ms

	public static List<Item> overflowItems;
	private List<Item> leftoverItems;
	private List<Item> itemsToMigrate;

	public Base() {
		itemsToMigrate = new ArrayList<Item>();
		leftoverItems = new ArrayList<Item>();
		graphCSV = new GraphCSVWriter("vms.csv");

		itemsQueue = new PredictionQueue();

		LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader
				.readData();

		ACOAlgorithm.NB_OF_BINS = numbers.get(0);
		aco.setNB_OF_ITEMS(numbers.get(1));

		int[] resourceCapacity = new int[Resource.values().length - 1];
		resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(2);
		resourceCapacity[Resource.CORES.getIndex()] = numbers.get(3);
		resourceCapacity[Resource.RAM.getIndex()] = numbers.get(4);
		resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(5);
		resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers.get(6);

		aco.initalizeBinsData(resourceCapacity);
		List<Item> items = new ArrayList<Item>();
		pboxes = new ArrayList<PredictionBox>();
		Item i;

		for (int s = 7; s < numbers.size(); s += 6) {
			int[] resourceDemand = new int[Resource.values().length];
			resourceDemand[Resource.MIPS.getIndex()] = numbers.get(s);
			resourceDemand[Resource.CORES.getIndex()] = numbers.get(s + 1);
			resourceDemand[Resource.RAM.getIndex()] = numbers.get(s + 2);
			resourceDemand[Resource.STORAGE.getIndex()] = numbers.get(s + 3);
			resourceDemand[Resource.BANDWIDTH.getIndex()] = numbers.get(s + 4);
			resourceDemand[Resource.RUN_TIME.getIndex()] = numbers.get(s + 5);

			i = new Item();
			i.setResourceDemand(resourceDemand);
			items.add(i);

			PredictionBox pboxBuff = new PredictionBox(i, resourceCapacity,
					graphCSV);
			pboxes.add(pboxBuff);
		}

		predEpoch = 0;
		acoEpoch = 0;
		overflowItems = new ArrayList<Item>();
	}

	public void start() {
		predictionTimer = new Timer();
		predictionTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updatePrediction();
			}
		}, 2 * 1000, PREDICTION_INTERVAL);

		acoTimer = new Timer();
		acoTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateACO();
			}
		}, 2 * 1000, ACO_INTERVAL);
	}

	public void stop() {
		if (predictionTimer != null)
			predictionTimer.cancel();
		if (acoTimer != null)
			acoTimer.cancel();
	}

	private void updatePrediction() {
		lock.lock();
		List<Item> items = new ArrayList<Item>();
		System.out.println("---------------------------------");
		System.out.println("Prediction epoch " + predEpoch);
		System.out.println("---------------------------------");

		// update all the items via neural nets
		if (aco.getItems() != null && aco.getItems().size() > 0) {
			items = aco.getItems();
		}
		checkItemsTimer();
		System.out.println("Items size: " + items.size());
		for (int i = 0; i < pboxes.size(); i++) {
			pboxes.get(i).Update();
			if (aco.getAvailableResources() != null)
				items.add(new Item(pboxes.get(i).getItem()));
		}
		itemsQueue.add(items);

		if (predEpoch == 5)
			predictionTimer.cancel();
		predEpoch++;
		lock.unlock();
	}

	private void updateACO() {
		lock.lock();
		List<Item> items = new ArrayList<Item>();
		System.out.println("---------------------------------");
		System.out.println("ACO epoch " + acoEpoch);
		System.out.println("---------------------------------");

		if (itemsQueue.hasItems()) {
			List<Item> queueFront = itemsQueue.popFront();
			aco.setItems(queueFront);
		} else {
			items = aco.getItems();
			checkItemsTimer();
			aco.setItems(items);
		}
		int[] originalBinIds = null;
		if (itemsToMigrate != null && itemsToMigrate.size() > 0) {
			aco.getLeftoverItems().removeAll(itemsToMigrate);
			originalBinIds = new int[itemsToMigrate.size()];
			int i = 0;
			for (Item item : itemsToMigrate) {
				// System.out.println("Items to migrate before aco runs : "
				// + item.getResourceDemand()[Resource.MIPS.getIndex()]
				// + " in bin " + item.getDeploymentBin().getId());
				originalBinIds[i] = item.getDeploymentBin().getId();
				i++;
			}
		}
		for (Item item : aco.getItems()) {
			System.out.println("ACO items: "
					+ item.getResourceDemand()[Resource.MIPS.getIndex()]);
		}
		aco.setNB_OF_ITEMS(aco.getItems().size());
		//
		// System.out.println("NB ITEMS " + aco.getNB_OF_ITEMS());

		aco.init();
		aco.run();
		boolean flag = true;
		for (Item item : itemsToMigrate) {
			if (overflowItems.contains(item)) {
				System.out.println("Redo placement");
				flag = false;
				break;
			}
		}
		if (flag) {
			items = aco.getItems();
			int binIdBeforeMigrate, binIdAfterMigrate;

			if (items != null && items.size() > 0
					&& aco.getGlobalBestSolution() != null) {

				int[][] globalBestSolution = aco.getGlobalBestSolution();
				List<Bin> bins = aco.getBins();
				for (int row = 0; row < aco.getNB_OF_ITEMS(); row++) {
					for (int col = 0; col < ACOAlgorithm.NB_OF_BINS; col++) {

						if (globalBestSolution[row][col] != 0) {
							// deploy VM in corresponding Machine
							System.out
									.println("Item " + row + " in bin " + col);
							items.get(row).setDeploymentBin(bins.get(col));
							if (items.get(row).getEndRunTime() == null
									|| (items.get(row).getEndRunTime() != null && !items
											.get(row).getEndRunTime()
											.isRunning())) {
								items.get(row).start();
							}
							globalBestSolution[row][col] = 0;
							break;
						}
					}
				}
			}
			itemsToMigrate = aco.getItemsToMigrate();
			int i = 0;
			for (Item item : itemsToMigrate) {
				binIdBeforeMigrate = originalBinIds[i++];
				binIdAfterMigrate = items.get(items.indexOf(item))
						.getDeploymentBin().getId();

				if (binIdAfterMigrate != binIdBeforeMigrate) {
					items.get(items.indexOf(item)).getEndRunTime().stop();
					// delete + redeploy VM to NEW BIN
					System.out.println("Migrate item " + items.indexOf(item)
							+ " from " + binIdBeforeMigrate + " to "
							+ binIdAfterMigrate);
					items.get(items.indexOf(item)).getEndRunTime().start();
				} else {
					// no need to migrate
					aco.getLeftoverItems().add(item);
				}
			}
			aco.setItems(items);
		}
		if (acoEpoch == 15)
			stop();
		acoEpoch++;
		lock.unlock();
	}

	public void checkItemsTimer() {
		List<Item> items = new ArrayList<Item>();
		items = aco.getItems();
		if (leftoverItems != null && leftoverItems.size() > 0)
			leftoverItems.clear();
		if (itemsToMigrate != null && itemsToMigrate.size() > 0)
			itemsToMigrate.clear();
		int row = 0;
		while (row < items.size() && items.size() > 0) {
			if (items.get(row).getEndRunTime() != null) {
				if (!items.get(row).getEndRunTime().isRunning()) {
					System.out
							.println("Remove item "
									+ items.get(row).getResourceDemand()[Resource.MIPS
											.getIndex()]
									+ " after "
									+ items.get(row).getResourceDemand()[Resource.RUN_TIME
											.getIndex()]);
					items.remove(row);
					if (row == (items.size() + 1))
						row--;
				} else {
					leftoverItems.add(items.get(row));
					if (items.get(row).getDeploymentBin().isMigrateTrigger() == true) {
						itemsToMigrate.add(items.get(row));
						System.out
								.println("Item "
										+ row
										+ " in bin "
										+ items.get(row).getDeploymentBin()
												.getId()
										+ " set to migrate with "
										+ items.get(row).getResourceDemand()[Resource.MIPS
												.getIndex()]);
					}
					row++;
				}
			} else { 
				row++;
			}
		}
		aco.setItems(items);
		aco.setLeftoverItems(leftoverItems);
		aco.setItemsToMigrate(itemsToMigrate);
	}

}
