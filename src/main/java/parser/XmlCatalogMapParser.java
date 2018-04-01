package parser;

import java.util.Map;
import java.util.Optional;

public class XmlCatalogMapParser {

    private XmlWoodStockCatalogParser xmlWoodStockCatalogParser;

    private Map<String, CatalogNode> catalogNodeMap;

    private CatalogNode startCatalogNode;

    public XmlCatalogMapParser(XmlWoodStockCatalogParser xmlWoodStockCatalogParser) {
        this.xmlWoodStockCatalogParser = xmlWoodStockCatalogParser;
        this.catalogNodeMap = xmlWoodStockCatalogParser.getCatalogNodeMap();
    }

    public boolean hasNext() {
        return !catalogNodeMap.isEmpty();
    }

    public CatalogNode readNode() {
        if (startCatalogNode == null) {
            startCatalogNode = xmlWoodStockCatalogParser.readNode();
        }

        if (startCatalogNode.isRoot() && !startCatalogNode.hasChildren()) {
            catalogNodeMap.remove(startCatalogNode.getUniqueIdentifier());
            return startCatalogNode;
        }

        if (startCatalogNode.isRoot()) {
            return parseNode(startCatalogNode, null);
        }

        return null;
    }

    private CatalogNode parseNode(CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && currentNode.hasBeenRead()) {
            previousNode.removeDependency(currentNode.getUniqueIdentifier());
            catalogNodeMap.remove(currentNode.getUniqueIdentifier());
            return currentNode;
        } else if (!currentNode.hasChildren() && !currentNode.hasBeenRead()) {
            CatalogNode tmpLastRead = xmlWoodStockCatalogParser.readNode();
            if (currentNode.getUniqueIdentifier().equals(tmpLastRead.getUniqueIdentifier())) {
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
        return parseNode(catalogNodeMap.get(childNodeUniqueIdentifier.get()), currentNode);
    }
}
