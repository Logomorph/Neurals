package transfer;

public class Log extends TransferFunction {

	@Override
	public double Process(double in) {
		return Math.log(Math.abs(in));
	}
}
