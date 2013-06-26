package test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import dclink_entities.HostData;
import dclink_impl.HostDAO;

public class HostTest {
	
	HostDAO hostDAO = new HostDAO();
	
	@Test
	public void testWriteToXML() {
		hostDAO.writeHostPoolInfoToXml("host.txt");
	}
	
	@Test
	public void testHostList() {
		List<HostData> hosts = hostDAO.getHosts();
		for(HostData h : hosts)
			System.out.println("Host " + h.getId() + " has state "
					+ h.getStatus());
		assertNotNull("Not null", hostDAO.getHosts());
	}
}
