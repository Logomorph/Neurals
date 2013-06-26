package dclink_impl;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.host.Host;
import org.opennebula.client.host.HostPool;

import util.XMLParser;
import dclink_entities.HostData;
import dclink_if.HostDAORemote;

public class HostDAO implements HostDAORemote {
	private Client oneClient;
	private List<HostData> hosts = new ArrayList<HostData>();
	private Host host;
	private HostPool hostPool;
	private OneResponse res;

	public HostDAO() {
		try {
			oneClient = new Client("oneadmin:oneadmin",
					"http://localhost:2633/RPC2");
			hostPool = new HostPool(oneClient);
			hostPool.info();
			XMLParser parser = new XMLParser();
			setHosts(parser.readXML("host.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<HostData> getHosts() {
		int i = 0;
		if (hostPool != null && hostPool.getLength() > 0) {
			for (Host h : hostPool) {
				hosts.get(i).setStatus(h.stateStr());				
				i++;
			}		
			return hosts;
		}
		return null;
	}

	public void writeHostPoolInfoToXml(String filename) {
		PrintWriter writer;
		try {
			writer = new PrintWriter("host.txt", "UTF-8");
			writer.println(hostPool.info().getMessage());
			writer.close(); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Client getOneClient() {
		return oneClient;
	}

	public void setOneClient(Client oneClient) {
		this.oneClient = oneClient;
	}

	public void setHosts(List<HostData> hosts) {
		this.hosts = hosts;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public HostPool getHostPool() {
		return hostPool;
	}

	public void setHostPool(HostPool hostPool) {
		this.hostPool = hostPool;
	}

	public OneResponse getRes() {
		return res;
	}

	public void setRes(OneResponse res) {
		this.res = res;
	}

}
