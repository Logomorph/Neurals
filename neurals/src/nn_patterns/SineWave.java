package nn_patterns;

import java.util.Random;

import util.Util;

public class SineWave extends PatternGenerator {

	@Override
	public double[] GeneratePattern(double verticalDisplacement) {
		Random r = new Random();
		double[] data = new double[1000];

		double step = 0;
		boolean up = true;
		for (int i = 0; i < 1000; i++) {
			//data[i] = Util.ScaleRange(Math.sin(step) + r.nextDouble(), -2, 2, -1, 1);
			data[i] = verticalDisplacement + Math.sin(step);
			if (up) {
				step += 0.05;
				if (step > 1) {
					step = 1;
					up = false;
				}
			} else {
				step -= 0.05;
				if (step < -1) {
					step = -1;
					up = true;
				}
			}
		}

		return data;
	}

}