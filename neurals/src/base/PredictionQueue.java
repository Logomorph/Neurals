package base;

import java.util.ArrayList;
import java.util.List;

import aco.entities.Item;

public class PredictionQueue {
	private class QueueItem {
		public List<Item> items;
	}

	List<QueueItem> qi;
	
	public PredictionQueue() {
		qi = new ArrayList<QueueItem>();
	}

	public synchronized void add(List<Item> items) {
		QueueItem buff = new QueueItem();
		buff.items = items;

		qi.add(buff);
	}

	/*
	 * @return null if queue is empty, list if queue has items in it
	 */
	public synchronized List<Item> popFront() {
		if (qi.size() == 0) {
			System.out.println("---------------------------------");
			System.out.println("Pop front, but prediction queue is empty.");
			System.out.println("---------------------------------");
			return null;
		}

		List<Item> buff = qi.get(0).items;
		qi.remove(0);
		return buff;
	}
	public synchronized boolean hasItems(){
		return qi.size() > 0;
	}
}
;