package dclink_entities;

public class HostData {
	private String id;
	private String name;
	private String status;
	private int cpuUsage;
	private int maxCPU;
	private int memoryUsage;
	private int maxMemory;
	
	public HostData() {}
	
	public HostData(String id, String name, String status) {
		this.id = id;
		this.name = name;
		this.status = status;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public int getMaxCPU() {
		return maxCPU;
	}

	public void setMaxCPU(int maxCPU) {
		this.maxCPU = maxCPU;
	}

	public int getMemoryUsage() {
		return memoryUsage;
	}
	public void setMemoryUsage(int memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public int getMaxMemory() {
		return maxMemory;
	}
	public void setMaxMemory(int maxMemory) {
		this.maxMemory = maxMemory;
	}
	
	
}
