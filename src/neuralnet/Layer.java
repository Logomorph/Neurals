package neuralnet;

import java.util.ArrayList;
import java.util.List;

public class Layer {
	public List<Neuron> neurons;

	public Layer() {
		neurons = new ArrayList<Neuron>();
	}
	
	public Layer(int neuronsCount) {
		neurons = new ArrayList<Neuron>();
		for(int i=0;i<neuronsCount;i++) {
			Neuron n = new Neuron();
			neurons.add(n);
		}
	}
	public void addNeuron(Neuron n) {
		neurons.add(n);
	}
	public List<Neuron> getNeurons() {
		return neurons;
	}
	
	public void Process() {
		//System.out.println("[Layer] Updating neurons");
		int i=0;;
		for(Neuron n:this.neurons) {
			//System.out.println("[Layer] Neuron: " + i);
			n.Process();
			i++;
		}
	}
	
	public void Reset() {
		for(Neuron neuron : this.neurons) {
			neuron.reset();
		}		
	}

	public void RandomizeWeights(double minWeight, double maxWeight) {
		for(Neuron neuron : this.neurons) {
			neuron.RandomizeWeights(minWeight, maxWeight);
		}
	}

	public void RandomizeWeights() {
		for(Neuron neuron : this.neurons) {
			neuron.RandomizeWeights();
		}
	}
}
