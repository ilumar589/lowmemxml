package mc;


import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Collections;
import java.util.Set;

public class XmlDocumentItem extends AbstractDocumentItem {

	private Node xmlNode;
	private XPath xPath;

	public XmlDocumentItem(Node xmlNode) {
		this.xmlNode = xmlNode;
		this.xPath = new XPathFactoryImpl().newXPath();
	}

	@Override
	protected Object resolveValue(String value) {
		try {
			XPathExpression expression = xPath.compile(value);
			return expression.evaluate(getXmlNode(), XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			return null;
		}
	}

	public Node getXmlNode() {
		return xmlNode;
	}

	@Override
	public Set<String> getUnusedKeys() {
		return Collections.emptySet();
	}
}
