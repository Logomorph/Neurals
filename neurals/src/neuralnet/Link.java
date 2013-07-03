package neuralnet;

import java.io.Serializable;
import java.util.Random;

public class Link implements Serializable {
	private static final long serialVersionUID = 1L;
	public Neuron start,end;
	public double weight;
	
	public Link(Neuron st, Neuron en) {
		this.start = st;
		this.end = en;
		this.weight = Math.random() - 0.5d;
	}
	
	/**
	 *  Return input through the link
	 * @return input through the link
	 */
	public double getInput() {
		return this.start.getOutput();
	}
	
	/**
	 *  Return weighted input through the link
	 * @return weighted input through the link
	 */
	public double getWeightedInput() {
		//System.out.println("[Link] Input "+this.start.getOutput()+", weight "+weight);
		return this.start.getOutput() * weight;
	}
	
	/**
	 * Randomize the weight of the link
	 * @param generator
	 */
	public void randomizeWeight(Random generator) {
        this.weight = generator.nextDouble();
	}

	
	/**
	 * Randomize the weight of the link
	 * @param generator
	 */
	public void randomizeWeight(double min, double max) {
        this.weight = min + Math.random() * (max - min);
	}
	
	public void randomizeWeight() {
		this.weight = Math.random() - 0.5d;
	}
}
