package input;

import java.util.List;

import neuralnet.Link;

public class WeightedSum extends InputFunction {

	@Override
	public double Process(List<Link> links) {
        double output = 0d;

        for (Link link : links) {
            output += link.GetWeightedInput();
        }

        return output;
    }

}
