package utils;

public class Util {
	public static double Sign(double v) {
		if(v<0)
			return -1;
		else
			return 1;
	}
	public static double ScaleRange(double in, double oldMin, double oldMax, double newMin, double newMax) {
		double delta = in / oldMax;
		return delta * newMax;
	}
}
