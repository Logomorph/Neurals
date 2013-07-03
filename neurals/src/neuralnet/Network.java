package neuralnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Network implements Serializable {
	private static final long serialVersionUID = 1L;
	List<Layer> layers;
	List<Neuron> inputNeurons;
	List<Neuron> outputNeurons;
	
	private double[] output;
	
	public Network() {
		layers = new ArrayList<Layer>();
	}
	
	public void addLayer(Layer l) {
		layers.add(l);
	}
	
	public List<Layer> getLayers() {
		return layers;
	}
	
	public void setInputNeurons(List<Neuron> n) {
		this.inputNeurons = n;
	}
	
	public void setOutputNeurons(List<Neuron> n) {
		this.outputNeurons = n;
	}
	
	public List<Neuron> getOutputNeurons() {
		return this.outputNeurons;
	}
	
	public List<Neuron> getInputNeurons() {
		return this.inputNeurons;
	}
	
	public void setInput(double[] input) {
		if(input.length != inputNeurons.size()){
			//System.out.println("[Network] Input vector size mismatch");
			return;
		}
		for(int i=0;i<input.length;i++)
			inputNeurons.get(i).setInput(input[i]);
	}
	
	public double[] getOutput() {
		return output;
	}
	
	public void process() {
		//System.out.println("[Network] Started processing");
		
		for(int i=0;i<layers.size();i++) {
			//System.out.println("[Network] Updating Layer "+i);
			layers.get(i).process();
		}
		
		output = new double[outputNeurons.size()];
		for(int i=0;i<outputNeurons.size();i++)
			output[i] = outputNeurons.get(i).getOutput();
		
		//System.out.println("[Network] Done processing");
	}
	public void randomizeWeights() {
		for(Layer l : layers) {
			l.randomizeWeights();
		}
	}
}
