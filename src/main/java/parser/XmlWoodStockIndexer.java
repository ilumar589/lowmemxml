package parser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;

public class XmlWoodStockIndexer {

	private XmlWoodStockConfig config;

	private XMLStreamReader reader;

	private Stack<String> tagStack;

	private NodeFactory nodeFactory;

	private String lastReadPackaging;

	private String lastReadBarcode;

	private String lastReadVendorProductNumber;

	private String tempVendorProductNumber;

	private boolean isLatestReadNodeRoot = false;

	//-----------------------------------

	private Multimap<String, CatalogNode> unfinishedDependencies;

	private Multimap<String, CatalogNode> visitedNodes;

	public XmlWoodStockIndexer(XmlWoodStockConfig config) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();
		this.unfinishedDependencies = ArrayListMultimap.create();
		this.visitedNodes = ArrayListMultimap.create();

		XMLInputFactory factory =  XMLInputFactory.newInstance();

		try {
			this.reader = factory.createXMLStreamReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
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

	public Multimap<CatalogIdentifier, CatalogNode> getNodeMap() {
		return nodeFactory.getNodeMap();
	}

	private void handleStartElement() {
		tagStack.push(reader.getName().getLocalPart());
	}

	private void handleCharacters() {
		determineRootNode();

		determineBarcode();

		determineVendorProductNumber();

		determineDependency();

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

	private void determineDependency() {
		if (config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {

			//TODO figure it out
			CatalogNode currentCatalogNode = nodeFactory.getNode(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging)).stream().findFirst().get();

			checkCurrentNodeForVisitedDependencies(reader.getText(), currentCatalogNode);
		}
	}

	private void checkCurrentNodeForUnfinishedDependencies(CatalogNode currentNode) {

		// unfinished dependencies contain only the barcode as an identifier because
		// the child trade item only has that property while reading
		Collection<CatalogNode> dependantCreatedNodes = unfinishedDependencies.get(currentNode.getUniqueIdentifier().getBarcode());

		if (!dependantCreatedNodes.isEmpty()) {
			dependantCreatedNodes.forEach(node -> {
				// check to see that the nodes belong to tha same product
				if (node.getUniqueIdentifier().getVendorProductNumber().equalsIgnoreCase(currentNode.getUniqueIdentifier().getVendorProductNumber())) {
					currentNode.addDependency(node.getUniqueIdentifier());
				}
			});
		}
	}

	private void checkCurrentNodeForVisitedDependencies(String childBarcode, CatalogNode currentNode) {
		// check if the child has already been read
		Collection<CatalogNode> visitedNodesWithSameBarcode = visitedNodes.get(childBarcode);

		if (!visitedNodesWithSameBarcode.isEmpty()) {
			// if the nodes are from the same product
			Optional<CatalogNode> alreadyVisitedNode = visitedNodesWithSameBarcode.stream()
					.filter(visitedNode -> visitedNode.getUniqueIdentifier().getVendorProductNumber().equalsIgnoreCase(currentNode.getUniqueIdentifier().getVendorProductNumber()))
					.findFirst();

			if (alreadyVisitedNode.isPresent()) {
				alreadyVisitedNode.get().addDependency(currentNode.getUniqueIdentifier());
			} else {
				unfinishedDependencies.put(childBarcode, currentNode);
			}
		} else {
			unfinishedDependencies.put(childBarcode, currentNode);
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

				generateNode();
			}
		}
	}

	private void generateNode() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {

			CatalogNode catalogNode = nodeFactory.generateNode(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging), isLatestReadNodeRoot);

			visitedNodes.put(lastReadBarcode, catalogNode);

			checkCurrentNodeForUnfinishedDependencies(catalogNode);
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

		private Multimap<CatalogIdentifier, CatalogNode> nodeMap;

		private NodeFactory() {
			this.nodeMap = ArrayListMultimap.create();
		}

		private CatalogNode generateNode(CatalogIdentifier uniqueIdentifier, boolean isRootNode) {
			CatalogNode catalogNode = new CatalogNode(uniqueIdentifier, isRootNode);

			nodeMap.put(uniqueIdentifier, catalogNode);

			return catalogNode;
		}

		private Collection<CatalogNode> getNode(CatalogIdentifier uniqueIdentifier) {
			return nodeMap.get(uniqueIdentifier);
		}

		private Multimap<CatalogIdentifier, CatalogNode> getNodeMap() {
			return nodeMap;
		}

	}
}
