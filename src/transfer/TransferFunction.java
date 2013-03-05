package transfer;

abstract public class TransferFunction {
	abstract public double Process(double in);
	
	public double getDerivative(double in) {
		return 1d;
	}
}
