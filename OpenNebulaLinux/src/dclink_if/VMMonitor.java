package dclink_if;

import java.util.List;

import dclink_entities.MonitorData;

public interface VMMonitor {
	public boolean startMonitoring();
	public void stopMonitoring();
	
	public boolean isLocked();
	
	public int getRam();
	public int getCpu();
	public int getHost();
	
	public List<MonitorData> getCPUData();
	public List<MonitorData> getRAMData();
	public List<MonitorData> getNETData();
	public String getIP();
        public void deploy(int hostID);
	public void migrate(int hostID);
        public void destroy();
        public int getID();
}
