package nn_transfer;

public class Linear extends TransferFunction {

	private double slope = 1d;
	
	public Linear() {
	}
	public Linear(double slope) {
		this.slope = slope;
	}
	
	@Override
	public double Process(double in) {
		//System.out.println((slope * in) < 0);
		return (slope * in) < 0 ? 0 : (slope *in);
	}

	@Override
	public double getDerivative(double in) {
		return this.slope;
	}

}
