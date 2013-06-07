package nn_patterns;

public class Exp extends PatternGenerator {

	@Override
	public double[] generatePattern(int length, double verticalDisplacement) {
		double[] data = new double[length];
		for(int i=0;i<length;i++) {
			data[i] = Math.exp(i);
		}
		for(int i=0;i<length;i++) {
			data[i] /= data[length-1];
		}
		return data;
	}
}
