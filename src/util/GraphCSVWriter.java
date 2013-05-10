package util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.jmx.snmp.Timestamp;

public class GraphCSVWriter {
	String path;
	BufferedWriter writer;

	public GraphCSVWriter(String path) {
		this.path = path;
		writeHeader();
	}

	private void writeHeader() {
		openFile(false);
		try {
			writer.write("TIMESTAMP,VM,MIPS,RAM,CORES,STORAGE,BANDWIDTH,RUNTIME\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeFile();
	}

	public void addLine(String item, int mips, int ram, int cores, int storage,
			int bw, int rt) {
		openFile(true);
		try {
			String ts = new SimpleDateFormat("dd/MM/yyyy:HH:MM:ss")
					.format(new Date());
			writer.write(ts + "," + item + "," + mips + "," + ram + "," + cores
					+ "," + storage + "," + bw + "," + rt + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeFile();
	}

	private void openFile(boolean append) {
		try {
			FileWriter fstream;
			fstream = new FileWriter(path, append);
			writer = new BufferedWriter(fstream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closeFile() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer = null;
	}
}
