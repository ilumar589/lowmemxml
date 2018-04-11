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

	private String lastReadPackaging;

	private String lastReadBarcode;

	private String lastReadVendorProductNumber;

	private String tempVendorProductNumber;

	private boolean isLatestReadNodeRoot = false;

	//-----------------------------------

	private Multimap<String, CatalogIdentifier> readDependencyMap;

	public XmlWoodStockIndexer(XmlWoodStockConfig config) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();
		this.readDependencyMap = ArrayListMultimap.create();

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

	public Map<CatalogIdentifier, CatalogNode> getNodeMap() {
		return nodeFactory.getNodeMap();
	}

	private void handleStartElement() {
		tagStack.push(reader.getName().getLocalPart());
	}

	private void handleCharacters() {
		determineRootNode();

		determineBarcode();

		determineVendorProductNumber();

		determineDependency2();

//		determineDependency();

	}

	private void determineRootNode() {
		if (config.getRootTag().equalsIgnoreCase(tagStack.peek())) {
			lastReadPackaging = reader.getText();

			isLatestReadNodeRoot = config.getRootTagValue().equalsIgnoreCase(lastReadPackaging);
		}
	}

	private void determineBarcode() {
		/**
		 * If the unique identifier tag is found a @CatalogNode
		 * is generated with this unique identifier and it is
		 * saved in order to be used later if a dependency tag
		 * is found
		 */
		if (!config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getUniqueIdentifierContainerTag().equalsIgnoreCase(getPreviousElement(config.getUniqueIdentifierContainerTagStackDistance())) &&
				config.getUniqueIdentifierTag().equalsIgnoreCase(tagStack.peek())) {

			lastReadBarcode = reader.getText();
		}
	}

	private void determineDependency2() {
		if (config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {

			readDependencyMap.put(reader.getText(), new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging));
		}
	}

	private void determineDependency() {
		/**
		 * If while reading the xml tags inside our container tag
		 * we find a dependency we create a @CatalogNode with the unique
		 * identifier inside that dependency if it's not already
		 * created. Afterwards we add a dependency to this node
		 * which is the @lastReadUniqueIdentifier  found while
		 * reading previous tags inside our container tag. The
		 * graph will contain base units as root nodes and nodes
		 * that are dependent upon another node are children of
		 * that respective node
		 */
		if (config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {

			String dependencyBarcode = reader.getText();

			if (dependencyBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {
				CatalogIdentifier catalogIdentifier = new CatalogIdentifier(dependencyBarcode, lastReadVendorProductNumber, lastReadPackaging);

				CatalogNode catalogNode = nodeFactory.getNode(catalogIdentifier);

				if (catalogNode == null) {
					catalogNode = nodeFactory.generateNode(new CatalogIdentifier(dependencyBarcode, lastReadVendorProductNumber, null), false);
				}

				catalogNode.addDependency(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging));
			}

		}
	}

	private void determineVendorProductNumber() {
		/** if we are inside the vendor product number containing tag**/
		if (config.getVendorProductNumberContainingTag().equalsIgnoreCase(getPreviousElement(2))) {

			/** the vendor product number values is found before the type so it has to be stored until
			 * we cant test that it's supplier assigned **/
			if (config.getVendorProductNumberValueTag().equalsIgnoreCase(tagStack.peek())) {
				tempVendorProductNumber = reader.getText();
			}

			/** if we reach the vendor product number type tag and it's value is supplier assigned **/
			if (config.getVendorProductNumberTypeTag().equalsIgnoreCase(tagStack.peek()) &&
					config.getVendorProductNumberTypeValue().equalsIgnoreCase(reader.getText())) {
				lastReadVendorProductNumber = tempVendorProductNumber;

				generateNode2();
//				generateNode();

			}
		}
	}

	private void generateNode2() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {
			CatalogIdentifier catalogIdentifier = new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging);

			Collection<CatalogIdentifier> dependencyIdentifier = readDependencyMap.get(lastReadBarcode);

			if (!dependencyIdentifier.isEmpty()) {
				CatalogNode currentNode = nodeFactory.generateNode(catalogIdentifier, false);
				dependencyIdentifier.forEach(currentNode::addDependency);

				readDependencyMap.removeAll(lastReadBarcode);

			} else {
				nodeFactory.generateNode(catalogIdentifier, isLatestReadNodeRoot);
			}
		}
	}

	private void generateNode() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {
			CatalogIdentifier catalogIdentifier = new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging);

			CatalogNode catalogNode = nodeFactory.getNode(catalogIdentifier);

			if (catalogNode == null) {

				catalogNode = nodeFactory.getNode(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, null));

				if (catalogNode != null) {
					// update identifier
					catalogIdentifier.setPackaging(lastReadPackaging);
				} else {
					nodeFactory.generateNode(catalogIdentifier, isLatestReadNodeRoot);
				}
			} else {
				/** update is root status **/
				catalogNode.setRoot(isLatestReadNodeRoot);
			}
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

		private Map<CatalogIdentifier, CatalogNode> nodeMap;

		private NodeFactory() {
			this.nodeMap = new HashMap<>();
		}

		private CatalogNode generateNode(CatalogIdentifier uniqueIdentifer, boolean isRootNode) {
			CatalogNode catalogNode = new CatalogNode(uniqueIdentifer, isRootNode);
			nodeMap.putIfAbsent(uniqueIdentifer, catalogNode);

			return catalogNode;
		}

		private CatalogNode getNode(CatalogIdentifier uniqueIdentifier) {
			return nodeMap.get(uniqueIdentifier);
		}

		private Map<CatalogIdentifier, CatalogNode> getNodeMap() {
			return nodeMap;
		}

	}
}
