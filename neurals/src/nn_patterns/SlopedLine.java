package nn_patterns;

import java.util.Random;

public class SlopedLine extends PatternGenerator {

	double angle = -1000;
	public SlopedLine() {
		
	}
	public SlopedLine(double angle) {
		this.angle = angle;
	}
	@Override
	public double[] generatePattern(int length, double verticalDisplacement) {
		double[] data = new double[length];
		Random r = new Random();
		
		if(angle == -1000)
			angle = ((r.nextInt(180)-90));
		double start = angle < 0 ? 1.0d : 0.0d;
		System.out.println(angle);
		double factor = 1.0d / (double)length;
		for(int i=0;i<length;i++) {
			double value = start + factor * Math.sin(angle * (Math.PI / 180.0d));
			start = value;
			data[i] = value;
		}
		return data;
	}

}
