package util;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nn_data.DataSet;
import nn_data.DataSetRow;


public class CSVWriter {
	PrintWriter out;
	public void OpenFile(String file) throws IOException  {
		FileWriter outFile = new FileWriter(file);
		out = new PrintWriter(outFile);	
	}
	public void CloseFile() throws IOException  {
		out.close();
	}
	public void WriteLine(double[] data) {
		for (double d : data) {
			out.print(d+",");
		}
		out.print("\n");
	}
	
	public void WriteDataSet(DataSet ds) {
		for(DataSetRow dsr : ds.GetRows()) {
			for(double d : dsr.inputData)
				out.print(d+",");
			for(double d : dsr.outputData)
				out.print(d+",");
		}
		out.print("\n");
	}
	
	public void WriteDataSetInput(DataSet ds) {
		for(DataSetRow dsr : ds.GetRows()) {
			for(double d : dsr.inputData)
				out.print(d+",");
		}
		out.print("\n");
	}
	
	public void WriteDataSetOutput(DataSet ds) {
		for(DataSetRow dsr : ds.GetRows()) {
			for(double d : dsr.outputData)
				out.print(d+",");
		}
		out.print("\n");
	}
}
