package nn_transfer;

import java.io.Serializable;

abstract public class TransferFunction implements Serializable {
	private static final long serialVersionUID = 1L;
	abstract public double Process(double in);
	
	abstract public double getDerivative(double in);
}
