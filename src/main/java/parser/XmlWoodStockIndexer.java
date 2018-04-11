package parser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class XmlWoodStockIndexer {

	private XmlWoodStockConfig config;

	private XMLStreamReader2 reader;

	private Stack<String> tagStack;

	private NodeFactory nodeFactory;

	private String lastReadUniqueIdentifier;

	private boolean isLatestReadNodeRoot = false;

	public XmlWoodStockIndexer(XmlWoodStockConfig config) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();

		XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2.newInstance();

		try {
			this.reader = (XMLStreamReader2) factory.createXMLStreamReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void index() {
		int eventType;
		try {
			while (reader.hasNext()) {
				eventType = reader.next();

				switch (eventType) {
					case XMLEvent.START_ELEMENT: {
						handleStartElement();
					}break;
					case XMLEvent.CHARACTERS: {
						handleCharacters();
					}break;
					case XMLEvent.END_ELEMENT: {
						handleEndElement();
					}break;
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public Multimap<String, CatalogNode> getNodeMap() {
		return nodeFactory.getNodeMap();
	}

	private void handleStartElement() {
		tagStack.push(reader.getName().getLocalPart());
	}

	private void handleCharacters() {


		if (config.getRootTag().equalsIgnoreCase(tagStack.peek()) && config.getRootTagValue().equalsIgnoreCase(reader.getText())) {
			isLatestReadNodeRoot = true;
		} else if (config.getRootTag().equalsIgnoreCase(tagStack.peek()) && !config.getRootTagValue().equalsIgnoreCase(reader.getText())) {
			isLatestReadNodeRoot = false;
		}

		/**
		 * If the unique identifier tag is found a @CatalogNode
		 * is generated with this unique identifier and it is
		 * saved in order to be used later if a dependency tag
		 * is found
		 */
		if (!config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getUniqueIdentifierContainerTag().equalsIgnoreCase(getPreviousElement(config.getUniqueIdentifierContainerTagStackDistance())) &&
				config.getUniqueIdentifierTag().equalsIgnoreCase(tagStack.peek())) {

			String uniqueIdentifier = reader.getText();
			Collection<CatalogNode> catalogNodes = nodeFactory.getNode(uniqueIdentifier);
			if (catalogNodes.isEmpty() || (!catalogNodes.isEmpty() && catalogNodes.stream().noneMatch(CatalogNode::hasChildren))) {
				nodeFactory.generateNode(reader.getText(), (long) (catalogNodes.size() + 1), isLatestReadNodeRoot);
			} else {
				catalogNodes.forEach(catalogNode -> catalogNode.setRoot(isLatestReadNodeRoot));
			}
			lastReadUniqueIdentifier = uniqueIdentifier;
		}

		/**
		 * If while reading the xml tags inside our container tag
		 * we find a dependency we create a @CatalogNode with the unique
		 * identifier inside that dependency if it's not already
		 * created. Afterwards we add a dependency to this node
		 * which is the @lastReadUniqueIdentifier  found while
		 * reading previous tags inside out container tag. The
		 * graph will contain base units as root nodes and nodes
		 * that are dependent upon another node are children of
		 * that respective node
		 */
		if (config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {

			String dependencyUniqueIdentifier = reader.getText();
			Collection<CatalogNode> catalogNodes = nodeFactory.getNode(dependencyUniqueIdentifier);
			if (catalogNodes.isEmpty()) {
				CatalogNode catalogNode = nodeFactory.generateNode(dependencyUniqueIdentifier, (long) (catalogNodes.size() + 1),false);
			}

			Collection<CatalogNode> catalogNodes2 = nodeFactory.getNode(dependencyUniqueIdentifier);
			catalogNodes2.forEach(catalogNode -> catalogNode.addDependency(lastReadUniqueIdentifier));

			lastReadUniqueIdentifier = null;
		}
	}

	private void handleEndElement() {
		tagStack.pop();
	}

	private String getPreviousElement(int distance) {
		int tagStackSize = tagStack.size();
		if (tagStackSize < distance) {
			return null;
		}

		return tagStack.get(tagStackSize - distance);
	}

	private static final class NodeFactory {

		private Multimap<String, CatalogNode> nodeMap;

		private NodeFactory() {
			this.nodeMap = ArrayListMultimap.create();
		}

		private CatalogNode generateNode(String uniqueIdentifierValue, Long rank, boolean isRootNode) {
			CatalogNode catalogNode = new CatalogNode(uniqueIdentifierValue, rank, isRootNode);
			nodeMap.put(uniqueIdentifierValue, catalogNode);

			return catalogNode;
		}

		private Collection<CatalogNode> getNode(String uniqueIdentifier) {
			return nodeMap.get(uniqueIdentifier);
		}

		private Multimap<String, CatalogNode> getNodeMap() {
			return nodeMap;
		}

	}
}
