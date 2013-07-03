package nn_patterns;

import java.util.Random;

import util.Util;

public class SineWave extends PatternGenerator {

	private double min=-1,max=1;
	public SineWave() {
		
	}
	public SineWave(double lowClamp, double highClamp) {
		min = lowClamp;
		max = highClamp;
	}
	@Override
	public double[] generatePattern(int length, double verticalDisplacement) {
		Random r = new Random();
		double[] data = new double[length];

		double step = 0;
		boolean up = true;
		for (int i = 0; i < length; i++) {
			//data[i] = Util.ScaleRange(Math.sin(step) + r.nextDouble(), -2, 2, -1, 1);
			double point = verticalDisplacement + Math.sin(step);
			if(point < min)
				point = min;
			if(point > max)
				point = max;
			data[i] = point;
			if (up) {
				step += 0.05;
				if (step > 1) {
					step = 1;
					up = false;
				}
			} else {
				step -= 0.05;
				if (step < 0) {
					step = 0;
					up = true;
				}
			}
		}

		return data;
	}

}
