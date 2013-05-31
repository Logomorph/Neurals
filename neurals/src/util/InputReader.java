package util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputReader {

	public static List<Integer> readData() {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream("input.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			LinkedList<Integer> numbers = new LinkedList<Integer>();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				Pattern p = Pattern.compile("\\d+");
				Matcher m = p.matcher(strLine);
				while (m.find()) {
					numbers.add(Integer.parseInt(m.group()));
				}
			}
			in.close();
			return numbers;
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		return null;
	}
}
