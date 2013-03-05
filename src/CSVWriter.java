import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


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
}
