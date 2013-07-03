package main;

import java.io.IOException;
import java.util.List;

import neuralnet.Layer;
import neuralnet.Network;
import nn_data.DataSet;
import nn_data.DataSetRow;
import nn_learning.Learner;
import nn_patterns.Exp;
import nn_patterns.Line;
import nn_patterns.SineWave;
import nn_patterns.SlopedLine;
import nn_transfer.Linear;
import nn_transfer.Sigmoid;
import nn_transfer.Sin;
import nn_transfer.Tanh;
import util.CSVWriter;
import util.Util;
import base.Base;

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
		SineWave sw_gen = new SineWave(0.2,0.6);
		Exp exp_gen = new Exp();
		SlopedLine sloped_gen = new SlopedLine(-70);
		SlopedLine sloped_gen2 = new SlopedLine(-100);

		DataSet line = Util.createDataSet(ln_gen.generatePattern(100, 0),3, 1);		
		DataSet test_line = Util.createDataSet(ln_gen.generatePattern(100, 0),3,1);
		DataSet sine_wave = Util.createDataSet(sw_gen.generatePattern(70, 0),3,1);
		DataSet exp = Util.createDataSet(exp_gen.generatePattern(100, 0), 3, 1);
		DataSet sloped = Util.createDataSet(sloped_gen.generatePattern(100, 0), 3, 1);
		DataSet sloped2 = Util.createDataSet(sloped_gen2.generatePattern(100, 0), 3, 1);
		
		Layer in = new Layer(3, new Tanh(1.0d), false);
		Layer hidden = new Layer(3, new Tanh(1.0d), false);
		Layer out = new Layer(1, new Linear(), false);

		in.connectLayers(hidden);
		hidden.connectLayers(out);

		Network net = new Network();
		net.addLayer(in);
		net.addLayer(hidden);
		net.addLayer(out);
		net.setInputNeurons(in.neurons);
		net.setOutputNeurons(out.neurons);


		CSVWriter csvw = new CSVWriter();
		try {
			csvw.OpenFile("test.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// train the network with sine data set
		System.out.println("Started training");
		Learner l = new Learner(net, 0.6);
		
		kFoldTest(10, net, l, sine_wave, csvw);
		csvw.WriteDataSetOutput(sine_wave);
/*
		//for (int i = 0; i < 50; i++) 
		{
			l.trainNetwork(sine_wave,0.01d);
			l.trainNetwork(line,0.001d);
			l.trainNetwork(sloped,0.001d);
			l.trainNetwork(sloped2,0.001d);
			//l.trainNetwork(exp,0.01d);
		}
		System.out.println("Done training");

		System.out.println("Started testing");
*/

		csvw.WriteDataSetOutput(sloped);
		TestNetwork(net,sloped, csvw);
		
		csvw.WriteDataSetOutput(sloped2);
		TestNetwork(net,sloped2, csvw);
		
		/*csvw.WriteDataSetOutput(sine_wave);
		TestNetwork(net,sine_wave, csvw);
		
		csvw.WriteDataSetOutput(test_line);
		TestNetwork(net,test_line, csvw);
		
		csvw.WriteDataSetOutput(exp);
		TestNetwork(net,exp, csvw);*/

		try {
			csvw.CloseFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done testing");
		
		Base b = new Base();
		b.start();
	}
	
	static void kFoldTest(int k, Network net, Learner l, DataSet ds, CSVWriter csvw) {
		List<DataSet> dataSets = Util.splitDataSet(ds, k);
		Line ln_gen = new Line();
		DataSet line = Util.createDataSet(ln_gen.generatePattern(100, 0),3, 1);	
		for(int i=0;i<dataSets.size();i++) {
			net.randomizeWeights();
			l.trainNetwork(line,0.001d);
			for(int j=0;j<dataSets.size();j++) {
				if(j!=i) {
					l.trainNetwork(dataSets.get(j), 0.00001d);
				}
			}
			
			csvw.WriteDataSetOutput(dataSets.get(i));
			TestNetwork(net, dataSets.get(i),csvw);
		}
	}
	
	static void TestNetwork(Network net, DataSet ds, CSVWriter csvw) {
		double[] output = new double[ds.getRowCount()];
		int i=0;
		for (DataSetRow dsr : ds.GetRows()) {
			net.setInput(dsr.inputData);
			net.process();
			csvw.WriteValue(net.getOutput()[0]);
			output[i] = net.getOutput()[0];
			i++;
		}
		csvw.WriteNewLine();
		csvw.WriteValue(computeMeanError(ds, output));
		csvw.WriteNewLine();
	}
	static double computeMeanError(DataSet ds1, double[] ds2) {
		double mean=0;
		for(int i=0;i<ds1.getRowCount();i++) {
			double delta = ds1.getRow(i).outputData[0] - ds2[i];
			
			mean += delta;
		}
		return mean/ds1.getRowCount();
	}
}
