package nn_data;

import java.util.ArrayList;
import java.util.List;

public class DataSet {
	List<DataSetRow> rows;
	
	public DataSet() {
		rows = new ArrayList<DataSetRow>();
	}
	
	public void addRow(DataSetRow r) {
		rows.add(r);
	}
	
	public void clear() {
		rows.clear();
	}
	
	public int getRowCount() {
		return rows.size();
	}
	
	public DataSetRow getRow(int index) {
		return rows.get(index);
	}
	public List<DataSetRow> GetRows() {
		return rows;
	}
}
