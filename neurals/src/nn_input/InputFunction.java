package nn_input;

import java.io.Serializable;
import java.util.List;

import neuralnet.Link;

abstract public class InputFunction implements Serializable {
	private static final long serialVersionUID = 1L;

	abstract public double process(List<Link> links);
}
