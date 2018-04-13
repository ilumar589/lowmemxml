package parser;

import com.google.common.collect.Multimap;

import java.util.Iterator;
import java.util.Map;

public class XmlCatalogIterator implements Iterator<CatalogNode>{

	private XmlCatalogMapParser catalogNodeMapParser;

	public XmlCatalogIterator(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap) {
		this.catalogNodeMapParser = new XmlCatalogMapParser(new XmlWoodStockCatalogParser(config, catalogNodeMap));
	}


	@Override
	public boolean hasNext() {
		return catalogNodeMapParser.hasNext();
	}

	@Override
	public CatalogNode next() {
		return catalogNodeMapParser.readNode();
	}
}
