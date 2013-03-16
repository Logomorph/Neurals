package transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import learning.Learner;

import data.DataSet;
import data.DataSetRow;

import utils.Util;

import neuralnet.Layer;
import neuralnet.Link;
import neuralnet.Network;

public class TransferFunctionPicker implements Serializable{
	private static final long serialVersionUID = 1L;
	
	List<TransferFunction> tfs;
	
	Network net;
	Learner teacher;
	
	public TransferFunctionPicker() {
		tfs = new ArrayList<TransferFunction>();
		tfs.add(new Sin());
		tfs.add(new Log());
		tfs.add(new Sigmoid());
	}
	
	public TransferFunction Pick(List<Link> links) {
		double[] input = new double[links.size()];
		
		for (int i=0; i<links.size(); i++) {
            input[i] = links.get(i).GetWeightedInput();
        }
		
		net.setInput(input);
		net.Process();
		double[] output = net.getOutput();
		
		int max = 0;
		for(int i=1;i<output.length;i++) 
			if(output[i]>output[max])
				max = i;
		Util.usage[max]++;
		
		return tfs.get(max);
	}
	
	public void InitNeuralNet(int input_no) {
		net = new Network();		

		Layer in = new Layer(input_no,false);
		Layer hidden = new Layer(3,false);
		Layer out = new Layer(tfs.size(),false);
		
		in.connectLayers(hidden);
		hidden.connectLayers(out);
		
		net.addLayer(in);
		net.addLayer(hidden);
		net.addLayer(out);
		
		net.setInputNeurons(in.neurons);
		net.setOutputNeurons(out.neurons);
		
		teacher = new Learner(net, 0.7d);
	}
	
	public void Train(List<Link> input, double netInput, double desiredOut) {
		// check which transfer function is closer to the desired output
		double minError = 1000;
		int closest = -1;
		
		for(int i=0;i<tfs.size();i++) {
			double out = tfs.get(i).Process(netInput);
			double error = desiredOut - out;
			if(Math.abs(error) < minError) {
				minError = Math.abs(error);
				closest = i;
			}
		}
		
		// create a dataset
		DataSet ds = new DataSet();
		DataSetRow dsr = new DataSetRow();
		
		double[] in = new double[input.size()];
		for(int i=0;i<in.length;i++) {
			in[i] = input.get(i).GetWeightedInput();
		}
		
		double[] out = new double[tfs.size()];
		for(int i=0;i<out.length;i++) {
			if(i == closest)
				out[i] = 1;
			else
				out[i] = 0;
		}
		dsr.inputData = in;
		dsr.outputData = out;
		ds.AddRow(dsr);
		
		// train the network with the dataset
		for(int i=0;i<50;i++) {
			//System.out.println(i);
			teacher.TrainNetwork(ds);
		}
	}
}
