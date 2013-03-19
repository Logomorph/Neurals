package nn_data;

import java.util.ArrayList;
import java.util.List;

public class DataSet {
	List<DataSetRow> rows;
	
	public DataSet() {
		rows = new ArrayList<DataSetRow>();
	}
	
	public void AddRow(DataSetRow r) {
		rows.add(r);
	}
	
	public void Clear() {
		rows.clear();
	}
	
	public int GetRowCount() {
		return rows.size();
	}
	
	public DataSetRow GetRow(int index) {
		return rows.get(index);
	}
	public List<DataSetRow> GetRows() {
		return rows;
	}
}
