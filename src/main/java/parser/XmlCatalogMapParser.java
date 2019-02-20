package parser;

import com.google.common.collect.Multimap;
import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;

public class XmlCatalogMapParser {

    private static final String PARSE_EXP = "catalogueItem[last()]/tradeItem/tradeItemInformation/tradingPartnerNeutralTradeItemInformation/packagingMaterial/packagingMaterialCompositionQuantity/measurementValue/@unitOfMeasure";

    private static final String PARSE_EXP2 = "./*[1]/*[2]/catalogueItem/tradeItem/tradeItemInformation/tradingPartnerNeutralTradeItemInformation/packagingMaterial/packagingMaterialCompositionQuantity/measurementValue/@unitOfMeasure";

    private static final String T1 = "./catalogueItem[last()]/tradeItem/tradeItemUnitDescriptor";

    private static final String T2 = "catalogueItem[last()]/tradeItem/tradeItemUnitDescriptor";

    private static final String CONCAT = "concat(" + T2  + "," + T1 + ")";


    private XmlWoodStockCatalogParser xmlWoodStockCatalogParser;

    private Multimap<CatalogIdentifier, CatalogNode> catalogNodeMap;

    private Collection<CatalogIdentifier> visitedNodes;

    private Stack<CatalogIdentifier> unfinishedRoots;

    private int removedFromNodeTreeCounter = 0;

    private DocumentBuilder builder;


    public XmlCatalogMapParser(XmlWoodStockCatalogParser xmlWoodStockCatalogParser) {
        this.xmlWoodStockCatalogParser = xmlWoodStockCatalogParser;

        this.catalogNodeMap = xmlWoodStockCatalogParser.getCatalogNodeMap();

        this.visitedNodes = new ArrayList<>();

        this.unfinishedRoots = new Stack<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    // for zip files to continue from latest state
    public XmlCatalogMapParser(XmlWoodStockCatalogParser xmlWoodStockCatalogParser, Collection<CatalogIdentifier> visitedNodes, Stack<CatalogIdentifier> unfinishedRoots) {
        this.xmlWoodStockCatalogParser = xmlWoodStockCatalogParser;

        this.catalogNodeMap = xmlWoodStockCatalogParser.getCatalogNodeMap();

        if (visitedNodes != null) {
            this.visitedNodes = visitedNodes;
        } else {
            this.visitedNodes = new ArrayList<>();
        }

        if (unfinishedRoots != null) {
            this.unfinishedRoots = unfinishedRoots;
        } else {
            this.unfinishedRoots = new Stack<>();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    public boolean hasNext() {
        // condition that takes into account separate zip files that
        // have been read but the node tree has not been cleared yet
        // when one file was read
        return xmlWoodStockCatalogParser.hasNext() || !unfinishedRoots.isEmpty();
    }


    public Collection<CatalogIdentifier> getVisitedNodes() {
        return visitedNodes;
    }

    public Stack<CatalogIdentifier> getUnfinishedRoots() {
        return unfinishedRoots;
    }

    public CatalogNode readNode() {
        CatalogNode catalogNode = getNode();

        if (isRootAndLeaf(catalogNode)) {
            return catalogNode;
        }

        return  catalogNode.isRoot() ? parseRootDependency(catalogNode, null) : null;
    }

    private CatalogNode getNode() {
        // this reading has to be done every step in order
        // to progress with the node content inside the map
        // otherwise I would have used this expression in the final return statement
        CatalogNode catalogNode = null;

        if (xmlWoodStockCatalogParser.hasNext()) {
            catalogNode = xmlWoodStockCatalogParser.readNode();
        }

        if (catalogNode != null) {
            visitNode(catalogNode.getUniqueIdentifier(), catalogNode.isRoot());
        }

        if (!unfinishedRoots.isEmpty()) {
            catalogNode =  catalogNodeMap.get(unfinishedRoots.peek()).stream().findFirst().get();
        }

        return catalogNode;
    }

    private boolean isRootAndLeaf(CatalogNode catalogNode) {
        if (catalogNode.isRoot() && !catalogNode.hasChildren()) {

            unfinishedRoots.pop();

            catalogNodeMap.remove(catalogNode.getUniqueIdentifier(), catalogNode);

            removedFromNodeTreeCounter ++;

            return true;
        }

        return false;
    }

    private boolean isSingleNode(CatalogNode catalogNode) {
        if (!catalogNode.isRoot() && !catalogNode.hasChildren()) {
            visitedNodes.remove(catalogNode.getUniqueIdentifier());

            return true;
        }

        return false;
    }

    private void visitNode(CatalogIdentifier nodeIdentifier, boolean isRoot) {
        if (isRoot) {
            unfinishedRoots.add(nodeIdentifier);
        } else {
            visitedNodes.add(nodeIdentifier);
        }
    }

    private CatalogNode parseRootDependency(CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && visitedNodes.contains(currentNode.getUniqueIdentifier())) {

	        currentNode.setContent(createNewUnifiedDocument(currentNode.getContent(), previousNode.getContent()));

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

            catalogNodeMap.remove(currentNode.getUniqueIdentifier(), currentNode);

            removedFromNodeTreeCounter ++;

            visitedNodes.remove(currentNode.getUniqueIdentifier());

            return currentNode;

        } else if (!currentNode.hasChildren() && !visitedNodes.contains(currentNode.getUniqueIdentifier())) {

            return null;
        }

        Optional<CatalogIdentifier> childNodeUniqueIdentifier = currentNode.getNodeDependencies().stream().findFirst();
        if (!childNodeUniqueIdentifier.isPresent()) {
            try {
                throw new Exception("Weird that the identifier is not stored");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	    CatalogNode newCurrentNode = catalogNodeMap.get(childNodeUniqueIdentifier.get()).stream().findFirst().get();

        return parseRootDependency(newCurrentNode, currentNode);
    }


    private Node createNewUnifiedDocument(Node currentNode, Node previousNode) {
        Document combinedDocument = createDocumentWithRoot();

        combinedDocument.adoptNode(currentNode);
        combinedDocument.adoptNode(previousNode);

        Node root = combinedDocument.getFirstChild();

	    root.appendChild(previousNode);
        root.appendChild(currentNode);

        return root;
    }

    private Node addRootToNode(Node node) {

        if (node == null) {
            System.out.println();
        }

        Document document = createDocumentWithRoot();

        document.adoptNode(node);

        Node root = document.getFirstChild();

        root.appendChild(node);

        return root;
    }

    private Document createDocumentWithRoot() {
        Document document = builder.newDocument();

        Element root = document.createElement("root");
        document.appendChild(root);

        return document;
    }
}
