package base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import util.InputReader;

import aco.ACOAlgorithm;
import aco_entities.Item;
import aco_entities.Resource;

public class Base {
	private final static ACOAlgorithm aco = new ACOAlgorithm();
	private Timer timer;
	private List<Item> items;
	private List<PredictionBox> pboxes;
	private int epoch;
	private PredictionQueue itemsQueue;
	

	private static int PREDICTION_INTERVAL = 3000; // ms
	
	public Base() {		
		itemsQueue = new PredictionQueue();
		
		LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader.readData();

		ACOAlgorithm.NB_OF_BINS = numbers.get(0);
		ACOAlgorithm.NB_OF_ITEMS = numbers.get(1);

		int[] resourceCapacity = new int[Resource.values().length];
		resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(2);
		resourceCapacity[Resource.CORES.getIndex()] = numbers.get(3);
		resourceCapacity[Resource.RAM.getIndex()] = numbers.get(4);
		resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(5);
		resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers.get(6);

		aco.initalizeBinsData(resourceCapacity);


		items = new ArrayList<Item>();
		pboxes = new ArrayList<PredictionBox>();
		Item i;

		for (int s = 7; s < numbers.size(); s += 5) {
			int[] resourceDemand = new int[Resource.values().length];
			resourceDemand[Resource.MIPS.getIndex()] = numbers.get(s);
			resourceDemand[Resource.CORES.getIndex()] = numbers.get(s + 1);
			resourceDemand[Resource.RAM.getIndex()] = numbers.get(s + 2);
			resourceDemand[Resource.STORAGE.getIndex()] = numbers.get(s + 3);
			resourceDemand[Resource.BANDWIDTH.getIndex()] = numbers
					.get(s + 4);
			i = new Item();
			i.setResourceDemand(resourceDemand);
			items.add(i);
			
			PredictionBox pboxBuff = new PredictionBox(i, resourceCapacity);
			pboxes.add(pboxBuff);
		}
		epoch = 0;
	}

	public void Start() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Update();
			}
		}, 2 * 1000, PREDICTION_INTERVAL);
	}

	public void Stop() {
		if (timer != null)
			timer.cancel();
	}
	
	private void Update() {
		System.out.println("---------------------------------");
		System.out.println("Epoch "+epoch);
		System.out.println("---------------------------------");
		
		// update all the items via neural nets
		for(int i=0;i<pboxes.size();i++) {
			pboxes.get(i).Update();
			items.set(i, pboxes.get(i).getItem());
		}
		itemsQueue.add(items);
		
		// run the algorithm
		aco.setItems(itemsQueue.popFront());
		aco.init();
		aco.run();
		epoch++;
	}
}
