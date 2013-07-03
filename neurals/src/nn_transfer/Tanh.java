package nn_transfer;

public class Tanh extends TransferFunction{

	private double slope = 2d;
	private double output = 0;

	public Tanh() {
	}

	public Tanh(double slope) {
		this.slope = slope;
	}
	
	@Override
	public double Process(double in) {
		// conditional logic helps to avoid NaN
        if (in > 100) {
            return 1.0;
        }else if (in < -100) {
            return -1.0;
        }

        double E_x = Math.exp(this.slope * in);                
        this.output = (E_x - 1d) / (E_x + 1d);

		//System.out.println("tanh" + (this.output < 0));
        return this.output ;//< 0 ? 0 : this.output;
	}

	@Override
	public double getDerivative(double in) {
		return (1d - output * output);
	}

}
