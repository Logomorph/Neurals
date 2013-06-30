package dclink_impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.image.ImagePool;
import org.opennebula.client.template.Template;
import org.opennebula.client.template.TemplatePool;
import org.opennebula.client.vm.VirtualMachine;

import dclink_entities.MonitorData;
import dclink_if.VMMonitor;

public class MonitorOpenNebula implements VMMonitor {
	VirtualMachine vm;
	Template vmTemplate;
	Client oneClient;
	int MONITOR_INTERVAl = 10 * 1000;
	Timer monitorTimer;
	int ram, cpu;
	int hostId=0;

	List<MonitorData> cpuData;
	List<MonitorData> ramData;
	List<MonitorData> netUsageData;

	int lastCPURetrieve = -1;
	int lastRAMRetrieve = -1;
	int lastNETRetrieve = -1;
	String IP = "";
	int templateId = 0;
	boolean locked = false;
	ImagePool imagePool;

	// List<Data> netTxData;
	// List<Data> netRxData;

	public MonitorOpenNebula(VirtualMachine vm, TemplatePool tp, ImagePool imagePool,
			Client oneClient) {
		this.imagePool = imagePool;
		this.vm = vm;
		this.oneClient = oneClient;
		if (vm != null)
			this.vm.info();
		cpuData = new ArrayList<MonitorData>();
		ramData = new ArrayList<MonitorData>();
		netUsageData = new ArrayList<MonitorData>();
		// netTxData = new ArrayList<Data>();
		// netRxData = new ArrayList<Data>();

		// get host id
		try {
			SAXBuilder builder = new SAXBuilder();

			// Should be parsed out of info, but the parsing fails, for some
			// reason
			Reader in = new StringReader(vm.info().getMessage());
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			Element history = root.getChild("HISTORY_RECORDS");
			Element hist = history.getChild("HISTORY");
			Element HID = hist.getChild("HID");
			hostId = Integer.parseInt(HID.getValue());
			System.out.println(hostId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// parse the IP and the template id
		try {
			SAXBuilder builder = new SAXBuilder();

			// Should be parsed out of info, but the parsing fails, for some
			// reason
			Reader in = new StringReader(vm.monitoring().getMessage());
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			Element vme = root.getChild("VM");

			Element memory = vme.getChild("MEMORY");
			ram = Integer.parseInt(memory.getValue());
			System.out.println(ram);

			Element template = vme.getChild("TEMPLATE");
			Element nic = template.getChild("NIC");
			Element ip = nic.getChild("IP");
			CDATA c = (CDATA) ip.getContent().get(1);
			IP = c.getText();

			Element tempId = template.getChild("TEMPLATE_ID");
			c = (CDATA) tempId.getContent().get(1);
			templateId = Integer.parseInt(c.getText());

			Element cpus = template.getChild("CPU");
			c = (CDATA) cpus.getContent().get(1);
			cpu = Integer.parseInt(c.getText());
			System.out.println(cpu);
			System.out.println(templateId);
			this.vmTemplate = tp.getById(templateId);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getIP() {
		return IP;
	}

	@Override
	public boolean startMonitoring() {
		if (vm == null)
			return false;
		reset();
		monitorTimer = new Timer();
		monitorTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				monitor();
			}
		}, 2 * 1000, MONITOR_INTERVAl);
		return true;
	}

	@Override
	public void stopMonitoring() {
		if (monitorTimer != null)
			monitorTimer.cancel();
	}

	private void reset() {
		cpuData.clear();
		ramData.clear();
		netUsageData.clear();
		netUsageData.add(new MonitorData(0, 0));
	}

	private void monitor() {
		if (locked)
			return;
		SAXBuilder builder = new SAXBuilder();
		try {
			int lastCPU = -1;
			if (cpuData.size() > 0) {
				lastCPU = cpuData.get(cpuData.size() - 1).timestamp;
			}
			Reader in = new StringReader(vm.info().getMessage());
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			int lastPoll = Integer.parseInt(root.getChild("LAST_POLL")
					.getValue());

			if (lastPoll > lastCPU || lastCPU == -1) {
				boolean shoudAdd = false;
				// time to add more data
				in = new StringReader(vm.monitoring().getMessage());
				Document doc2 = builder.build(in);
				Element root2 = doc2.getRootElement();

				// to add only new stuff
				for (Element e : root2.getChildren()) {
					int lp = Integer.parseInt(e.getChild("LAST_POLL")
							.getValue());
					if (shoudAdd) {
						Element cpu = e.getChild("CPU");
						int cpu_val = Integer.parseInt(cpu.getValue());
						cpuData.add(new MonitorData(cpu_val, lp));

						Element ram = e.getChild("MEMORY");
						int ram_val = Integer.parseInt(ram.getValue());
						ramData.add(new MonitorData(ram_val, lp));

						Element net_tx = e.getChild("NET_TX");
						int net_tx_val = Integer.parseInt(net_tx.getValue());
						// netTxData.add(new Data(net_tx_val,lp));

						Element net_rx = e.getChild("NET_RX");
						int net_rx_val = Integer.parseInt(net_rx.getValue());
						// netRxData.add(new Data(net_rx_val,lp));

						int total_net = net_tx_val + net_rx_val;

						MonitorData old = netUsageData
								.get(netUsageData.size() - 1);
						int data = (total_net - old.data)
								/ (lp - old.timestamp);
						netUsageData.add(new MonitorData(data, lp));
						System.out.println("added value");
					}
					if (lp == lastCPU) {
						shoudAdd = true;
						System.out.println("found timestamp");
					}
				}
				// for some reason, the timestamp doesn't exist, so we'll add
				// everything
				// ToDo: should clear data?
				if (shoudAdd == false) {
					System.out.println("no timestamp");
					for (Element e : root2.getChildren()) {
						int lp = Integer.parseInt(e.getChild("LAST_POLL")
								.getValue());
						Element cpu = e.getChild("CPU");
						int cpu_val = Integer.parseInt(cpu.getValue());
						cpuData.add(new MonitorData(cpu_val, lp));

						Element ram = e.getChild("MEMORY");
						int ram_val = Integer.parseInt(ram.getValue());
						ramData.add(new MonitorData(ram_val, lp));

						Element net_tx = e.getChild("NET_TX");
						int net_tx_val = Integer.parseInt(net_tx.getValue());
						// netTxData.add(new Data(net_tx_val,lp));

						Element net_rx = e.getChild("NET_RX");
						int net_rx_val = Integer.parseInt(net_rx.getValue());
						// netRxData.add(new Data(net_rx_val,lp));

						int total_net = net_tx_val + net_rx_val;

						MonitorData old = netUsageData
								.get(netUsageData.size() - 1);
						int data = (total_net - old.data)
								/ (lp - old.timestamp);
						netUsageData.add(new MonitorData(data, lp));
						System.out.println("added value");
					}
				}
			} else {
				System.out.println("everything is up to date");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Returns last 3 CPU monitoring entries, or null if the timestamp didn't
	 * change since the last retrieval or there is not enough data;
	 */

	@Override
	public List<MonitorData> getCPUData() {
		if (((cpuData.get(cpuData.size() - 1).timestamp != lastCPURetrieve) || lastCPURetrieve == -1)
				&& cpuData.size() >= 3) {
			List<MonitorData> out = new ArrayList<MonitorData>();
			out.add(cpuData.get(cpuData.size() - 3));
			out.add(cpuData.get(cpuData.size() - 2));
			out.add(cpuData.get(cpuData.size() - 1));
			lastCPURetrieve = cpuData.get(cpuData.size() - 1).timestamp;
			return out;
		} else {
			return null;
		}
	}

	/*
	 * Returns last 3 RAM monitoring entries, or null if the timestamp didn't
	 * change since the last retrieval or there is not enough data;
	 */

	@Override
	public List<MonitorData> getRAMData() {
		if (((ramData.get(ramData.size() - 1).timestamp != lastRAMRetrieve) || lastRAMRetrieve == -1)
				&& ramData.size() >= 3) {
			List<MonitorData> out = new ArrayList<MonitorData>();
			out.add(ramData.get(ramData.size() - 3));
			out.add(ramData.get(ramData.size() - 2));
			out.add(ramData.get(ramData.size() - 1));
			lastRAMRetrieve = ramData.get(ramData.size() - 1).timestamp;
			return out;
		} else {
			return null;
		}
	}

	/*
	 * Returns last 3 NET monitoring entries, or null if the timestamp didn't
	 * change since the last retrieval or there is not enough data;
	 */

	@Override
	public List<MonitorData> getNETData() {
		if (((netUsageData.get(netUsageData.size() - 1).timestamp != lastNETRetrieve) || lastNETRetrieve == -1)
				&& netUsageData.size() >= 3) {
			List<MonitorData> out = new ArrayList<MonitorData>();
			out.add(netUsageData.get(netUsageData.size() - 3));
			out.add(netUsageData.get(netUsageData.size() - 2));
			out.add(netUsageData.get(netUsageData.size() - 1));
			lastNETRetrieve = netUsageData.get(netUsageData.size() - 1).timestamp;
			return out;
		} else {
			return null;
		}
	}

	@Override
	public void migrate(int hostID) {
		locked = true;

		System.out.println("Closing the previous VM");
		vm.destroy();
		OneResponse orp = vmTemplate.instantiate();
		System.out.println("New VM has ID " + orp.getMessage());
		vm = new VirtualMachine(Integer.parseInt(orp.getMessage()), oneClient);
		vm.info();
		System.out.println("Deploying");
		vm.deploy(hostID);

		while (!vm.lcmStateStr().equals("RUNNING")) {
			try {
				vm.info();
				// System.out.println("State: " + vm.lcmStateStr());
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done Migrating");
		locked = false;

		// System.out.println(orp.getMessage());
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public int getRam() {
		return this.ram;
	}

	@Override
	public int getCpu() {
		return this.cpu;
	}

	@Override
	public int getHost() {
		// TODO Auto-generated method stub
		return 0;
	}
}
