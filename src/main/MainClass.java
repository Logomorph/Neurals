package main;

import base.Base;

public class MainClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// RunACO();
		RunNNACO();
	}

	private static void RunNNACO() {
		Base b = new Base();
		b.Start();

	}

//	private static void RunACO() {
//		LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader
//				.readData();
//
//		ACOAlgorithm.NB_OF_BINS = numbers.get(0);
//		ACOAlgorithm.NB_OF_ITEMS = numbers.get(1);
//		ACOAlgorithm aco = new ACOAlgorithm();
//
//		int[] resourceCapacity = new int[Resource.values().length];
//		resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(2);
//		resourceCapacity[Resource.CORES.getIndex()] = numbers.get(3);
//		resourceCapacity[Resource.RAM.getIndex()] = numbers.get(4);
//		resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(5);
//		resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers.get(6);
//
//		aco.initalizeBinsData(resourceCapacity);
//
//		int s;
//		List<Item> items = new ArrayList<Item>();
//		Item i;
//
//		for (s = 7; s < numbers.size(); s += 6) {
//			resourceCapacity = new int[Resource.values().length];
//			resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(s);
//			resourceCapacity[Resource.CORES.getIndex()] = numbers.get(s + 1);
//			resourceCapacity[Resource.RAM.getIndex()] = numbers.get(s + 2);
//			resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(s + 3);
//			resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers
//					.get(s + 4);
//			resourceCapacity[Resource.RUN_TIME.getIndex()] = numbers.get(s + 5);
//			
//			i = new Item();
//			i.setResourceDemand(resourceCapacity);
//			items.add(i);
//		}
//
//		// System.out.println(aco.getBins().size());
//		// for (Item item : items) {
//		// System.out.println(item.getValueSet()[0]);
//		// }
//
//		aco.setItems(items);
//		aco.init();
//		aco.run();
//	}
}
