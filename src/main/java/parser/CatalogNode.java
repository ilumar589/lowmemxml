package parser;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;

public class CatalogNode implements TailCall{

	private Long rank;
	private String uniqueIdentifier;
	private boolean isRoot;
	private Node content;
	private Collection<String> nodeDependencies;

	public CatalogNode(String uniqueIdentifier, Long rank, boolean isRoot) {
		this.uniqueIdentifier = uniqueIdentifier;
		this.rank = rank;
		this.isRoot = isRoot;
		this.nodeDependencies = new ArrayList<>();
	}

	public CatalogNode(CatalogNode catalogNode) {
		this.uniqueIdentifier = catalogNode.getUniqueIdentifier();
		this.isRoot = catalogNode.isRoot();
		this.content = catalogNode.getContent();
		this.nodeDependencies = catalogNode.getNodeDependencies();
	}

	public void addDependency(String index) {
		nodeDependencies.add(index);
	}

	public void removeDependency(String uniqueIdentifier) {
		if (!nodeDependencies.contains(uniqueIdentifier)) {
			try {
				throw new Exception("Identifer " + uniqueIdentifier + " not in dependency list for node " + this.uniqueIdentifier);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		nodeDependencies.remove(uniqueIdentifier);
	}

	public Collection<String> getNodeDependencies() {
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

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}


	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CatalogNode that = (CatalogNode) o;

		return new EqualsBuilder()
				.append(rank, that.rank)
				.append(uniqueIdentifier, that.uniqueIdentifier)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(rank)
				.append(uniqueIdentifier)
				.toHashCode();
	}

	@Override
	public TailCall get() {
		return this;
	}

	@Override
	public boolean terminated() {
		return true;
	}

	@Override
	public String toString() {
		return "CatalogNode{" +
				"uniqueIdentifier='" + uniqueIdentifier + '\'' +
				", nodeDependencies=" + nodeDependencies +
				'}';
	}
}
