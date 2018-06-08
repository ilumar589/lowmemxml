package parser;

import com.google.common.collect.Multimap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.util.*;

import static java.lang.Boolean.TRUE;

public class XmlWoodStockCatalogParser {

	private XmlWoodStockConfig config;

	private Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap;

	private XMLEventReader reader;

	private NodeFactory nodeFactory;

	private Stack<String> tagStack;

	private CatalogNode lastReadCatalogNode;

	private String lastReadBarcode;

	private String lastReadPackaging;

	private String lastReadVendorProductNumber;

	private String tempVendorProductNumber;

// ---------------------------------

	private List<String> barcodePath;
	private List<String> vendorProductNumberPath;
	private List<String> vendorProductNumberTypePath;
	private StringBuilder vendorProductNumberTypeValue;
	private List<String> packagingPath;

	private String savedVendorProductNumberAttribute;

	public XmlWoodStockCatalogParser(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap) {
		this.config = config;

		this.catalogNodeMap = catalogNodeMap;

		try {
			this.nodeFactory = new NodeFactory(config.getEncoding());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		this.tagStack = new Stack<>();

		setupTagPaths();

		XMLInputFactory factory = XMLInputFactory.newInstance();

		try {
			this.reader = factory.createXMLEventReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public XmlWoodStockCatalogParser(XmlWoodStockConfig config, Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap, InputStream inputStream) {
		this.config = config;

		this.catalogNodeMap = catalogNodeMap;

		try {
			this.nodeFactory = new NodeFactory(config.getEncoding());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		this.tagStack = new Stack<>();

		XMLInputFactory factory = XMLInputFactory.newInstance();

		try {
			this.reader = factory.createXMLEventReader(new BufferedReader(new InputStreamReader(inputStream, config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public boolean hasNext() {
		return reader.hasNext();
	}

	public CatalogNode readNode() {
		nodeFactory.readyForNextNode();
		boolean insideMatchingNode = false;

		try {
			while (reader.hasNext()) {
				XMLEvent event = (XMLEvent) reader.next();
				if (event.isStartElement() && handleStartElement(event.asStartElement())) {
					insideMatchingNode = true;
				} else if (event.isEndElement() && handleEndElement(event.asEndElement())) {
					lastReadCatalogNode.setContent(nodeFactory.createNode());
					break;

				} else if (insideMatchingNode) {
					nodeFactory.add(event);
					switch (event.getEventType()) {
						case XMLEvent.START_ELEMENT: {
							tagStack.push(event.asStartElement().getName().getLocalPart());

							String type = null;

							Iterator<Attribute> attributeIterator = event.asStartElement().getAttributes();
							if (attributeIterator.hasNext()) {
								type = attributeIterator.next().getValue();
							}

							/* save vendor product number tag type attribute */
							if (checkTagExistence(vendorProductNumberTypePath) && type != null && vendorProductNumberTypeValue.toString().equalsIgnoreCase(type)) {
								savedVendorProductNumberAttribute = type;
							}

						}break;
						case XMLEvent.END_ELEMENT: {
							if (!tagStack.isEmpty()) {
								tagStack.pop();
							}
						}break;
					}
				}

				if (event.isCharacters()) {
					handleCharacters(event.asCharacters());
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return lastReadCatalogNode;
	}

	public Multimap<CatalogIdentifier, CatalogNode> getCatalogNodeMap() {
		return catalogNodeMap;
	}

	private boolean handleStartElement(StartElement startElement) throws XMLStreamException {
		nodeFactory.addNamespaces(startElement);

		String tag = startElement.getName().getLocalPart();

		if (config.getContainingTag().equalsIgnoreCase(tag)) {

			nodeFactory.addRoot(startElement);

			tagStack.push(tag);

			return true;
		}

		return false;
	}

	private void handleCharacters(Characters characters) {

		determinePackaging(characters.getData().trim());

		determineBarcode(characters.getData().trim());

		determineVendorProductNumber(characters.getData().trim());

	}

	private void determinePackaging(String text) {
		if (!tagStack.isEmpty() && checkTagExistence(packagingPath)) {
			lastReadPackaging = text;
		}

//		if (!tagStack.isEmpty() && config.getRootTag().equalsIgnoreCase(tagStack.peek())) {
//			lastReadPackaging = text;
//		}
	}

	private void setLastNode() {
		if (lastReadBarcode != null && lastReadVendorProductNumber != null && lastReadPackaging != null) {

			//TODO figure it out
			Optional<CatalogNode> foundNode = catalogNodeMap.get(new CatalogIdentifier(lastReadBarcode, lastReadVendorProductNumber, lastReadPackaging)).stream().findFirst();

			if (!foundNode.isPresent()) {
				System.out.println("No node found for barcode: " + lastReadBarcode + " and vendor product number: " + lastReadVendorProductNumber);
			} else {
				lastReadCatalogNode = foundNode.get();
			}
		}
	}

	private boolean handleEndElement(EndElement endElement) throws XMLStreamException {
		if (config.getContainingTag().equalsIgnoreCase(endElement.getName().getLocalPart())) {
			setLastNode();
			nodeFactory.add(endElement);
			return true;
		}
		return false;
	}

	private String getPreviousElement(int distance) {
		int tagStackSize = tagStack.size();
		if (tagStackSize < distance) {
			return null;
		}

		return tagStack.get(tagStackSize - distance);
	}

	private void determineBarcode(String text) {
		/**
		 * If the unique identifier tag is found a @CatalogNode
		 * is generated with this unique identifier and it is
		 * saved in order to be used later if a dependency tag
		 * is found
		 */

		if (checkTagExistence(barcodePath)) {
			lastReadBarcode  = removeLeadingZeros(text);
		}

//		if (!config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
//				config.getUniqueIdentifierContainerTag().equalsIgnoreCase(getPreviousElement(config.getUniqueIdentifierContainerTagStackDistance())) &&
//				config.getUniqueIdentifierTag().equalsIgnoreCase(tagStack.peek())) {
//
//			lastReadBarcode = removeLeadingZeros(text);
//		}
	}

	private void determineVendorProductNumber(String text) {

		if (checkTagExistence(vendorProductNumberPath)) {
			tempVendorProductNumber = text;
		}

		/* vendor product type is declared either as a separate tag with the tag value being
		* SUPPLIER_ASSIGNED/somethig else or as a single tag with the type being a tag attribute*/
		if (checkTagExistence(vendorProductNumberTypePath) &&
				(vendorProductNumberTypeValue.toString().equalsIgnoreCase(text) || savedVendorProductNumberAttribute != null)) {

			lastReadVendorProductNumber = tempVendorProductNumber;

			savedVendorProductNumberAttribute = null;

//			tempVendorProductNumber == null ? reader.getText() :
		}


//		/** if we are inside the vendor product number containing tag**/
//		if (config.getVendorProductNumberContainingTag().equalsIgnoreCase(getPreviousElement(2))) {
//
//			/** the vendor product number values is found before the type so it has to be stored until
//			 * we cant test that it's supplier assigned **/
//			if (config.getVendorProductNumberValueTag().equalsIgnoreCase(tagStack.peek())) {
//				tempVendorProductNumber = text;
//			}
//
//			/** if we reach the vendor product number type tag and it's value is supplier assigned **/
//			if (config.getVendorProductNumberTypeTag().equalsIgnoreCase(tagStack.peek()) &&
//					config.getVendorProductNumberTypeValue().equalsIgnoreCase(text)) {
//				lastReadVendorProductNumber = tempVendorProductNumber;
//
//				setLastNode();
//			}
//
//		}
	}

	private void setupTagPaths() {
		this.vendorProductNumberTypeValue = new StringBuilder();
		this.vendorProductNumberTypePath = new ArrayList<>();
		this.barcodePath = splitSetting(this.config.getBarcodeTag(), "/");
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

	private String removeLeadingZeros(String barcode) {
		return barcode.replaceAll("^(0+(?!$))", "");
	}

	private static final class NodeFactory {

		private ByteArrayOutputStream xmlOutputStream;

		private XMLEventWriter writer;

		private XMLOutputFactory outputFactory;

		private XMLEventFactory xmlEventFactory;

		private HashMap<String, String> namespaceContext;

		private String encoding;

		private DocumentBuilder nodeBuilder;

		private NodeFactory(String encoding) throws ParserConfigurationException {
			this.encoding = encoding;

			this.outputFactory =  XMLOutputFactory.newInstance();

			this.xmlEventFactory = XMLEventFactory.newFactory();

			this.namespaceContext = new HashMap<>();

			this.nodeBuilder = createNodeBuilder();

		}


		private void addNamespaces(StartElement startElement) {
			Iterator<? extends Namespace> namespaces = startElement.getNamespaces();
			namespaces.forEachRemaining(ns -> namespaceContext.put(ns.getPrefix(), ns.getNamespaceURI()));
		}

		private void readyForNextNode() {
			this.xmlOutputStream = new ByteArrayOutputStream();
			try {
				this.writer = outputFactory.createXMLEventWriter(this.xmlOutputStream, this.encoding);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		void addRoot(StartElement rootElement) throws XMLStreamException {
			add(rootElement);
			for (String prefix : namespaceContext.keySet()) {
				writer.setPrefix(prefix, namespaceContext.get(prefix));
				writer.add(xmlEventFactory.createNamespace(prefix, namespaceContext.get(prefix)));
			}
		}

		private void add(XMLEvent event) throws XMLStreamException {
			writer.add(event);
		}

		private DocumentBuilder createNodeBuilder() throws ParserConfigurationException {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			return documentBuilderFactory.newDocumentBuilder();
		}

		Node createNode() throws XMLStreamException {
			this.writer.close();
			try {
				ByteArrayInputStream nodeStream = new ByteArrayInputStream(xmlOutputStream.toByteArray());
				return nodeBuilder.parse(nodeStream).getFirstChild();
			} catch (SAXException | IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

}
