package main;

import java.io.IOException;

import neuralnet.Layer;
import neuralnet.Network;
import nn_data.DataSet;
import nn_data.DataSetRow;
import nn_learning.Learner;
import nn_patterns.Exp;
import nn_patterns.Line;
import nn_patterns.SineWave;
import nn_patterns.SlopedLine;
import nn_transfer.Sin;
import util.CSVWriter;
import util.Util;
import base.Base;
import dclink_if.DCMonitor;

public class MainClass {
	public static void main(String[] args) {
		// RunACO();
		RunNNACO();
		//RunNN();
		//DCMonitor dcm = new DCMonitor();
		//dcm.getVMMonitor(399);
	}

	private static void RunNNACO() {
		Base b = new Base();
		b.start();

	}

	// private static void RunACO() {
	// LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader
	// .readData();
	//
	// ACOAlgorithm.NB_OF_BINS = numbers.get(0);
	// ACOAlgorithm.NB_OF_ITEMS = numbers.get(1);
	// ACOAlgorithm aco = new ACOAlgorithm();
	//
	// int[] resourceCapacity = new int[Resource.values().length];
	// resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(2);
	// resourceCapacity[Resource.CORES.getIndex()] = numbers.get(3);
	// resourceCapacity[Resource.RAM.getIndex()] = numbers.get(4);
	// resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(5);
	// resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers.get(6);
	//
	// aco.initalizeBinsData(resourceCapacity);
	//
	// int s;
	// List<Item> items = new ArrayList<Item>();
	// Item i;
	//
	// for (s = 7; s < numbers.size(); s += 6) {
	// resourceCapacity = new int[Resource.values().length];
	// resourceCapacity[Resource.MIPS.getIndex()] = numbers.get(s);
	// resourceCapacity[Resource.CORES.getIndex()] = numbers.get(s + 1);
	// resourceCapacity[Resource.RAM.getIndex()] = numbers.get(s + 2);
	// resourceCapacity[Resource.STORAGE.getIndex()] = numbers.get(s + 3);
	// resourceCapacity[Resource.BANDWIDTH.getIndex()] = numbers
	// .get(s + 4);
	// resourceCapacity[Resource.RUN_TIME.getIndex()] = numbers.get(s + 5);
	//
	// i = new Item();
	// i.setResourceDemand(resourceCapacity);
	// items.add(i);
	// }
	//
	// // System.out.println(aco.getBins().size());
	// // for (Item item : items) {
	// // System.out.println(item.getValueSet()[0]);
	// // }
	//
	// aco.setItems(items);
	// aco.init();
	// aco.run();
	// }

	private static void RunNN() {
		Line ln_gen = new Line();
		SineWave sw_gen = new SineWave();
		Exp exp_gen = new Exp();
		SlopedLine sloped_gen = new SlopedLine();

		DataSet line = Util.createDataSet(ln_gen.generatePattern(100, 0),3, 1);		
		DataSet test_line = Util.createDataSet(ln_gen.generatePattern(100, 0),3,1);
		DataSet sine_wave = Util.createDataSet(sw_gen.generatePattern(100, 0),3,1);
		DataSet exp = Util.createDataSet(exp_gen.generatePattern(100, 0), 3, 1);
		DataSet sloped = Util.createDataSet(sloped_gen.generatePattern(100, 0), 3, 1);
		DataSet sloped2 = Util.createDataSet(sloped_gen.generatePattern(100, 0), 3, 1);
		
		Layer in = new Layer(3, new Sin(), false);
		Layer hidden = new Layer(2, new Sin(), false);
		Layer out = new Layer(1, new Sin(), false);

		in.connectLayers(hidden);
		hidden.connectLayers(out);

		Network net = new Network();
		net.addLayer(in);
		net.addLayer(hidden);
		net.addLayer(out);
		net.setInputNeurons(in.neurons);
		net.setOutputNeurons(out.neurons);

		// train the network with sine data set
		System.out.println("Started training");
		Learner l = new Learner(net, 0.8);

		for (int i = 0; i < 50; i++) {
			l.TrainNetwork(sine_wave,0.01d);
			l.TrainNetwork(line,0.001d);
			//l.TrainNetwork(exp,0.01d);
		}
		System.out.println("Done training");

		System.out.println("Started testing");


		CSVWriter csvw = new CSVWriter();
		try {
			csvw.OpenFile("test.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		csvw.WriteDataSetOutput(sloped);
		TestNetwork(net,sloped, csvw);
		
		csvw.WriteDataSetOutput(sloped2);
		TestNetwork(net,sloped2, csvw);
		
		csvw.WriteDataSetOutput(sine_wave);
		TestNetwork(net,sine_wave, csvw);
		
		csvw.WriteDataSetOutput(test_line);
		TestNetwork(net,test_line, csvw);
		
		csvw.WriteDataSetOutput(exp);
		TestNetwork(net,exp, csvw);

		/*for (DataSetRow dsr : sloped.GetRows()) {
			net.setInput(dsr.inputData);
			net.Process();
			csvw.WriteValue(net.getOutput()[0]);
		}
		csvw.WriteNewLine();

		for (DataSetRow dsr : sine_wave.GetRows()) {
			net.setInput(dsr.inputData);
			net.Process();
			csvw.WriteValue(net.getOutput()[0]);
		}
		csvw.WriteNewLine();
		
		for (DataSetRow dsr : test_line.GetRows()) {
			net.setInput(dsr.inputData);
			net.Process();
			csvw.WriteValue(net.getOutput()[0]);
		}
		csvw.WriteNewLine();

		for (DataSetRow dsr : exp.GetRows()) {
			net.setInput(dsr.inputData);
			net.Process();
			csvw.WriteValue(net.getOutput()[0]);
		}*/

		try {
			csvw.CloseFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done testing");
		
		Base b = new Base();
		b.start();
	}
	
	static void TestNetwork(Network net, DataSet ds, CSVWriter csvw) {
		for (DataSetRow dsr : ds.GetRows()) {
			net.setInput(dsr.inputData);
			net.Process();
			csvw.WriteValue(net.getOutput()[0]);
		}
		csvw.WriteNewLine();
	}
}
