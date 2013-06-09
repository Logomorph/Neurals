package nn_patterns;

import java.util.Random;

public class Line extends PatternGenerator {

	@Override
	public double[] generatePattern(int length, double verticalDisplacement) {
		Random r = new Random();
		double height = r.nextDouble();
		double[] data = new double[length];
		for(int i=0;i<length;i++)
			data[i] = height;
		return data;
	}

}
