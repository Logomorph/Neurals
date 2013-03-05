package neuralnet;

import input.InputFunction;
import input.WeightedSum;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

import transfer.Log;
import transfer.Sigmoid;
import transfer.Sin;
import transfer.TransferFunction;

public class Neuron {
	public Layer parentLayer;
	public List<Link> inLinks;
	public List<Link> outLinks;
	
	TransferFunction transferFunc;
	InputFunction inputFunc;
	
	// stuff
	private double output;
	private double netInput=0;
	private double error;
	
	public Neuron() {
		parentLayer = null;
		inLinks = new ArrayList<Link>();
		outLinks = new ArrayList<Link>();
		transferFunc = new Sigmoid();
		inputFunc = new WeightedSum();
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

	/**
	 * Calculates neuron's output
	 */
	public void Process() {
		if ((this.inLinks.size() > 0)) {
			this.netInput = this.inputFunc.Process(this.inLinks);
			//System.out.println("[Neuron] Net input: " + this.netInput);
		}

		this.output = this.transferFunc.Process(this.netInput);
		//System.out.println("[Neuron] Output: " + this.output);
	}
	
	public boolean hasInputs() {
		return (inLinks.size() != 0);
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
