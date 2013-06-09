package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import nn_data.DataSet;
import nn_data.DataSetRow;

import neuralnet.Network;

public class Util {
	public static double Sign(double v) {
		if(v<0)
			return -1;
		else
			return 1;
	}
	public static double ScaleRange(double in, double oldMin, double oldMax, double newMin, double newMax) {
		double delta = in / oldMax;
		return delta * newMax;
	}
	
	public static void WriteNetwork(Network net, String file)
			throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(net);
		out.close();
		fileOut.close();
	}

	public static Network ReadNetwork(String file) throws IOException,
			ClassNotFoundException {
		Network net;
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		net = (Network) in.readObject();
		in.close();
		fileIn.close();

		return net;
	}
	
	public static DataSet createDataSet(double[] data, int in_no, int out_no) {
		DataSet ds = new DataSet();
		int data_l = in_no + out_no;
		for (int i = 0; i < data.length - data_l; i++) {
			DataSetRow dsr = new DataSetRow();
			double[] inp = new double[in_no];
			
			for(int j=0;j<in_no;j++) {
				inp[j] = data[i+j] >= 0 ? data[i+j] : 0;
			}
			
			int out_start = i+in_no;
			double[] outp = new double[out_no];
			for(int j=0;j<out_no;j++) {
				outp[j] = data[out_start + j] >= 0 ? data[out_start + j] : 0;
			}

			dsr.inputData = inp;
			dsr.outputData = outp;
			ds.AddRow(dsr);
		}
		return ds;
	}
	
	public static int[] usage = new int[3];
}
