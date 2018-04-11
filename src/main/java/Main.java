import parser.*;

import static parser.XmlWoodStockConfig.Builder.xmlWoodStockConfig;

public class Main {

	private static final String XML_FILE = "E:\\projects\\lowmem\\src\\main\\java\\CIN_04039239500025.6819.xml";
	private static final String GRAPH1 = "E:\\projects\\lowmem\\src\\main\\java\\graph1.xml";
	private static final String GRAPH2 = "E:\\projects\\lowmem\\src\\main\\java\\graph2.xml";
	private static final String HARTMAN_FULL = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_full.xml";
	private static final String HARTMAN_SMALL = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small.xml";
	private static final String GRAPH_IDENTICAL = "E:\\projects\\lowmem\\src\\main\\java\\graphWithIdenticalNodes";

	private static final String STORY_XML_FILE = "C:\\Users\\eduard.parvu\\Desktop\\160222_Original_Artikelkatalog_IVF_Hartmann_AG_CH_CorByThzAndDch.xml";

	private static final String ENCODING = "UTF8";
	private static final String CONTAINING_TAG = "catalogueItemNotification";
	private static final String UNIQUE_IDENTIFIER_TAG = "gtin";
	private static final String UNIQUE_IDENTIFIER_CONTAINER_TAG = "tradeItemIdentification";
	private static final String DEPENDENCY_TAG = "gtin";
	private static final String DEPENDENCY_CONTAINER_TAG = "childTradeItem";
	private static final String ROOT_TAG = "tradeItemUnitDescriptor";
	private static final String ROOT_TAG_VALUE = "BASE_UNIT_OR_EACH";


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
				.build();

		XmlWoodStockIndexer indexer = new XmlWoodStockIndexer(config);

		indexer.index();

//		indexer.getNodeMap().forEach((key, value) -> {
//			CatalogNode catalogNode = indexer.getNodeMap().get(key);
//			if (catalogNode.getNodeDependencies().contains("04049500340625")) {
//				System.out.println(catalogNode.getUniqueIdentifier() + " has 04049500340625 as a dependency");
//			}
//		});


		XmlCatalogIterator xmlCatalogIterator = new XmlCatalogIterator(config, indexer.getNodeMap());

		while (xmlCatalogIterator.hasNext()) {
			CatalogNode catalogNode = xmlCatalogIterator.next();
//			System.out.println(indexer.getNodeMap().size());
//			System.out.println("***** NODE CONTENT *****");
//			System.out.println(catalogNode != null && catalogNode.getContent() != null ?
//					NodeUtil.toString(catalogNode.getContent(), true, true) : "Null for now");
//			System.out.println("**** NEXT NODE *******");
		}

		System.out.println("END");
	}
}
