package util;

import aco.entities.Bin;
import aco.entities.Resource;

public class Initializer {

	public static int[] initializeAvailableResources(Bin bin, int numberOfBins) {
		int[] availableResources = new int[Resource.values().length - 1];
		availableResources[Resource.CPU.getIndex()] = bin
				.getResourceCapacity()[Resource.CPU.getIndex()] * numberOfBins;
		availableResources[Resource.NETWORK_TRANSFER_SPEED.getIndex()] = bin
				.getResourceCapacity()[Resource.NETWORK_TRANSFER_SPEED.getIndex()]
				* numberOfBins;
		availableResources[Resource.RAM.getIndex()] = bin.getResourceCapacity()[Resource.RAM
				.getIndex()] * numberOfBins;
		availableResources[Resource.STORAGE.getIndex()] = bin
				.getResourceCapacity()[Resource.STORAGE.getIndex()]
				* numberOfBins;
		return availableResources;
	}

	public static double[][] initializePheromones(int nbOfItems, int nbOfBins,
			double pheromoneConcentration) {
		final double[][] localArray = new double[nbOfItems][nbOfBins];
		int rows = nbOfItems;
		int cols = nbOfBins;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				localArray[i][j] = pheromoneConcentration;
			}
		}
		return localArray;
	}

	public static int[][] initializeIndividualAntMatrix(int nbOfItems,
			int nbOfBins) {
		int[][] temp = new int[nbOfItems + 1][nbOfBins];
		for (int i = 0; i < nbOfItems; i++) {
			for (int j = 0; j < nbOfBins; j++) {
				temp[i][j] = 0;
			}
		}
		for (int j = 0; j < nbOfBins; j++) {
			temp[nbOfItems][j] = 0;
		}
		return temp;
	}

	public static double[][] initializeDeltaTau(int nbOfItems, int nbOfBins,
			double pheromoneConcentration) {
		final double[][] localArray = new double[nbOfItems][nbOfBins];
		int rows = nbOfItems;
		int cols = nbOfBins;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				localArray[i][j] = pheromoneConcentration;
			}
		}
		return localArray;
	}

	public static double[][] initializeMatrices(int nbOfItems, int nbOfBins) {
		double[][] temp = new double[nbOfItems + 1][nbOfBins];
		for (int i = 0; i < nbOfItems; i++) {
			for (int j = 0; j < nbOfBins; j++) {
				temp[i][j] = 0.0;
			}
		}
		return temp;
	}
}
