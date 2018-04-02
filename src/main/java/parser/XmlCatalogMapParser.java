package parser;

import java.util.*;

public class XmlCatalogMapParser {

    private XmlWoodStockCatalogParser xmlWoodStockCatalogParser;

    private Map<String, CatalogNode> catalogNodeMap;

    private CatalogNode startCatalogNode;

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

        processUnfinishedRoots(catalogNode);

        return parseRootDependency(new CatalogNode(catalogNode), catalogNode, null);
    }

    private CatalogNode getNode() {
        // this reading has to be done every step in order
        // to progress with the node content inside the map
        // otherwise I would have used this expression in the final return statement
        CatalogNode catalogNode = xmlWoodStockCatalogParser.readNode();

        if (!unfinishedRoots.isEmpty()) {
            return catalogNodeMap.get(unfinishedRoots.peek());
        }

        visitNode(catalogNode.getUniqueIdentifier(), catalogNode.isRoot());

        return catalogNode;
    }

    private void visitNode(String nodeIdentifier, boolean isRoot) {
        if (isRoot) {
            unfinishedRoots.add(nodeIdentifier);
        } else {
            visitedNodes.add(nodeIdentifier);
        }
    }

    private CatalogNode processUnfinishedRoots(CatalogNode readNode) {

        if (!unfinishedRoots.isEmpty()) {

            CatalogNode nodeFromStack = new CatalogNode(catalogNodeMap.get(unfinishedRoots.peek()));

            if (!nodeFromStack.hasChildren()) {
                unfinishedRoots.pop();

                catalogNodeMap.remove(nodeFromStack.getUniqueIdentifier());

            } else {

                readNode = nodeFromStack;
            }
        }
        return readNode;
    }

    private CatalogNode parseRootDependency(CatalogNode root, CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && visitedNodes.contains(currentNode.getUniqueIdentifier())) {

            currentNode.setBaseContent(root.getContent());

            previousNode.removeDependency(currentNode.getUniqueIdentifier());

            catalogNodeMap.remove(currentNode.getUniqueIdentifier());

            visitedNodes.remove(currentNode.getUniqueIdentifier());

            return currentNode;

        } else if (!visitedNodes.contains(currentNode.getUniqueIdentifier())) {

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

        return parseRootDependency(root, catalogNodeMap.get(childNodeUniqueIdentifier.get()), currentNode);
    }


    public CatalogNode readNode2() {
        if (startCatalogNode == null) {
            startCatalogNode = xmlWoodStockCatalogParser.readNode();
        }

        if (startCatalogNode.isRoot() && !startCatalogNode.hasChildren()) {
            catalogNodeMap.remove(startCatalogNode.getUniqueIdentifier());
            CatalogNode returnCatalog = new CatalogNode(startCatalogNode);
            startCatalogNode = null;
            return returnCatalog;
        }

        if (startCatalogNode.isRoot()) {
            return parseNode2(startCatalogNode, null);
        }

        startCatalogNode = xmlWoodStockCatalogParser.readNode();
        return null;
    }

    private CatalogNode parseNode2(CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && currentNode.hasBeenRead()) {
            previousNode.removeDependency(currentNode.getUniqueIdentifier());
            catalogNodeMap.remove(currentNode.getUniqueIdentifier());
            return currentNode;
        } else if (!currentNode.hasChildren() && !currentNode.hasBeenRead()) {
            CatalogNode tmpLastRead = xmlWoodStockCatalogParser.readNode();
            if (currentNode.getUniqueIdentifier().equals(tmpLastRead.getUniqueIdentifier())) {
                previousNode.removeDependency(tmpLastRead.getUniqueIdentifier());
                catalogNodeMap.remove(tmpLastRead.getUniqueIdentifier());
                return tmpLastRead;
            }
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
        return parseNode2(catalogNodeMap.get(childNodeUniqueIdentifier.get()), currentNode);
    }
}
