package parser;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CatalogNode {

	private CatalogIdentifier uniqueIdentifier;
	private boolean isRoot;
	private Node content;
	private Collection<CatalogIdentifier> nodeDependencies;

	public CatalogNode(CatalogIdentifier uniqueIdentifier, boolean isRoot) {
		this.uniqueIdentifier = uniqueIdentifier;
		this.isRoot = isRoot;
		this.nodeDependencies = new ArrayList<>();
	}

	public CatalogNode(CatalogNode catalogNode) {
		this.uniqueIdentifier = catalogNode.getUniqueIdentifier();
		this.isRoot = catalogNode.isRoot();
		this.content = catalogNode.getContent();
		this.nodeDependencies = catalogNode.getNodeDependencies();
	}

	public void addDependency(CatalogIdentifier index) {
		nodeDependencies.add(index);
	}

	public void removeDependency(CatalogIdentifier uniqueIdentifier) {
		if (!nodeDependencies.contains(uniqueIdentifier)) {
			try {
				throw new Exception("Identifer " + uniqueIdentifier + " not in dependency list for node " + this.uniqueIdentifier);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		nodeDependencies.remove(uniqueIdentifier);
	}

	public Collection<CatalogIdentifier> getNodeDependencies() {
		return nodeDependencies;
	}

	public void setContent(Node content) {
		this.content = content;
	}

	public Node getContent() {
		return this.content;
	}

	public boolean hasChildren() {
		return !nodeDependencies.isEmpty();
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean root) {
		isRoot = root;
	}

	public boolean hasBeenRead() {
		return this.content != null;
	}

	public CatalogIdentifier getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(CatalogIdentifier uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
}
