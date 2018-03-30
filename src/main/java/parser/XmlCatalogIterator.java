package parser;

import java.util.Iterator;
import java.util.Map;

public class XmlCatalogIterator implements Iterator<CatalogNode>{

	private XmlWoodStockCatalogParser parser;

	private CatalogNode nextNode;

	public XmlCatalogIterator(XmlWoodStockConfig config, Map<String, CatalogNode> catalogNodeMap) {
		this.parser = new XmlWoodStockCatalogParser(config, catalogNodeMap);
		this.nextNode = next();
	}


	@Override
	public boolean hasNext() {
		if (nextNode == null) {
			return false;
		}

		return true;
	}

	@Override
	public CatalogNode next() {
		return parser.readNode();
	}
}
