package neuralnet;


import java.awt.event.InputEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nn_input.InputFunction;
import nn_input.WeightedSum;
import nn_transfer.Log;
import nn_transfer.Sigmoid;
import nn_transfer.Sin;
import nn_transfer.TransferFunction;
import nn_transfer.TransferFunctionPicker;


public class Neuron implements Serializable {
	public List<Link> inLinks;
	public List<Link> outLinks;
	
	TransferFunction transferFunc;
	InputFunction inputFunc;
	TransferFunctionPicker tfp;
	
	// stuff
	private double output;
	private double netInput=0;
	private double error;
	
	// for adaptive transfer functions;
	private double desiredOutput;
	private boolean auto_pick;
	
	public Neuron(boolean auto_pick) {
		inLinks = new ArrayList<Link>();
		outLinks = new ArrayList<Link>();
		transferFunc = new Sin();
		inputFunc = new WeightedSum();
		this.auto_pick = auto_pick;
		if(auto_pick) {
			tfp = new TransferFunctionPicker();
		}
	}
	
	public void reset() {
		this.setInput(0d);
		this.setOutput(0d);
	}
	
	public void setInput(double input) {
		this.netInput = input;
	}
	
	public double getInput() {
		return netInput;
	}
	
	public void setOutput(double output) {
		this.output = output;
	}
	
	public double getOutput() {
		return output;
	}
	
	public void setDesiredOutput(double output) {
		this.desiredOutput = output;
	}
	
	public double getDesiredOutput() {
		return desiredOutput;
	}
	
	public void setError(double error) {
		this.error = error;
	}
	
	public double getError() {
		return this.error;
	}
	
	public List<Link> getInputLinks() {
		return inLinks;
	}
	
	public List<Link> getOutputLinks() {
		return outLinks;
	}
	
	public void PostInit() {
		if(auto_pick)
			tfp.InitNeuralNet(inLinks.size());
	}
	
	public boolean isAdaptive() {
		return auto_pick;
	}
	
	// For adaptive transfer functions
	public void Train() {
		tfp.Train(this.inLinks, netInput, this.output + this.error);
	}

	/**
	 * Calculates neuron's output
	 */
	public void Process() {
		if (auto_pick) {
			assert(this.inLinks.size() > 0);
			
			this.netInput = this.inputFunc.Process(this.inLinks);

			this.output = tfp.Pick(this.inLinks).Process(this.netInput);
		} else {
			if ((this.inLinks.size() > 0)) {
				this.netInput = this.inputFunc.Process(this.inLinks);
				// System.out.println("[Neuron] Net input: " + this.netInput);
			}

			this.output = this.transferFunc.Process(this.netInput);
		}
		// System.out.println("[Neuron] Output: " + this.output);
	}

	public boolean hasInputs() {
		return inLinks.size() != 0;
	}
	
	public void addInputLink(Neuron in) {
		inLinks.add(new Link(in,this));
	}
	
	public void addOutputLink(Neuron out) {
		outLinks.add(new Link(this,out));
	}	

	public void RandomizeWeights(double minWeight, double maxWeight) {
		for(Link connection : this.inLinks) {
			connection.RandomizeWeight(minWeight, maxWeight);
		}
	}
	
	public void RandomizeWeights() {
		for(Link connection : this.inLinks) {
			connection.RandomizeWeight();
		}		
	}
	
	public TransferFunction getTransferFunction() {
		return this.transferFunc;
	}
	
	public InputFunction getInputFunction() {
		return this.inputFunc;
	}
}
