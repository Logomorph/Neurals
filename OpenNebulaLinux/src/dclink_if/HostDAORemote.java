package dclink_if;

import java.util.List;

import dclink_entities.HostData;

public interface HostDAORemote {
	public List<HostData> getHosts();
	
	public void writeHostPoolInfoToXml(String filename);
}
