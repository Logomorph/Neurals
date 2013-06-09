package nn_transfer;

public class Log extends TransferFunction {

	@Override
	public double Process(double in) {
		double ret = Math.abs(in);
		if(ret>0)
			return Math.log(ret);
		else 
			return 0;
	}
	
	@Override
    public double getDerivative(double net) {
	return (1/net);
    }  
}
