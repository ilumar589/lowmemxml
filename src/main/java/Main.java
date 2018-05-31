import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import mc.DocumentItem;
import mc.XmlDocumentItem;
import mc.XmlNodeToDocumentItemConverter;
import parser.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Objects.isNull;
import static parser.XmlWoodStockConfig.Builder.xmlWoodStockConfig;

public class Main {


	private static final String XML_FILE = "E:\\projects\\lowmem\\src\\main\\java\\CIN_04039239500025.6819.xml";
	private static final String GRAPH1 = "E:\\projects\\lowmem\\src\\main\\java\\graph1.xml";
	private static final String GRAPH2 = "E:\\projects\\lowmem\\src\\main\\java\\graph2.xml";
	private static final String HARTMAN_FULL = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_full.xml";
	private static final String HARTMAN_SMALL = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small.xml";

	private static final String STORY_XML_FILE = "C:\\Users\\eduard.parvu\\Desktop\\160222_Original_Artikelkatalog_IVF_Hartmann_AG_CH_CorByThzAndDch.xml";

	private static final String BIG_XML = "E:\\projects\\lowmem\\src\\main\\java\\171130_Original_Artikelkatalog_Lohman_A.xml";

	private static final String ZIP_1 = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small_chunk1.xml";
	private static final String ZIP_2 = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small_chunk2.xml";
	private static final String COMBINED_ZIP = "E:\\projects\\lowmem\\src\\main\\java\\combined_zip.xml";

	private static final String HARTMAN_ZIP = "E:\\projects\\lowmem\\src\\main\\java\\IVFHartmann_GS1_example_small_split.zip";

	private static final String ENCODING = "UTF8";
	private static final String CONTAINING_TAG = "catalogueItem";
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


	// refactor settings
	// check with a maximum of 3 stack distance
	private static final String BARCODE_TAG = "tradeItem/tradeItemIdentification/gtin";
	private static final String CHILD_BARCODE_TAG = "childTradeItem/tradeItemIdentification/gtin";
	private static final String VENDOR_PRODUCT_NUMBER_TAG = "additionalTradeItemIdentification/additionalTradeItemIdentificationValue";
	private static final String VENDOR_PRODUCT_NUMBER_TYPE_TAG_AND_VALUE = "additionalTradeItemIdentification/additionalTradeItemIdentificationType=SUPPLIER_ASSIGNED";
	private static final String PACKAGING_TAG = "tradeItemUnitDescriptor";
	private static final String CHILD_IDENTIFIER_TAG = "childTradeItem";

	public static void main(String[] args) {
		XmlWoodStockConfig config = xmlWoodStockConfig()
				.withFilePath(HARTMAN_SMALL)
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

				// refactored values
				.withBarcodeTag(BARCODE_TAG)
				.withChildBarcodeTag(CHILD_BARCODE_TAG)
				.withVendorProductNumberTag(VENDOR_PRODUCT_NUMBER_TAG)
				.withVendorProductNumberTypeTagAndValue(VENDOR_PRODUCT_NUMBER_TYPE_TAG_AND_VALUE)
				.withPackagingTag(PACKAGING_TAG)
//				.withChildIdentifierTag(CHILD_IDENTIFIER_TAG)
				.build();

		// ----- ZIP TEST -----
//		ZipFile zipFile = null;
//		try {
//			zipFile = new ZipFile(HARTMAN_ZIP);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		Vector<Multimap<CatalogIdentifier, CatalogNode>> wholeZipIndex = new Vector<>();
//
//		if (zipFile != null) {
//			Enumeration<? extends ZipEntry> entries = zipFile.entries();
//			while (entries.hasMoreElements()) {
//				ZipEntry zipEntry = entries.nextElement();
//				try {
//					XmlWoodStockIndexer indexer = new XmlWoodStockIndexer(config, zipFile.getInputStream(zipEntry));
//
//					indexer.index();
//					wholeZipIndex.add(indexer.getNodeMap());
//
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		Multimap<CatalogIdentifier, CatalogNode> completeIndex = combine(wholeZipIndex);

//		int oneByte;
//		try {
//			while ((oneByte = sequenceInputStream.read()) != -1) {
//				System.out.write(oneByte);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.flush();


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

		// ----- ZIP TEST -----
//		if (zipFile != null) {
//
//			Collection<CatalogIdentifier> visitedNodes = null;
//
//			Stack<CatalogIdentifier> unfinishedRoots = null;
//
//			Enumeration<? extends ZipEntry> entries2 = zipFile.entries();
//			while (entries2.hasMoreElements()) {
//				ZipEntry entry = entries2.nextElement();
//
//				try {
//					XmlCatalogIterator xmlCatalogIterator = new XmlCatalogIterator(config, completeIndex, zipFile.getInputStream(entry), visitedNodes, unfinishedRoots);
//
//					while (xmlCatalogIterator.hasNext()) {
//						CatalogNode catalogNode = xmlCatalogIterator.next();
//						System.out.println("***** NODE CONTENT *****");
//						System.out.println(catalogNode != null && catalogNode.getContent() != null ?
//							NodeUtil.toString(catalogNode.getContent(), true, true) : "Null for now");
//						System.out.println("**** NEXT NODE *******");
//				}
//
//				// preserve state for next file
//					visitedNodes = xmlCatalogIterator.getVisitedNodes();
//					unfinishedRoots = xmlCatalogIterator.getUnfinishedRoots();
//
//				} catch (IOException e) {
//					e.printStackTrace();
//					}
//				}
//
//			try {
//				zipFile.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}

		XmlWoodStockIndexer indexer = new XmlWoodStockIndexer(config);

		indexer.index();

		XmlCatalogIterator xmlCatalogIterator = new XmlCatalogIterator(config, indexer.getNodeMap());

		while (xmlCatalogIterator.hasNext()) {
			CatalogNode catalogNode = xmlCatalogIterator.next();
			System.out.println("***** NODE CONTENT *****");
			System.out.println(catalogNode != null && catalogNode.getContent() != null ?
					NodeUtil.toString(catalogNode.getContent(), true, true) : "Null for now");
			System.out.println("**** NEXT NODE *******");
		}

//		indexer.getNodeMap();

		// ------------------ SAME AS IN MC PROJECT -------------------

//		Iterator<DocumentItem> allValues = Iterators.transform(xmlCatalogIterator, new XmlNodeToDocumentItemConverter());
//
//		Iterator<DocumentItem> remainingValues = removeNullValues(allValues);
//
//		while (remainingValues.hasNext()) {
//			XmlDocumentItem documentItem = (XmlDocumentItem) remainingValues.next();
//			System.out.println(NodeUtil.toString(documentItem.getXmlNode(), true, true));
//		}
	}

	private static Iterator<DocumentItem> removeNullValues(Iterator<DocumentItem> documentItemIterator) {
		List<DocumentItem> documentItems = new ArrayList<>();
		documentItemIterator.forEachRemaining(documentItem -> {
			if (!isNull(documentItem)) {
				documentItems.add(documentItem);
			} else {
			}
		});

		return documentItems.iterator();
	}

	private static Multimap<CatalogIdentifier, CatalogNode> combine(Vector<Multimap<CatalogIdentifier, CatalogNode>> multimaps) {
		Multimap<CatalogIdentifier, CatalogNode> combined = ArrayListMultimap.create();  // or whatever kind you'd like

		multimaps.forEach(combined::putAll);

		return combined;
	}
}
