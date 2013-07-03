package nn_input;

import java.util.List;
import java.io.Serializable;

import neuralnet.Link;

public class WeightedSum extends InputFunction {

	@Override
	public double process(List<Link> links) {
        double output = 0d;

        for (Link link : links) {
            output += link.getWeightedInput();
        }

        return output;
    }

}
