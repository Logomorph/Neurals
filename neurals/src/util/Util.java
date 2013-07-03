package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import nn_data.DataSet;
import nn_data.DataSetRow;

import neuralnet.Network;

public class Util {
	public static double sign(double v) {
		if(v<0)
			return -1;
		else
			return 1;
	}
	public static double scaleRange(double in, double oldMin, double oldMax, double newMin, double newMax) {
		double delta = in / oldMax;
		return delta * newMax;
	}
	
	public static void writeNetwork(Network net, String file)
			throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(net);
		out.close();
		fileOut.close();
	}

	public static Network readNetwork(String file) throws IOException,
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
			ds.addRow(dsr);
		}
		return ds;
	}
	
	public static List<DataSet> splitDataSet(DataSet ds, int k) {
		List<DataSet> output = new ArrayList<DataSet>();
		int rows_per_split = ds.getRowCount() / k;
		
		for(int i=0;i<k;i++) {
			DataSet new_ds = new DataSet();
			for(int j=i*rows_per_split;j<(i+1)*rows_per_split;j++) {
				new_ds.addRow(ds.getRow(j));
			}
			output.add(new_ds);
		}
		return output;
	}
}
