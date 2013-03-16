package learning;

import java.util.List;

import transfer.TransferFunction;
import data.DataSet;
import data.DataSetRow;
import neuralnet.Layer;
import neuralnet.Link;
import neuralnet.Network;
import neuralnet.Neuron;

public class Learner {
	Network network;
	//protected double totalSquaredErrorSum;
	protected double learnRate;
	public Learner(Network n, double learnRate) {
		this.network = n;		
		//this.totalSquaredErrorSum = 0;
		this.learnRate = learnRate;
	}
	
	public void TrainNetwork(DataSet ds) {
		for(int i=0; i<ds.GetRowCount();i++) {
			DataSetRow row = ds.GetRow(i);
			learnPattern(row);
		}
	}
	
	private void learnPattern(DataSetRow r) {
		double[] input = r.inputData;
		network.setInput(input);
		network.Process();
		
		double[] output = network.getOutput();
		double[] desiredOut = r.outputData;
		double[] error = calculateError(desiredOut, output);
		//addToSquaredErrSum(error);
		updateWeights(error);
	}
	
	protected double[] calculateError(double[] desired, double[] actual) {
		double[] error = new double[desired.length];
		
		for(int i=0;i<desired.length;i++) {
			error[i] = desired[i] - actual[i];
		}
		
		return error;
	}
	
	/*protected void addToSquaredErrSum(double[] error){
		double errSqrSum = 0;
		
		for(int i=0;i<error.length;i++) {
			errSqrSum += (error[i] * error[i]) * 0.5d;
		}
		
		totalSquaredErrorSum += errSqrSum;
	}*/
	
	protected void updateWeights(double[] error) {
		updateOutputNeurons(error);
		updateHiddenNeurons();
	}
	
	/*
	 * Calculate the error and update the output neurons
	 */
	protected void updateOutputNeurons(double[] error) {
		int i=0;
		for(Neuron n : network.getOutputNeurons()) {
			if(error[i] == 0) {
				n.setError(0);
			} else {
				TransferFunction tf = n.getTransferFunction();
				double nInput = n.getInput();
				double delta = error[i] * tf.getDerivative(nInput);
				n.setError(delta);
				
				updateNeuronWeights(n);
			}
			i++;
		}
	}
	
	protected void updateHiddenNeurons() {
		List<Layer> layers = network.getLayers();
		
		for(int i = layers.size()-2; i>0; i--) {
			for(Neuron n:layers.get(i).getNeurons()) {
				double nError = calculateHiddenNeuronError(n);
				n.setError(nError);
				if(n.isAdaptive())
					n.Train();
				this.updateNeuronWeights(n);
			}
		}
	}
	
	protected double calculateHiddenNeuronError(Neuron n) {
		double deltaSum = 0d;
		
		for(Link l : n.getOutputLinks()) {
			double delta = l.end.getError() * l.weight;
			deltaSum += delta;
		}
		
		TransferFunction tf = n.getTransferFunction();
		double nInput = n.getInput();
		double deriv = tf.getDerivative(nInput);
		double nError = deriv * deltaSum;
		return nError;
	}
	
	protected void updateNeuronWeights(Neuron n) {
		double err = n.getError();
		
		for(Link l : n.getInputLinks()) {
			double input = l.GetInput();
			
			double wChange = learnRate * err * input;
			
			l.weight += wChange;
		}
	}
}
