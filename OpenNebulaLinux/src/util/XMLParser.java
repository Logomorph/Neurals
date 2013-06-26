package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dclink_entities.HostData;

public class XMLParser {

	public List<HostData> readXML(String filename) {
		
		List<HostData> hosts = new ArrayList<HostData>();
		try {

			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			Element root = doc.getDocumentElement();
//			System.out.println("Root element :"
//					+ root.getNodeName());

			NodeList nList = root.getElementsByTagName("HOST");

//			System.out.println("----------------------------");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Element host = (Element) nList.item(temp);
//				System.out.println("\nCurrent Element :" + host.getNodeName());

					
					HostData hd = new HostData();
//					System.out.println("Host id : "
//							+ host.getElementsByTagName("ID").item(0)
//							.getTextContent());
//					System.out.println("Host Name : "
//							+ host.getElementsByTagName("NAME").item(0)
//							.getTextContent());
//					System.out.println("CPU usage : "
//							+ host.getElementsByTagName("CPU_USAGE")
//									.item(0).getTextContent());
//					System.out.println("Max CPU : "
//							+ host.getElementsByTagName("MAX_CPU").item(0)
//									.getTextContent());
					hd.setId(host.getElementsByTagName("ID").item(0)
							.getTextContent());
					hd.setName(host.getElementsByTagName("NAME").item(0)
							.getTextContent());
					hd.setMaxCPU(Integer.parseInt(host
							.getElementsByTagName("MAX_CPU").item(0)
							.getTextContent()));
					hd.setCpuUsage(Integer.parseInt(host
							.getElementsByTagName("CPU_USAGE").item(0)
							.getTextContent()));
					hd.setMaxMemory(Integer.parseInt(host
							.getElementsByTagName("MAX_MEM").item(0)
							.getTextContent()));
					hd.setMemoryUsage(Integer.parseInt(host
							.getElementsByTagName("MEM_USAGE").item(0)
							.getTextContent()));
					hosts.add(hd);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return hosts;
	}
}
