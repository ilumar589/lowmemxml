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

	private Multimap<String, CatalogNode> unfinishedDependencies;

	public XmlWoodStockIndexer(XmlWoodStockConfig config) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();
		this.unfinishedDependencies = ArrayListMultimap.create();

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

		CatalogNode currentCatalogNode = determineVendorProductNumber();

		determineDependency(currentCatalogNode);

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

	private void determineDependency(CatalogNode currentCatalogNode) {
		if (currentCatalogNode != null &&
				config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {

			checkCurrentNodeForUnfinishedDependencies(currentCatalogNode);

			readDependencyMap.put(reader.getText(), new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging));
		}
	}

	private void checkCurrentNodeForUnfinishedDependencies(CatalogNode currentNode) {

		// unfinished dependencies contain only the barcode as an identifier because
		// the child trade item only has that property while reading
		Collection<CatalogNode> dependantCreatedNodes = unfinishedDependencies.get(currentNode.getUniqueIdentifier().getBarcode());

		if (!dependantCreatedNodes.isEmpty()) {
			dependantCreatedNodes.forEach(node -> currentNode.addDependency(node.getUniqueIdentifier()));
		}
	}

	private void checkCurrentNodeDependencyStatus(String childBarcode, CatalogNode currentNode) {
		// check if the child has already been read
		// the only information for the child at this point is the barcode

	}


	private CatalogNode determineVendorProductNumber() {
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

				return generateNode();
			}
		}
		return null;
	}

	private CatalogNode generateNode() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {
			return nodeFactory.generateNode(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging), isLatestReadNodeRoot);
		}

		return null;
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
