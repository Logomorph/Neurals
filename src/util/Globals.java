package util;

public class Globals {
	private static int ID = -1;
	
	public static int GetVMID() {
		ID++;
		return ID;
	}
}
