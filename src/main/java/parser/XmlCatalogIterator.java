package parser;

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class XmlCatalogIterator implements Iterator<CatalogNode>{

	private XmlCatalogMapParser catalogNodeMapParser;

	public XmlCatalogIterator(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap) {
		this.catalogNodeMapParser = new XmlCatalogMapParser(new XmlWoodStockCatalogParser(config, catalogNodeMap));
	}

	public XmlCatalogIterator(XmlWoodStockConfig config, Multimap<CatalogIdentifier,
							  CatalogNode> catalogNodeMap,
	                          InputStream inputStream,
	                          Collection<CatalogIdentifier> visitedNodes,
	                          Stack<CatalogIdentifier> unfinishedRoots) {
		this.catalogNodeMapParser = new XmlCatalogMapParser(new XmlWoodStockCatalogParser(config, catalogNodeMap, inputStream), visitedNodes, unfinishedRoots);
	}


	public Collection<CatalogIdentifier> getVisitedNodes() {
		return catalogNodeMapParser.getVisitedNodes();
	}

	public Stack<CatalogIdentifier> getUnfinishedRoots() {
		return catalogNodeMapParser.getUnfinishedRoots();
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
