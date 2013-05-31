package util;

public class Initializer {

	public static double[][] initializePheromones(int nbOfItems, int nbOfBins, double pheromoneConcentration) {
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
	
	public static int[][] initializeIndividualAntMatrix(int nbOfItems, int nbOfBins) {
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
	
	public static double[][] initializeDeltaTau(int nbOfItems, int nbOfBins, double pheromoneConcentration) {
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
