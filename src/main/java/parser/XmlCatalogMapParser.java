package parser;

import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;

public class XmlCatalogMapParser {

    private static final String PARSE_EXP = "catalogueItem[1]/tradeItem/tradeItemInformation/tradingPartnerNeutralTradeItemInformation/packagingMaterial/packagingMaterialCompositionQuantity/measurementValue/@unitOfMeasure";

    private XmlWoodStockCatalogParser xmlWoodStockCatalogParser;

    private Map<String, CatalogNode> catalogNodeMap;

    private Collection<String> visitedNodes;

    private Stack<String> unfinishedRoots;

    public XmlCatalogMapParser(XmlWoodStockCatalogParser xmlWoodStockCatalogParser) {
        this.xmlWoodStockCatalogParser = xmlWoodStockCatalogParser;
        this.catalogNodeMap = xmlWoodStockCatalogParser.getCatalogNodeMap();
        this.visitedNodes = new ArrayList<>();
        this.unfinishedRoots = new Stack<>();
    }

    public boolean hasNext() {
        return !catalogNodeMap.isEmpty();
    }


    public CatalogNode readNode() {
        CatalogNode catalogNode = getNode();

        if (isRootAndLeaf(catalogNode)) {
            return catalogNode;
        }

        return catalogNode.isRoot() ? parseRootDependency(new CatalogNode(catalogNode), catalogNode, null) : null;
    }

    private CatalogNode getNode() {
        // this reading has to be done every step in order
        // to progress with the node content inside the map
        // otherwise I would have used this expression in the final return statement
        CatalogNode catalogNode = xmlWoodStockCatalogParser.readNode();

        visitNode(catalogNode.getUniqueIdentifier(), catalogNode.isRoot());

        if (!unfinishedRoots.isEmpty()) {
            return catalogNodeMap.get(unfinishedRoots.peek());
        }

        return catalogNode;
    }

    private boolean isRootAndLeaf(CatalogNode catalogNode) {
        if (catalogNode.isRoot() && !catalogNode.hasChildren()) {

            unfinishedRoots.pop();

            catalogNodeMap.remove(catalogNode.getUniqueIdentifier());

            return true;
        }

        return false;
    }

    private void visitNode(String nodeIdentifier, boolean isRoot) {
        if (isRoot) {
            unfinishedRoots.add(nodeIdentifier);
        } else {
            visitedNodes.add(nodeIdentifier);
        }
    }

    private CatalogNode parseRootDependency(CatalogNode root, CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && visitedNodes.contains(currentNode.getUniqueIdentifier())) {

//            currentNode.setBaseContent(root.getContent());

            Node n1 = currentNode.getContent();
            Document document = n1.getOwnerDocument();
            Node docNode = document.importNode(root.getContent(), true);
            docNode.appendChild(currentNode.getContent());
            currentNode.setContent(docNode);

            XPath xPath = new XPathFactoryImpl().newXPath();

            XPathExpression expression = null;
            try {
                expression = xPath.compile(PARSE_EXP);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("*** TEST XPATH ***");
                System.out.println(expression.evaluate(currentNode.getContent(), XPathConstants.STRING).toString());
                System.out.println("*** END TEST XPATH ***");
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

            previousNode.removeDependency(currentNode.getUniqueIdentifier());

            catalogNodeMap.remove(currentNode.getUniqueIdentifier());

            visitedNodes.remove(currentNode.getUniqueIdentifier());

            return currentNode;

        } else if (!currentNode.hasChildren() && !visitedNodes.contains(currentNode.getUniqueIdentifier())) {

            return null;
        }

        Optional<String> childNodeUniqueIdentifier = currentNode.getNodeDependencies().stream().findFirst();
        if (!childNodeUniqueIdentifier.isPresent()) {
            try {
                throw new Exception("Weird that the identifier is not stored");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CatalogNode testCatalogNode = catalogNodeMap.get(childNodeUniqueIdentifier.get());
//        if (testCatalogNode == null) {
//            System.out.println("Node with identifier: " + childNodeUniqueIdentifier.get() + " is not present in map ");
//        }

        return parseRootDependency(root, catalogNodeMap.get(childNodeUniqueIdentifier.get()), currentNode);
    }

}
