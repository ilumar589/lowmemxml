package parser;

import org.codehaus.stax2.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class XmlWoodStockCatalogParser {

	private XmlWoodStockConfig config;

	private Map<String, CatalogNode> catalogNodeMap;

	private XMLEventReader2 reader;

	private NodeFactory nodeFactory;

	private Stack<String> tagStack;

	private CatalogNode lastReadCatalogNode;

	public XmlWoodStockCatalogParser(XmlWoodStockConfig config, Map<String, CatalogNode> catalogNodeMap) {
		this.config = config;
		this.catalogNodeMap = catalogNodeMap;
		this.nodeFactory = new NodeFactory(config.getEncoding());
		this.tagStack = new Stack<>();

		XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2.newInstance();

		try {
			this.reader = (XMLEventReader2) factory.createXMLEventReader(new BufferedReader(new InputStreamReader(new FileInputStream(config.getFilePath()), config.getEncoding())));
		} catch (XMLStreamException | UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public CatalogNode readNode() {
		nodeFactory.readyForNextNode();
		boolean insideMatchingNode = false;

		try {
			while (reader.hasNextEvent()) {
				XMLEvent event = (XMLEvent) reader.next();

				if (event.isStartElement() && handleStartElement(event.asStartElement())) {
					insideMatchingNode = true;
				} else if (event.isEndElement() && handleEndElement(event.asEndElement())) {
					lastReadCatalogNode.setContent(nodeFactory.createNode());
					break;
				} else if (insideMatchingNode) {
					nodeFactory.writeEvent(event);

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

		return null;
	}

	public Map<String, CatalogNode> getCatalogNodeMap() {
		return catalogNodeMap;
	}

	private boolean handleStartElement(StartElement startElement) {
		String tag = startElement.getName().getLocalPart();
		if (config.getContainingTag().equalsIgnoreCase(tag)) {
			nodeFactory.writeEvent(startElement);
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

	private boolean handleEndElement(EndElement endElement) {
		if (config.getContainingTag().equalsIgnoreCase(endElement.getName().getLocalPart())) {
			nodeFactory.writeEvent(endElement);
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

		private XMLOutputFactory2 outputFactory;

		private HashMap<String, String> namespaceContext;

		private String encoding;

		private DocumentBuilder nodeBuilder;

		private NodeFactory(String encoding) {
			this.encoding = encoding;
			this.outputFactory = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			try {
				this.nodeBuilder = createNodeBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}

		private void readyForNextNode() {
			this.xmlOutputStream = new ByteArrayOutputStream();
			try {
				this.writer = outputFactory.createXMLEventWriter(this.xmlOutputStream, this.encoding);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		private void writeEvent(XMLEvent event) {
			try {
				writer.add(event);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		private DocumentBuilder createNodeBuilder() throws ParserConfigurationException {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(false);
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
