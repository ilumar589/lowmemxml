import parser.*;

import static parser.XmlWoodStockConfig.Builder.xmlWoodStockConfig;

public class Main {

	private static final String XML_FILE = "E:\\projects\\lowmem\\src\\main\\java\\CIN_04039239500025.6819.xml";
	private static final String GRAPH1 = "D:\\WorkZone\\Projects\\lowmemxml\\src\\main\\java\\graph1.xml";
	private static final String GRAPH2 = "E:\\projects\\lowmem\\src\\main\\java\\graph2.xml";
	private static final String HARTMAN_FULL = "D:\\WorkZone\\Projects\\lowmemxml\\src\\main\\java\\IVFHartmann_GS1_example_full.xml";
	private static final String HARTMAN_SMALL = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small.xml";

	private static final String STORY_XML_FILE = "C:\\Users\\eduard.parvu\\Desktop\\160222_Original_Artikelkatalog_IVF_Hartmann_AG_CH_CorByThzAndDch.xml";

	private static final String ENCODING = "UTF8";
	private static final String CONTAINING_TAG = "catalogueItemNotification";
	private static final String UNIQUE_IDENTIFIER_TAG = "gtin";
	private static final String UNIQUE_IDENTIFIER_CONTAINER_TAG = "tradeItemIdentification";
	private static final String DEPENDENCY_TAG = "gtin";
	private static final String DEPENDENCY_CONTAINER_TAG = "childTradeItem";
	private static final String ROOT_TAG = "tradeItemUnitDescriptor";
	private static final String ROOT_TAG_VALUE = "BASE_UNIT_OR_EACH";
	private static final String VPN_CONTAINING_TAG = "additionalTradeItemIdentification";
	private static final String VPN_VALUE_TAG = "additionalTradeItemIdentificationValue";
	private static final String VPN_TYPE_TAG = "additionalTradeItemIdentificationType";
	private static final String VPN_TYPE_VALUE = "SUPPLIER_ASSIGNED";


	public static void main(String[] args) {
		XmlWoodStockConfig config = xmlWoodStockConfig()
				.withFilePath(HARTMAN_FULL)
				.withEncoding(ENCODING)
				.withContainingTag(CONTAINING_TAG)
				.withUniqueIdentifierTag(UNIQUE_IDENTIFIER_TAG)
				.withUniqueIdentifierContainerTag(UNIQUE_IDENTIFIER_CONTAINER_TAG)
				.withDependencyTag(DEPENDENCY_TAG)
				.withDependencyContainerTag(DEPENDENCY_CONTAINER_TAG)
				.withDependencyContainerTagStackDistance(3)
				.withUniqueIdentifierContainerTagStackDistance(2)
				.withRootTag(ROOT_TAG)
				.withRootTagValue(ROOT_TAG_VALUE)
				.withVendorProductNumberContainingTag(VPN_CONTAINING_TAG)
				.withVendorProductNumberValueTag(VPN_VALUE_TAG)
				.withVendorProductNumberTypeTag(VPN_TYPE_TAG)
				.withVendorProductNumberTypeValue(VPN_TYPE_VALUE)
				.build();

		XmlWoodStockIndexer indexer = new XmlWoodStockIndexer(config);

		indexer.index();

//		indexer.getNodeMap().forEach((key, value) -> {
//			CatalogNode catalogNode = indexer.getNodeMap().get(key);
//			catalogNode.getNodeDependencies().forEach(catalogIdentifier -> {
//				if (catalogIdentifier.getBarcode().equalsIgnoreCase("04049500340625")) {
//					System.out.println(catalogNode.getUniqueIdentifier() + " has 04049500340625 as a dependency");
//				}
//			});
//		});

//		indexer.getNodeMap().forEach((key, value) -> {
//			CatalogNode catalogNode = indexer.getNodeMap().get(key);
//			catalogNode.getNodeDependencies().forEach(catalogIdentifier -> {
//				if (catalogIdentifier.getBarcode().equalsIgnoreCase("04049500255424") &&
//						catalogIdentifier.getVendorProductNumber().equalsIgnoreCase("1696705")) {
//					System.out.println("04049500255424 and 1696705 exists after indexing and are part of catalog node " + catalogNode.getUniqueIdentifier());
//				}
//			});
//		});

//		indexer.getNodeMap().forEach((key, value) -> {
//			CatalogNode catalogNode = indexer.getNodeMap().get(key);
//			catalogNode.getNodeDependencies().forEach(catalogIdentifier -> {
//				if (catalogIdentifier.getBarcode().equalsIgnoreCase("04049500255431") &&
//						catalogIdentifier.getVendorProductNumber().equalsIgnoreCase("1698705")) {
//					System.out.println("04049500255431 and 1698705 exists after indexing and are part of catalog node " + catalogNode.getUniqueIdentifier());
//				}
//			});
//		});


		XmlCatalogIterator xmlCatalogIterator = new XmlCatalogIterator(config, indexer.getNodeMap());

		while (xmlCatalogIterator.hasNext()) {
			CatalogNode catalogNode = xmlCatalogIterator.next();
			System.out.println("***** NODE CONTENT *****");
			System.out.println(catalogNode != null && catalogNode.getContent() != null ?
					NodeUtil.toString(catalogNode.getContent(), true, true) : "Null for now");
			System.out.println("**** NEXT NODE *******");
		}

//		indexer.getNodeMap();
	}
}
