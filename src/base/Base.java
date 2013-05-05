package base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

	private static int PREDICTION_INTERVAL = 5000; // ms
	private static int ACO_INTERVAL = 3000; // ms

	public Base() {
		itemsQueue = new PredictionQueue();

		LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader
				.readData();

		ACOAlgorithm.NB_OF_BINS = numbers.get(0);
		ACOAlgorithm.NB_OF_ITEMS = numbers.get(1);

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

			PredictionBox pboxBuff = new PredictionBox(i, resourceCapacity);
			pboxes.add(pboxBuff);
		}

		predEpoch = 0;
		acoEpoch = 0;
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

//		if(acoEpoch > 0)
//			checkItemsTimer();
		// update all the items via neural nets
		for (int i = 0; i < pboxes.size(); i++) {
			pboxes.get(i).Update();
			items.set(i, pboxes.get(i).getItem());
			//items.add(pboxes.get(i).getItem());
		}
		itemsQueue.add(items);
//		if (predEpoch == 5)
//			Stop();
		predEpoch++;
	}

	private void UpdateACO() {
		System.out.println("---------------------------------");
		System.out.println("ACO epoch " + acoEpoch);
		System.out.println("---------------------------------");

		// run the algorithm
		if (itemsQueue.hasItems()) {
//			ACOAlgorithm.NB_OF_ITEMS = itemsQueue.popFront().size();
			aco.setItems(itemsQueue.popFront());		
			aco.init();
			aco.run();
//			for (Item i : aco.getItems()) {
//				System.out.println("Item " + items.indexOf(i) + " has " + i.getResourceDemand()[Resource.MIPS.getIndex()]);
		}
	
		if (items != null && items.size() > 0 && aco.getGlobalBestSolution() != null) {
			int[][] globalBestSolution = aco.getGlobalBestSolution();
			List<Bin> bins = aco.getBins();
			for (int row = 0; row < ACOAlgorithm.NB_OF_ITEMS; row++) {
				for (int col = 0; col < ACOAlgorithm.NB_OF_BINS; col++) {
					if (globalBestSolution[row][col] != 0) {
						// deploy VM in corresponding Machine
						System.out.println("Item " + row + " in bin " + col);
						items.get(row).setDeploymentBin(bins.get(col));
						items.get(row).start();
						globalBestSolution[row][col] = 0;
						break;
					}
				}
			}
		}
//		if(acoEpoch == 6)
//		{}

		if (acoEpoch == 7)
			Stop();
		acoEpoch++;
	}
	
	public void checkItemsTimer() {
//		int[][] globalBestSolution = aco.getGlobalBestSolution();
//		List<Bin> bins = aco.getBins();
		for (int row = 0; row < ACOAlgorithm.NB_OF_ITEMS; row++) {
			if(items.get(row).getEndRunTime() != null && !items.get(row).getEndRunTime().isRunning()) {
				System.out.println("Remove item nb " + row);
				
				items.remove(row);
			}
		}
	}
}
