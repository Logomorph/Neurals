package nn_transfer;

public class Exp extends TransferFunction {

	@Override
	public double Process(double in) {
		if(in == Double.NaN)
			in=0;
		//System.out.println(""+in);
		return Math.exp(in);
	}

	@Override
	public double getDerivative(double in) {
		return Math.log(in);
	}

}
