import patterns.SineWave;
import utils.Util;
import learning.Learner;
import data.DataSet;
import data.DataSetRow;
import neuralnet.Layer;
import neuralnet.Network;

public class Main {
	static double[] in1 = { 0, 0 };
	static double[] out1 = { 0 };

	static double[] in2 = { 1, 0 };
	static double[] out2 = { 1 };

	static double[] in3 = { 0, 1 };
	static double[] out3 = { 1 };

	static double[] in4 = { 1, 1 };
	static double[] out4 = { 0 };

	static CSVWriter csvw = new CSVWriter();

	public static void main(String[] args) {
		try {

			csvw.OpenFile("out.csv");

			//create the network
			Layer in = new Layer(3, false);
			Layer hidden = new Layer(2, true);
			Layer out = new Layer(1, false);

			in.connectLayers(hidden);
			hidden.connectLayers(out);

			Network net = new Network();
			net.addLayer(in);
			net.addLayer(hidden);
			net.addLayer(out);
			net.setInputNeurons(in.neurons);
			net.setOutputNeurons(out.neurons);


			//train the network with sine data set
			System.out.println("Started training\n\n");
			DataSet ds = createSineWaveDataSet();
			Learner l = new Learner(net, 0.8);

			for (int i = 0; i < 500; i++)
				l.TrainNetwork(ds);
			System.out.println("Done training\n\n");

			Util.usage[0] = Util.usage[1] = Util.usage[2] = 0;
			//test the network with a straight line
			DataSet lineDs = createLineDataSet(0.001d);
			csvw.WriteDataSetOutput(lineDs);
			double[] outData = new double[10];
			for (int i = 0; i < lineDs.GetRowCount(); i++) {
				net.setInput(lineDs.GetRow(i).inputData);
				net.Process();
				outData[i] = net.getOutput()[0];
			}
			csvw.WriteLine(outData);


			csvw.CloseFile();
			System.out.println("Done");
			System.out.println(Util.usage[0] + "-" + Util.usage[1] + "-"
					+ Util.usage[2]);

			// Util.WriteNetwork(net, "network.net");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static DataSet createSineWaveDataSet() {
		DataSet ds = new DataSet();
		SineWave swg = new SineWave();
		double[] data = swg.GeneratePattern(0.0d);
		csvw.WriteLine(data);

		for (int i = 0; i < data.length - 4; i++) {
			DataSetRow dsr = new DataSetRow();
			double[] inp = new double[3];
			inp[0] = data[i];
			inp[1] = data[i + 1];
			inp[2] = data[i + 2];
			double[] outp = new double[1];
			outp[0] = data[i + 3];

			dsr.inputData = inp;
			dsr.outputData = outp;
			ds.AddRow(dsr);
		}
		return ds;
	}
	
	private static DataSet createLineDataSet(double dec) {
		DataSet ds = new DataSet();
		double start = 0.7d;
		
		for (int i = 0; i < 10; i++) {
			DataSetRow dsr = new DataSetRow();
			double[] inp = new double[3];
			inp[0] = start;
			inp[1] = start+dec;
			inp[2] = start+dec*2;
			double[] outp = new double[1];
			outp[0] = start+dec*3;
			
			start+=dec*4;

			dsr.inputData = inp;
			dsr.outputData = outp;
			ds.AddRow(dsr);
		}
		return ds;
	}

	private static DataSet createDataSetXOR() {
		DataSet data = new DataSet();

		{
			DataSetRow dsr = new DataSetRow();
			dsr.inputData = in1;
			dsr.outputData = out1;
			data.AddRow(dsr);
		}

		{
			DataSetRow dsr = new DataSetRow();
			dsr.inputData = in2;
			dsr.outputData = out2;
			data.AddRow(dsr);
		}

		{
			DataSetRow dsr = new DataSetRow();
			dsr.inputData = in3;
			dsr.outputData = out3;
			data.AddRow(dsr);
		}

		{
			DataSetRow dsr = new DataSetRow();
			dsr.inputData = in4;
			dsr.outputData = out4;
			data.AddRow(dsr);
		}

		return data;
	}
}
