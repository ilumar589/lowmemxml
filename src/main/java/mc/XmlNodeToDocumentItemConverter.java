package mc;

import com.google.common.base.Function;
import parser.CatalogNode;

import static java.util.Objects.isNull;

public class XmlNodeToDocumentItemConverter implements Function<CatalogNode, DocumentItem> {
	private int itemIndex;

	public XmlNodeToDocumentItemConverter() {
		itemIndex = 0;
	}

	@Override
	public DocumentItem apply(CatalogNode node) {
		if (!isNull(node)) {
			itemIndex++;
			DocumentItem documentItem = new XmlDocumentItem(node.getContent());

			return documentItem;
		}

		return null;
	}
}
