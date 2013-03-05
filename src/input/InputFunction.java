package input;

import java.util.List;

import neuralnet.Link;

abstract public class InputFunction {
	abstract public double Process(List<Link> links);
}
