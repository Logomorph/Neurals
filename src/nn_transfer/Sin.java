package nn_transfer;

public class Sin extends TransferFunction {

	@Override
	public double Process(double in) {
		return Math.sin(in);
	}
    
    @Override
    public double getDerivative(double in) {
	return Math.cos(in);
    }    
}
