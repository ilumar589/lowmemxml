package parser;

import java.util.Map;
import java.util.Optional;

public class XmlCatalogMapParser {

    private XmlWoodStockCatalogParser xmlWoodStockCatalogParser;

    private Map<String, CatalogNode> catalogNodeMap;

    private CatalogNode lastReadCatalogNode;

    public XmlCatalogMapParser(XmlWoodStockCatalogParser xmlWoodStockCatalogParser) {
        this.xmlWoodStockCatalogParser = xmlWoodStockCatalogParser;
        this.catalogNodeMap = xmlWoodStockCatalogParser.getCatalogNodeMap();
    }

    public boolean hasNext() {
        return !catalogNodeMap.isEmpty();
    }

    public CatalogNode readNode() {
        if (lastReadCatalogNode == null) {
            lastReadCatalogNode = xmlWoodStockCatalogParser.readNode();
        }

        if (lastReadCatalogNode.isRoot() && !lastReadCatalogNode.hasChildren()) {
            catalogNodeMap.remove(lastReadCatalogNode.getUniqueIdentifier());
            return lastReadCatalogNode;
        }

        if (lastReadCatalogNode.isRoot()) {
            return parseNode(lastReadCatalogNode, null);
        }

        return null;
    }

    private CatalogNode parseNode(CatalogNode currentNode, CatalogNode previousNode) {
        if (!currentNode.hasChildren() && currentNode.hasBeenRead()) {
            previousNode.removeDependency(currentNode.getUniqueIdentifier());
            catalogNodeMap.remove(currentNode.getUniqueIdentifier());
            return currentNode;
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
