package parser;

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public class XmlCatalogIterator implements Iterator<CatalogNode>{

	private XmlCatalogMapParser catalogNodeMapParser;

	public XmlCatalogIterator(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap) {
		this.catalogNodeMapParser = new XmlCatalogMapParser(new XmlWoodStockCatalogParser(config, catalogNodeMap));
	}

	public XmlCatalogIterator(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap, InputStream inputStream) {
		this.catalogNodeMapParser = new XmlCatalogMapParser(new XmlWoodStockCatalogParser(config, catalogNodeMap, inputStream));
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
