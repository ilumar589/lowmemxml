package parser;

import org.codehaus.stax2.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class XmlWoodStockCatalogParser {

	private XmlWoodStockConfig config;

	private Map<String, CatalogNode> catalogNodeMap;

	private XMLEventReader reader;

	private NodeFactory nodeFactory;

	private Stack<String> tagStack;

	private CatalogNode lastReadCatalogNode;

	public XmlWoodStockCatalogParser(XmlWoodStockConfig config, Map<String, CatalogNode> catalogNodeMap) {
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
			this.reader = factory.createXMLEventReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
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

	public Map<String, CatalogNode> getCatalogNodeMap() {
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
		if (!config.getDependencyContainerTag().equalsIgnoreCase(getPreviousElement(config.getDependencyContainerTagStackDistance())) &&
				config.getUniqueIdentifierContainerTag().equalsIgnoreCase(getPreviousElement(config.getUniqueIdentifierContainerTagStackDistance())) &&
				config.getUniqueIdentifierTag().equalsIgnoreCase(tagStack.peek())) {

			lastReadCatalogNode = catalogNodeMap.get(characters.getData());
		}
	}

	private boolean handleEndElement(EndElement endElement) throws XMLStreamException {
		if (config.getContainingTag().equalsIgnoreCase(endElement.getName().getLocalPart())) {
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
