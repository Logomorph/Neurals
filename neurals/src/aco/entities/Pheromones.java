package aco.entities;

public class Pheromones {

	public static double calculateTauMin(double tauMax, double gConst) {
		return tauMax/gConst;
	}

	public static double calculateTauMax(int fBest, double ro) {
		return 1.0/(fBest * (1 - ro));
	}
}
