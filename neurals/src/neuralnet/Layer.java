package neuralnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nn_transfer.TransferFunction;

public class Layer implements Serializable {
	private static final long serialVersionUID = 1L;
	public List<Neuron> neurons;

	public Layer() {
		neurons = new ArrayList<Neuron>();
	}
	
	public Layer(int neuronsCount, TransferFunction tf, boolean auto_pick) {
		neurons = new ArrayList<Neuron>();
		for(int i=0;i<neuronsCount;i++) {
			Neuron n = new Neuron(tf, auto_pick);
			neurons.add(n);
		}
	}
	public void addNeuron(Neuron n) {
		neurons.add(n);
	}
	public List<Neuron> getNeurons() {
		return neurons;
	}
	
	public void process() {
		//System.out.println("[Layer] Updating neurons");
		int i=0;;
		for(Neuron n:this.neurons) {
			//System.out.println("[Layer] Neuron: " + i);
			n.process();
			i++;
		}
	}
	
	public void reset() {
		for(Neuron neuron : this.neurons) {
			neuron.reset();
		}		
	}

	public void randomizeWeights(double minWeight, double maxWeight) {
		for(Neuron neuron : this.neurons) {
			neuron.RandomizeWeights(minWeight, maxWeight);
		}
	}

	public void randomizeWeights() {
		for(Neuron neuron : this.neurons) {
			neuron.randomizeWeights();
		}
	}

	public void connectLayers(Layer next_layer) {
		// forward
		for (int i = 0; i < next_layer.neurons.size(); i++) {
			for (int j = 0; j < this.neurons.size(); j++) {
				next_layer.neurons.get(i).addInputLink(this.neurons.get(j));
			}
			next_layer.neurons.get(i).postInit();
		}
		// backward
		for (int i = 0; i < this.neurons.size(); i++) {
			for (int j = 0; j < next_layer.neurons.size(); j++) {
				this.neurons.get(i).addOutputLink(next_layer.neurons.get(j));
			}
			this.neurons.get(i).postInit();
		}
	}
}
