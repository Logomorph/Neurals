package monitor;

import java.util.ArrayList;
import java.util.List;

public interface VMMonitor {
	public boolean startMonitoring();
	public void stopMonitoring();
	
	public List<MonitorData> getCPUData();
	public List<MonitorData> getRAMData();
	public List<MonitorData> getNETData();
	public String getIP();
}
