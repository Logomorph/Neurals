package monitor_impl;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.opennebula.client.vm.VirtualMachine;

import monitor.MonitorData;
import monitor.VMMonitor;

public class MonitorOpenNebula implements VMMonitor {
	VirtualMachine vm;
	int MONITOR_INTERVAl = 10 * 1000;
	Timer monitorTimer;

	List<MonitorData> cpuData;
	List<MonitorData> ramData;
	List<MonitorData> netUsageData;

	int lastCPURetrieve = -1;
	int lastRAMRetrieve = -1;
	int lastNETRetrieve = -1;
	String IP = "";

	// List<Data> netTxData;
	// List<Data> netRxData;

	public MonitorOpenNebula(VirtualMachine vm) {
		this.vm = vm;
		if (vm != null)
			this.vm.info();
		cpuData = new ArrayList<MonitorData>();
		ramData = new ArrayList<MonitorData>();
		netUsageData = new ArrayList<MonitorData>();
		// netTxData = new ArrayList<Data>();
		// netRxData = new ArrayList<Data>();

		// parse the IP
		try {
			SAXBuilder builder = new SAXBuilder();

			// Should be parsed out of info, but the parsing fails, for some
			// reason
			Reader in = new StringReader(vm.monitoring().getMessage());
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			Element vme = root.getChild("VM");
			Element template = vme.getChild("TEMPLATE");
			Element nic = template.getChild("NIC");
			Element ip = nic.getChild("IP");
			CDATA c = (CDATA) ip.getContent().get(1);
			IP = c.getText();
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

	private void monitor() {
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

						netUsageData.add(new MonitorData(net_tx_val
								+ net_rx_val, lp));
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

						netUsageData.add(new MonitorData(net_tx_val
								+ net_rx_val, lp));
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
}
