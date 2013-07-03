package dclink_if;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennebula.client.Client;
import org.opennebula.client.host.Host;
import org.opennebula.client.host.HostPool;
import org.opennebula.client.image.ImagePool;
import org.opennebula.client.template.TemplatePool;
import org.opennebula.client.vm.VirtualMachine;
import org.opennebula.client.vm.VirtualMachinePool;

import util.XMLParser;
import dclink_entities.HostData;
import dclink_impl.MonitorOpenNebula;

public class DCMonitor {
	private Client oneClient;
	private TemplatePool tempPool;
	private List<HostData> hosts = new ArrayList<HostData>();
	private HostPool hostPool;
	private ImagePool imagePool;

	public DCMonitor() {
		try {
			oneClient = new Client("oneadmin:oneadmin",
					"http://localhost:2633/RPC2");
			tempPool = new TemplatePool(oneClient);
			tempPool.info();
			hostPool = new HostPool(oneClient);
			hostPool.info();
			
			imagePool = new ImagePool(oneClient);
			imagePool.info();
			System.out.println(imagePool.getById(65).info().getMessage());
			
			XMLParser parser = new XMLParser();
			setHosts(parser.readXML("hosts.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public VMMonitor getVMMonitor(int id) {
		VirtualMachine vm = new VirtualMachine(id, oneClient);
		vm.info();
		return new MonitorOpenNebula(vm,tempPool,imagePool, oneClient);
	}
	
	public List<Integer> getVMs() {
		List<Integer> vms = new ArrayList<Integer>();
		VirtualMachinePool vmp = new VirtualMachinePool(oneClient);
		vmp.info();
		Iterator<VirtualMachine> iterator = vmp.iterator();
		while(iterator.hasNext()) {
			vms.add(iterator.next().id());
		}
		return vms;
	}
	
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

	private void setHosts(List<HostData> hosts) {
		this.hosts = hosts;
	}
}