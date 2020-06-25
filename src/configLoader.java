import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class configLoader {

	public static int players_per_game;
	public static int height;
	public static int width;

	public static void parseConfig() {
		try {
			File configFile = new File("assets/config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(configFile);
			doc.getDocumentElement().normalize();
			Node node = doc.getElementsByTagName("players_per_game").item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				players_per_game = Integer.parseInt(element.getTextContent());
			}
			node = doc.getElementsByTagName("maze").item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				//System.out.println("Height: " + element.getElementsByTagName("height").item(0).getTextContent());
				height = Integer.parseInt(element.getElementsByTagName("height").item(0).getTextContent());
				//System.out.println("Width: " + element.getElementsByTagName("width").item(0).getTextContent());
				width = Integer.parseInt(element.getElementsByTagName("width").item(0).getTextContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
