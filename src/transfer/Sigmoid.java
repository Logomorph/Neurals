package transfer;

public class Sigmoid extends TransferFunction {

	@Override
	public double Process(double in) {
		return 1.0d/(1+Math.exp(in));
	}

}
