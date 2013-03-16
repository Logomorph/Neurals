package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	
	public static int[] usage = new int[3];
}
