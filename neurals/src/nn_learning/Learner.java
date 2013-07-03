package nn_learning;

import java.io.IOException;
import java.util.List;

import util.CSVWriter;

import neuralnet.Layer;
import neuralnet.Link;
import neuralnet.Network;
import neuralnet.Neuron;
import nn_data.DataSet;
import nn_data.DataSetRow;
import nn_transfer.TransferFunction;

public class Learner {
	Network network;

	/**
	 * Total network error
	 */
	protected transient double totalNetworkError;
	/**
	 * Total squared sum of all pattern errors
	 */
	protected transient double totalSquaredErrorSum;
	/**
	 * Total network error in previous epoch
	 */
	protected transient double previousEpochError;
	/**
	 * Max allowed network error (condition to stop learning)
	 */
	protected double maxError = 0.01d;
	/**
	 * Stopping condition: training stops if total network error change is
	 * smaller than minErrorChange for minErrorChangeIterationsLimit number of
	 * iterations
	 */
	private double minErrorChange = Double.POSITIVE_INFINITY;
	/**
	 * Stopping condition: training stops if total network error change is
	 * smaller than minErrorChange for minErrorChangeStopIterations number of
	 * iterations
	 */
	private int minErrorChangeIterationsLimit = Integer.MAX_VALUE;
	/**
	 * Count iterations where error change is smaller then minErrorChange
	 */
	private transient int minErrorChangeIterationsCount;
	/**
	 * Setting to determine if learning (weights update) is in batch mode False
	 * by default.
	 */

	// protected double totalSquaredErrorSum;
	protected double learnRate;

	public Learner(Network n, double learnRate) {
		this.network = n;
		// this.totalSquaredErrorSum = 0;
		this.learnRate = learnRate;
	}

	/**
	 * Trains until maxError is reached or a maximum number of iterations have
	 * passed
	 * 
	 * @param ds
	 * @param maxError
	 */
	public void trainNetwork(DataSet ds, double maxError) {
		this.maxError = maxError;
		this.minErrorChange = 0;
		this.totalNetworkError = 0d;
		this.previousEpochError = 0d;
		this.doLearningEpoch(ds);
	}

	/**
	 * Does one learn epoch
	 */
	public void trainNetwork(DataSet ds) {
		for (int i = 0; i < ds.getRowCount(); i++) {
			DataSetRow row = ds.getRow(i);
			learnPattern(row);
		}
	}

	/**
	 * Trains until a stopping condition was reached
	 * @param ds
	 */
	public void doLearningEpoch(DataSet ds) {
		CSVWriter csvw = new CSVWriter();
		try {
			csvw.OpenFile("learning.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		do {
			this.previousEpochError = this.totalNetworkError;
			this.totalNetworkError = 0d;
			this.totalSquaredErrorSum = 0d;
			for (int i = 0; i < ds.getRowCount(); i++) {
				DataSetRow row = ds.getRow(i);
				learnPattern(row);
			}

			this.totalNetworkError = this.totalSquaredErrorSum
					/ ds.getRowCount();
			csvw.WriteValue(totalNetworkError);
		} while (!reachedStopCondition());
		try {
			csvw.CloseFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Learns one DataSetRow and updates the weights
	 * @param r
	 */
	private void learnPattern(DataSetRow r) {
		double[] input = r.inputData;
		this.network.setInput(input);
		this.network.process();

		double[] output = network.getOutput();
		double[] desiredOut = r.outputData;
		double[] error = calculateError(desiredOut, output);
		this.addToSquaredErrSum(error);
		this.updateWeights(error);
	}

	/**
	 * Returns true if a stopping condition was reached
	 * @return
	 */
	private boolean reachedStopCondition() {
		/*System.out.println("No more error "+(this.totalNetworkError < this.maxError));
		System.out.println("t1 "+this.totalNetworkError+" t2 "+this.maxError);
		System.out.println("No more change "+(this.errorChangeStalled()));*/
		return (this.totalNetworkError < this.maxError)
				|| this.errorChangeStalled();
	}

	/**
	 * Checks if the error has stopped changing
	 * @return
	 */
	private boolean errorChangeStalled() {
		double absErrorChange = Math
				.abs(previousEpochError - totalNetworkError);

		if (absErrorChange <= this.minErrorChange) {
			this.minErrorChangeIterationsCount++;

			if (this.minErrorChangeIterationsCount >= this.minErrorChangeIterationsLimit) {
				return true;
			}
		} else {
			this.minErrorChangeIterationsCount = 0;
		}

		return false;
	}

	/**
	 * Calculates the error between desired data and actual data
	 * @param desired
	 * @param actual
	 * @return
	 */
	protected double[] calculateError(double[] desired, double[] actual) {
		double[] error = new double[desired.length];

		for (int i = 0; i < desired.length; i++) {
			error[i] = desired[i] - actual[i];
		}

		return error;
	}

	protected void addToSquaredErrSum(double[] error) {
		double errSqrSum = 0;

		for (double err : error) {
			errSqrSum += (err * err) * 0.5d;
		}

		totalSquaredErrorSum += errSqrSum;
	}

	protected void updateWeights(double[] error) {
		updateOutputNeurons(error);
		updateHiddenNeurons();
	}

	/*
	 * Calculate the error and update the output neurons
	 */
	protected void updateOutputNeurons(double[] error) {
		int i = 0;
		for (Neuron n : network.getOutputNeurons()) {
			if (error[i] == 0) {
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

		for (int i = layers.size() - 2; i > 0; i--) {
			for (Neuron n : layers.get(i).getNeurons()) {
				double nError = calculateHiddenNeuronError(n);
				n.setError(nError);
				if (n.isAdaptive())
					n.train();
				this.updateNeuronWeights(n);
			}
		}
	}

	protected double calculateHiddenNeuronError(Neuron n) {
		double deltaSum = 0d;

		for (Link l : n.getOutputLinks()) {
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

		for (Link l : n.getInputLinks()) {
			double input = l.getInput();

			double wChange = learnRate * err * input;

			l.weight += wChange;
		}
	}
}
