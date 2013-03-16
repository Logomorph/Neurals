package input;

import java.io.Serializable;
import java.util.List;

import neuralnet.Link;

abstract public class InputFunction implements Serializable {
	private static final long serialVersionUID = 1L;

	abstract public double Process(List<Link> links);
}
