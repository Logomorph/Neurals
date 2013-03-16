package transfer;

public class Sigmoid extends TransferFunction {

	double slope = 1.0d;
	double output;
	@Override
	public double Process(double in) {
		if (in > 100) {
            return 1.0;
        }else if (in < -100) {
            return 0.0;
        }
		double den = 1d + Math.exp(-this.slope * in);
		output = (1d / den);
        return output;
	}
	
	@Override
	public double getDerivative(double in) { // remove net parameter? maybe we dont need it since we use cached output value
                // +0.1 is fix for flat spot see http://www.heatonresearch.com/wiki/Flat_Spot
		double derivative = this.slope * this.output * (1d - this.output) + 0.1;
		return derivative;
	}
}
