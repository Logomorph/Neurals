package base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import util.GraphCSVWriter;
import util.InputReader;
import aco.ACOAlgorithm;
import aco_entities.Bin;
import aco_entities.Item;
import aco_entities.Resource;

public class Base {
	private final static ACOAlgorithm aco = new ACOAlgorithm();
	private Timer predictionTimer, acoTimer;
	private List<Item> items;
	private List<PredictionBox> pboxes;
	private int predEpoch, acoEpoch;
	private PredictionQueue itemsQueue;
	GraphCSVWriter graphCSV;

	private static int PREDICTION_INTERVAL = 5000; // ms
	private static int ACO_INTERVAL = 3000; // ms

	public static List<Item> overflowItems;
	private List<Item> leftoverItems;

	public Base() {
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

		items = new ArrayList<Item>();
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

	public void Start() {
		predictionTimer = new Timer();
		predictionTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				UpdatePrediction();
			}
		}, 2 * 1000, PREDICTION_INTERVAL);

		acoTimer = new Timer();
		acoTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				UpdateACO();
			}
		}, 2 * 1000, ACO_INTERVAL);
	}

	public void Stop() {
		if (predictionTimer != null)
			predictionTimer.cancel();
		if (acoTimer != null)
			acoTimer.cancel();
	}

	private void UpdatePrediction() {
		System.out.println("---------------------------------");
		System.out.println("Prediction epoch " + predEpoch);
		System.out.println("---------------------------------");

		if (acoEpoch == 0) {
			items.clear();
		}
		checkItemsTimer();
		// update all the items via neural nets
		System.out.println("Items size: " + items.size());
		if (overflowItems != null && overflowItems.size() > 0) {
			// for (Item item : overflowItems) {
			// System.out
			// .println("Overflowed items at the beginning of prediction: "
			// + item.getResourceDemand()[Resource.MIPS
			// .getIndex()]);
			// }
			Iterator<Item> it = overflowItems.iterator();
			while (it.hasNext()) {
				Item item = it.next();
				if (aco.getAvailableResources() != null)
					// System.out
					// .println("Items contains item "
					// + item.getResourceDemand()[Resource.MIPS
					// .getIndex()] + " is "
					// + items.contains(item));
					if (checkSpaceConstraints(item) && !items.contains(item)) {
						items.add(item);
						// for (Item i : items) {
						// System.out.println("Items after add: "
						// + i.getResourceDemand()[Resource.MIPS
						// .getIndex()]);
						// }
						System.out
								.println("Overflowed item removed from queue: "
										+ item.getResourceDemand()[Resource.MIPS
												.getIndex()]);
						it.remove();
						// for (Item i : items) {
						// System.out.println("Items after remove: "
						// + i.getResourceDemand()[Resource.MIPS.getIndex()]);
						// }
					}
			}
		}

		// for (Item item : overflowItems) {
		// System.out.println("Remaining overflowed items: "
		// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
		// }
		// itemsQueue.popFront();
		// for (Item i : items) {
		// System.out.println("Items before pboxes: "
		// + i.getResourceDemand()[Resource.MIPS.getIndex()]);
		// }
		for (int i = 0; i < pboxes.size(); i++) {
			pboxes.get(i).Update();

			if (aco.getAvailableResources() != null)
				if (checkSpaceConstraints(pboxes.get(i).getItem())) {
					// if(items.size() > 0)
					// System.out.println(items.size());
					items.add(new Item(pboxes.get(i).getItem()));
					// else
					// items.add(pboxes.get(i).getItem());
					// System.out
					// .println("Pboxes items goes to items: "
					// + pboxes.get(i).getItem()
					// .getResourceDemand()[Resource.MIPS
					// .getIndex()]);
					// for (Item item : items) {
					// System.out.println("Items after new pbox: "
					// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
					// }
				} else {
					overflowItems.add(pboxes.get(i).getItem());
					// System.out
					// .println("Overflowed item: "
					// + pboxes.get(i).getItem()
					// .getResourceDemand()[Resource.MIPS
					// .getIndex()]);
				}
		}
		// for (Item item : items) {
		// System.out.println("Items after pboxes: "
		// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
		// }
		itemsQueue.add(items);

		if (predEpoch == 5)
			predictionTimer.cancel();
		predEpoch++;
	}

	private void UpdateACO() {
		System.out.println("---------------------------------");
		System.out.println("ACO epoch " + acoEpoch);
		System.out.println("---------------------------------");

		if (itemsQueue.hasItems()) {
			// System.out.println("has items");
			List<Item> queueFront = itemsQueue.popFront();
			// // ACOAlgorithm.NB_OF_ITEMS = queueFront.size();
			// for (Item item : queueFront) {
			// System.out.println("Prediction queue items: "
			// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
			// }

			aco.setItems(queueFront);
			int size = aco.getItems().size();
			// aco.getItems().addAll(size, queueFront);
			for (Item item : aco.getItems()) {
				System.out.println("ACO items: "
						+ item.getResourceDemand()[Resource.MIPS.getIndex()]);
			}
			// ACOAlgorithm.NB_OF_ITEMS = aco.getItems().size();
			aco.setNB_OF_ITEMS(aco.getItems().size());
			System.out.println("NB ITEMS " + aco.getNB_OF_ITEMS());
			aco.init();
			aco.run();
			// for (Item item : overflowItems) {
			// System.out.println("Overflowed items after ACO runs: "
			// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
			// }
		}
		// items.clear();
		items = aco.getItems();
		if (items != null && items.size() > 0
				&& aco.getGlobalBestSolution() != null) {

			int[][] globalBestSolution = aco.getGlobalBestSolution();
			boolean equalityFlag;
			List<Bin> bins = aco.getBins();
			// for (Item item : items) {
			// System.out.println("Items after ACO runs: "
			// + item.getResourceDemand()[Resource.MIPS.getIndex()]);
			// }
			for (int row = 0; row < aco.getNB_OF_ITEMS(); row++) {
				for (int col = 0; col < ACOAlgorithm.NB_OF_BINS; col++) {
					// if()
					// System.out.println(!overflowItems.contains(items.get(row)));

					if (globalBestSolution[row][col] != 0) {
						// System.out.println("here");
						// && !overflowItems.contains(items.get(row))) {
						// deploy VM in corresponding Machine
						System.out.println("Item " + row + " in bin " + col);
						items.get(row).setDeploymentBin(bins.get(col));
						if (items.get(row).getEndRunTime() == null
								|| (items.get(row).getEndRunTime() != null && !items
										.get(row).getEndRunTime().isRunning())) {
							items.get(row).start();
						}
						globalBestSolution[row][col] = 0;
						break;
					}
				}
			}
		}
		if (acoEpoch == 11)
			Stop();
		acoEpoch++;
	}

	public void checkItemsTimer() {
		if (leftoverItems != null && leftoverItems.size() > 0)
			leftoverItems.clear();
		int row = 0;
		while (row < items.size() && items.size() > 0) {
			if (items.get(row).getEndRunTime() != null) {
				if (!items.get(row).getEndRunTime().isRunning()) {
					System.out
							.println("Remove item "
									+ items.get(row).getResourceDemand()[Resource.MIPS
											.getIndex()]
									+ " nb "
									+ row
									+ " after "
									+ items.get(row).getResourceDemand()[Resource.RUN_TIME
											.getIndex()]);
					items.remove(row);
					if (row == (items.size() + 1))
						row--;
				} else {
					leftoverItems.add(items.get(row));
					row++;
					// System.out.println("Row " + row);
				}
			} else {
				row++;
			}
		}
		aco.setLeftoverItems(leftoverItems);

	}

	private boolean checkSpaceConstraints(Item item) {
		int[] resourceDemand = item.getResourceDemand();
		int[] availableResources = aco.getAvailableResources();
		if ((resourceDemand[Resource.MIPS.getIndex()] > availableResources[Resource.MIPS
				.getIndex()])
				|| (resourceDemand[Resource.CORES.getIndex()] > availableResources[Resource.CORES
						.getIndex()])
				|| (resourceDemand[Resource.BANDWIDTH.getIndex()] > availableResources[Resource.BANDWIDTH
						.getIndex()])
				|| (resourceDemand[Resource.STORAGE.getIndex()] > availableResources[Resource.STORAGE
						.getIndex()])
				|| (resourceDemand[Resource.RAM.getIndex()] > availableResources[Resource.RAM
						.getIndex()]))
			return false;
		return true;
	}

	// /**
	// * @return the overflowItems
	// */
	// public List<Item> getOverflowItems() {
	// return overflowItems;
	// }
	//
	// /**
	// * @param overflowItems the overflowItems to set
	// */
	// public void setOverflowItems(List<Item> overflowItems) {
	// this.overflowItems = overflowItems;
	// }
}
