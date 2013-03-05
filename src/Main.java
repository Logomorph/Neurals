import patterns.SineWave;
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

			Layer in = new Layer(3);
			Layer hidden = new Layer(2);
			Layer out = new Layer(1);

			connectLayers(in, hidden);
			connectLayers(hidden, out);

			Network net = new Network();
			net.addLayer(in);
			net.addLayer(hidden);
			net.addLayer(out);
			net.setInputNeurons(in.neurons);
			net.setOutputNeurons(out.neurons);

			/*
			csvw.WriteLine(in1);
			net.setInput(in1);
			net.Process();
			csvw.WriteLine(net.getOutput());
			
			csvw.WriteLine(in2);
			net.setInput(in2);
			net.Process();
			csvw.WriteLine(net.getOutput());

			csvw.WriteLine(in3);
			net.setInput(in3);
			net.Process();
			csvw.WriteLine(net.getOutput());

			csvw.WriteLine(in4);
			net.setInput(in4);
			net.Process();
			csvw.WriteLine(net.getOutput());*/

			System.out.println("Started training\n\n");
			DataSet ds = createSineWaveDataSet();
			Learner l = new Learner(net, 0.7);

			for (int i = 0; i < 50; i++)
				l.TrainNetwork(ds);
			System.out.println("Done training\n\n");
			

			double[] outData = new double[1000];
			for(int i=0;i<ds.GetRowCount();i++) {
				net.setInput(ds.GetRow(i).inputData);
				net.Process();
				outData[i] = net.getOutput()[0];
			}
			csvw.WriteLine(outData);

			/*csvw.WriteLine(in1);
			net.setInput(in1);
			net.Process();
			csvw.WriteLine(net.getOutput());

			csvw.WriteLine(in2);
			net.setInput(in2);
			net.Process();
			csvw.WriteLine(net.getOutput());

			csvw.WriteLine(in3);
			net.setInput(in3);
			net.Process();
			csvw.WriteLine(net.getOutput());

			csvw.WriteLine(in4);
			net.setInput(in4);
			net.Process();
			csvw.WriteLine(net.getOutput());*/

			csvw.CloseFile();
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * double[] arr = {1,2,3,4}; CSVWriter csvw = new CSVWriter();
		 * 
		 * try { csvw.OpenFile("out.csv"); csvw.WriteLine(arr);
		 * csvw.WriteLine(arr); csvw.CloseFile(); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
	}

	private static void connectLayers(Layer l1, Layer l2) {
		// forward
		for (int i = 0; i < l2.neurons.size(); i++) {
			for (int j = 0; j < l1.neurons.size(); j++) {
				l2.neurons.get(i).addInputLink(l1.neurons.get(j));
			}
		}
		// backward
		for (int i = 0; i < l1.neurons.size(); i++) {
			for (int j = 0; j < l2.neurons.size(); j++) {
				l1.neurons.get(i).addOutputLink(l2.neurons.get(j));
			}
		}
	}

	private static DataSet createSineWaveDataSet() {
		DataSet ds = new DataSet();
		SineWave swg = new SineWave();
		double[] data = swg.GeneratePattern(0.0d);
		csvw.WriteLine(data);
		
		for(int i=0;i<data.length-4;i++) {
			DataSetRow dsr = new DataSetRow();
			double[] inp = new double[3];
			inp[0] = data[i];
			inp[1] = data[i+1];
			inp[2] = data[i+2];
			double[] outp = new double[1];
			outp[0] = data[i+3];
			
			dsr.inputData = inp;
			dsr.outputData = outp;
			ds.AddRow(dsr);
		}
		return ds;
	}
	private static DataSet createDataSet() {
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
