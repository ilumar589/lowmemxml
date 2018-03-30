package parser;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;

public class CatalogNode {

	private boolean isRoot;
	private Node content;
	private Collection<String> nodeDependecies;

	public CatalogNode(boolean isRoot) {
		this.isRoot = isRoot;
		this.nodeDependecies = new ArrayList<>();
	}

	public void addDependency(String index) {
		nodeDependecies.add(index);
	}

	public void setContent(Node content) {
		this.content = content;
	}

	public Node getContent() {
		return this.content;
	}

	public boolean hasChildren() {
		return !nodeDependecies.isEmpty();
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean root) {
		isRoot = root;
	}
}
