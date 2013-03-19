package base;

import nn_learning.Learner;
import nn_data.DataSet;
import neuralnet.Layer;
import neuralnet.Network;
import nn_data.DataSetRow;
import nn_patterns.SineWave;
import aco_entities.Item;
import aco_entities.Resource;

/*
 * Contains all the neural networks needed for predicting data for one VM
 */
public class PredictionBox {
	Item vm;
	int[] resourceCapacity;

	// this stuff will actually come from open nebula in real time
	double[] MIPS_data;
	double[] CORES_data;
	double[] RAM_data;
	double[] STORAGE_data;
	double[] BANDWIDTH_data;

	int index;

	// stuff for prediction
	Network mipsNet, coresNet, ramNet, storeNet, bwNet;

	public PredictionBox(Item item, int[] resourceCapacity) {
		this.vm = item;
		this.resourceCapacity = resourceCapacity;
		populateData();
		createNeuralNets();
	}

	// Update all the neural networks and the Item
	public void Update() {
		int[] resourceDemand = new int[Resource.values().length];
		
		// MIPS NN
		double[] inMips = { MIPS_data[index - 2], MIPS_data[index - 1],
				MIPS_data[index] };
		mipsNet.setInput(inMips);
		mipsNet.Process();

		resourceDemand[Resource.MIPS.getIndex()] = (int) (mipsNet.getOutput()[0] * vm.MIPS_MAX);
		
		// CORES NN
		double[] inCores = { CORES_data[index - 2], CORES_data[index - 1],
				CORES_data[index] };
		coresNet.setInput(inCores);
		coresNet.Process();

		resourceDemand[Resource.CORES.getIndex()] = (int) (coresNet.getOutput()[0] * vm.CORES_MAX);
		
		// RAM NN
		double[] inRam = { RAM_data[index - 2], RAM_data[index - 1],
				RAM_data[index] };
		ramNet.setInput(inRam);
		ramNet.Process();

		resourceDemand[Resource.RAM.getIndex()] = (int) (ramNet.getOutput()[0] * vm.RAM_MAX);
		
		// STORAGE NN
		double[] inStorage = { STORAGE_data[index - 2], STORAGE_data[index - 1],
				STORAGE_data[index] };
		storeNet.setInput(inStorage);
		storeNet.Process();

		resourceDemand[Resource.STORAGE.getIndex()] = (int) (storeNet.getOutput()[0] * vm.STORAGE_MAX);
		
		// BANDWIDTH NN
		double[] inBw = { BANDWIDTH_data[index - 2], BANDWIDTH_data[index - 1],
				BANDWIDTH_data[index] };
		bwNet.setInput(inBw);
		bwNet.Process();

		resourceDemand[Resource.BANDWIDTH.getIndex()] = (int) (bwNet.getOutput()[0] * vm.BANDWIDTH_MAX);
		
		this.vm.setResourceDemand(resourceDemand);
		
		index++;
	}

	public Item getItem() {
		return this.vm;
	}

	// all this is temporary
	private void populateData() {
		SineWave swg = new SineWave();
		MIPS_data = swg.GeneratePattern(0);
		CORES_data = swg.GeneratePattern(0);
		RAM_data = swg.GeneratePattern(0);
		STORAGE_data = swg.GeneratePattern(0);
		BANDWIDTH_data = swg.GeneratePattern(0);
		index= 2;
	}

	private void createNeuralNets() {
		mipsNet = createAndTrainNetwork(MIPS_data);
		coresNet = createAndTrainNetwork(CORES_data);
		ramNet = createAndTrainNetwork(RAM_data);
		storeNet = createAndTrainNetwork(STORAGE_data);
		bwNet = createAndTrainNetwork(BANDWIDTH_data);
	}

	private Network createAndTrainNetwork(double[] data) {
		// create the network
		Layer in = new Layer(3, false);
		Layer hidden = new Layer(2, false);
		Layer out = new Layer(1, false);

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
		DataSet ds = createSineWaveDataSet(data);
		Learner l = new Learner(net, 0.8);

		for (int i = 0; i < 50; i++)
			l.TrainNetwork(ds);
		System.out.println("Done training");

		return net;
	}

	private static DataSet createSineWaveDataSet(double[] data) {
		DataSet ds = new DataSet();

		for (int i = 0; i < data.length - 4; i++) {
			DataSetRow dsr = new DataSetRow();
			double[] inp = new double[3];
			inp[0] = data[i] >=0 ? data[i] : 0;
			inp[1] = data[i + 1] >=0 ? data[i+1] : 0;
			inp[2] = data[i + 2] >=0 ? data[i+2] : 0;
			double[] outp = new double[1];
			outp[0] = data[i + 3] >=0 ? data[i+3] : 0;

			dsr.inputData = inp;
			dsr.outputData = outp;
			ds.AddRow(dsr);
		}
		return ds;
	}
}
