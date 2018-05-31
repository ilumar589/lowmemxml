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

import static java.lang.Boolean.TRUE;

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

	// ---------------------------------

	private List<String> barcodePath;
	private List<String> childBarcodePath;
	private List<String> vendorProductNumberPath;
	private List<String> vendorProductNumberTypePath;
	private StringBuilder vendorProductNumberTypeValue;
	private List<String> packagingPath;
	private List<String> childIdentifier;

	public XmlWoodStockIndexer(XmlWoodStockConfig config) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();
		this.unfinishedDependencies = ArrayListMultimap.create();
		this.visitedNodes = ArrayListMultimap.create();

		setupTagPaths();

		XMLInputFactory factory =  XMLInputFactory.newInstance();

		try {
			this.reader = factory.createXMLStreamReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public XmlWoodStockIndexer(XmlWoodStockConfig config, InputStream inputStream) {
		this.config = config;
		this.tagStack = new Stack<>();
		this.nodeFactory = new NodeFactory();
		this.unfinishedDependencies = ArrayListMultimap.create();
		this.visitedNodes = ArrayListMultimap.create();

		XMLInputFactory factory =  XMLInputFactory.newInstance();

		try {
			this.reader = factory.createXMLStreamReader(new BufferedReader(new InputStreamReader(inputStream, config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException e) {
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
		determinePackaging();

		determineBarcode();

		determineVendorProductNumber();

		determineDependency();

	}

	private void determinePackaging() {
		if (checkTagExistence(packagingPath)) {
			lastReadPackaging = reader.getText().trim();
		}

//		if (config.getRootTag().equalsIgnoreCase(tagStack.peek())) {
//
//			lastReadPackaging = reader.getText().trim();
//
//			isLatestReadNodeRoot = config.getRootTagValue().equalsIgnoreCase(lastReadPackaging);
//		}
	}

	private void determineBarcode() {
		/**
		 * If the unique identifier tag is found a @CatalogNode
		 * is generated with this unique identifier and it is
		 * saved in order to be used later if a dependency tag
		 * is found
		 */

		if (checkTagExistence(barcodePath)) {
			lastReadBarcode  = reader.getText().trim();
		}

//		if (!config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
//				config.getUniqueIdentifierContainerTag().equalsIgnoreCase(getPreviousElement(config.getUniqueIdentifierContainerTagStackDistance())) &&
//				config.getUniqueIdentifierTag().equalsIgnoreCase(tagStack.peek())) {
//
//			lastReadBarcode = reader.getText().trim();
//		}
	}

	private void determineDependency() {

		if (checkTagExistence(childBarcodePath)) {
			CatalogIdentifier catalogIdentifier = new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging);

			Optional<CatalogNode> currentCatalogNode = nodeFactory.getNode(catalogIdentifier).stream().findFirst();
			if (!currentCatalogNode.isPresent()) {
				try {
					throw new Exception("Can't find node for: " + catalogIdentifier.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			CatalogNode node = currentCatalogNode.get();

			// the child was found so we update the root boolean
			node.setRoot(false);

			checkCurrentNodeForVisitedDependencies(reader.getText().trim(), node);
		}

//		if (config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
//				config.getDependencyTag().equalsIgnoreCase(tagStack.peek())) {
//
//			//TODO figure it out
//			CatalogIdentifier catalogIdentifier = new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging);
//			Optional<CatalogNode> currentCatalogNode = nodeFactory.getNode(catalogIdentifier).stream().findFirst();
//			if (!currentCatalogNode.isPresent()) {
//				try {
//					throw new Exception("Can't find node for: " + catalogIdentifier.toString());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			checkCurrentNodeForVisitedDependencies(reader.getText().trim(), currentCatalogNode.get());
//		}
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

		if (checkTagExistence(vendorProductNumberPath)) {
			tempVendorProductNumber = reader.getText().trim();
		}

		if (checkTagExistence(vendorProductNumberTypePath) && vendorProductNumberTypeValue.toString().equalsIgnoreCase(reader.getText().trim())) {
			lastReadVendorProductNumber = tempVendorProductNumber;

			generateNode();
		}

//		/** if we are inside the vendor product number containing tag**/
//		if (config.getVendorProductNumberContainingTag().equalsIgnoreCase(getPreviousElement(2))) {
//
//			/** the vendor product number values is found before the type so it has to be stored until
//			 * we cant test that it's supplier assigned **/
//			if (config.getVendorProductNumberValueTag().equalsIgnoreCase(tagStack.peek())) {
//				tempVendorProductNumber = reader.getText().trim();
//			}
//
//			/** if we reach the vendor product number type tag and it's value is supplier assigned **/
//			if (config.getVendorProductNumberTypeTag().equalsIgnoreCase(tagStack.peek()) &&
//					config.getVendorProductNumberTypeValue().equalsIgnoreCase(reader.getText().trim())) {
//				lastReadVendorProductNumber = tempVendorProductNumber;
//
//				generateNode();
//			}
//		}
	}

	private void generateNode() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {

			CatalogNode catalogNode = nodeFactory.generateNode(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging));

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

	private void setupTagPaths() {
		this.vendorProductNumberTypeValue = new StringBuilder();
		this.vendorProductNumberTypePath = new ArrayList<>();
		this.barcodePath = splitSetting(this.config.getBarcodeTag(), "/");
		this.childBarcodePath = splitSetting(this.config.getChildBarcodeTag(), "/");
		this.vendorProductNumberPath = splitSetting(this.config.getVendorProductNumberTag(), "/");
		this.packagingPath = splitSetting(this.config.getPackagingTag(), "/");

		setupTagPathAndValue(this.vendorProductNumberTypePath, this.vendorProductNumberTypeValue, this.config.getVendorProductNumberTypeTagAndValue());

		// TODO do value extraction in separate generic function
//		this.vendorProductNumberTypePath = splitSetting(this.config.getVendorProductNumberTypeTagAndValue(), "/");
//
//		String vpnTagWithValue = this.vendorProductNumberTypePath.get(this.vendorProductNumberTypePath.size() - 1);
//
//		List<String> splitVpnTagWithValue = splitSetting(vpnTagWithValue, "=");
//
//		this.vendorProductNumberTypeValue = splitVpnTagWithValue.get(splitVpnTagWithValue.size() - 1);
//
//		// last tag in vendorProductNumberTypePath must be replaced with the same tag without the value
//		// Again this must be done in a separate generic function
//
//		this.vendorProductNumberTypePath.set(this.vendorProductNumberTypePath.size() - 1, splitVpnTagWithValue.get(0));

		System.out.println(); // to set breakpoint

	}

	private void setupTagPathAndValue(List<String> tag, StringBuilder tagValue, String configValue) {
		tag.addAll(splitSetting(configValue, "/"));

		String pathWithValue = tag.get(tag.size() - 1);
		List<String> splitPathWithValue = splitSetting(pathWithValue, "=");

		tagValue.append(splitPathWithValue.get(splitPathWithValue.size() - 1));

		tag.set(tag.size() - 1, splitPathWithValue.get(0));

	}

	private List<String> splitSetting(String setting, String regex) {
		return Arrays.asList(setting.split(regex));
	}

	private boolean checkTagExistence(List<String> tagElements) {

		List<Boolean> conditionList = new ArrayList<>();

		// size - index
		int elementsSize = tagElements.size();
		for (int i = 0; i < elementsSize; i++) {
			if (tagElements.get(i).equalsIgnoreCase(getPreviousElement(elementsSize - i))) {
				conditionList.add(true);
			} else {
				conditionList.add(false);
			}
		}

		return conditionList.stream().allMatch(TRUE::equals);
	}

	private static final class NodeFactory {

		private Multimap<CatalogIdentifier, CatalogNode> nodeMap;

		private NodeFactory() {
			this.nodeMap = ArrayListMultimap.create();
		}

		private CatalogNode generateNode(CatalogIdentifier uniqueIdentifier) {
			// assume it's root on first generation
			CatalogNode catalogNode = new CatalogNode(uniqueIdentifier, true);

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
