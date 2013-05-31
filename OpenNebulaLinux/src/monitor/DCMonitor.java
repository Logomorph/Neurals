package monitor;

import monitor_impl.MonitorOpenNebula;

import org.opennebula.client.Client;
import org.opennebula.client.vm.VirtualMachine;

public class DCMonitor {
	Client oneClient;
	
	public DCMonitor() {
		try {
			oneClient = new Client("oneadmin:oneadmin",
					"http://localhost:2633/RPC2");
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public VMMonitor getVMMonitor(int id) {
		VirtualMachine vm = new VirtualMachine(id, oneClient);
		vm.info();
		return new MonitorOpenNebula(vm);
	}
}
