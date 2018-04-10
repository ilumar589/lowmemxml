package parser;

public class XmlWoodStockConfig {

	private String filePath;

	private String encoding;

	private String containingTag;

	private String uniqueIdentifierTag;

	private String uniqueIdentifierContainerTag;

	private String dependencyTag;

	private String dependencyContainerTag;

	private int dependencyContainerTagStackDistance;

	private int uniqueIdentifierContainerTagStackDistance;

	private String rootTag;

	private String rootTagValue;

	private String vendorProductNumberContainingTag;

	private String vendorProductNumberValueTag;

	private String vendorProductNumberTypeTag;

	private String vendorProductNumberTypeValue;

	private XmlWoodStockConfig(Builder builder) {
		this.filePath = builder.filePath;

		this.encoding = builder.encoding;

		this.containingTag = builder.containingTag;

		this.uniqueIdentifierTag = builder.uniqueIdentifierTag;

		this.uniqueIdentifierContainerTag = builder.uniqueIdentifierContainerTag;

		this.dependencyTag = builder.dependencyTag;

		this.dependencyContainerTag = builder.dependencyContainerTag;

		this.dependencyContainerTagStackDistance = builder.dependencyContainerTagStackDistance;

		this.uniqueIdentifierContainerTagStackDistance = builder.uniqueIdentifierContainerTagStackDistance;

		this.rootTag = builder.rootTag;

		this.rootTagValue = builder.rootTagValue;

		this.vendorProductNumberContainingTag = builder.vendorProductNumberContainingTag;

		this.vendorProductNumberValueTag = builder.vendorProductNumberValueTag;

		this.vendorProductNumberTypeTag = builder.vendorProductNumberTypeTag;

		this.vendorProductNumberTypeValue = builder.vendorProductNumberTypeValue;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getContainingTag() {
		return containingTag;
	}

	public String getUniqueIdentifierTag() {
		return uniqueIdentifierTag;
	}

	public String getUniqueIdentifierContainerTag() {
		return uniqueIdentifierContainerTag;
	}

	public String getDependencyTag() {
		return dependencyTag;
	}

	public String getDependencyContainerTag() {
		return dependencyContainerTag;
	}

	public int getDependencyContainerTagStackDistance() {
		return dependencyContainerTagStackDistance;
	}

	public int getUniqueIdentifierContainerTagStackDistance() {
		return uniqueIdentifierContainerTagStackDistance;
	}

	public String getRootTag() {
		return rootTag;
	}

	public String getRootTagValue() {
		return rootTagValue;
	}

	public String getVendorProductNumberContainingTag() {
		return vendorProductNumberContainingTag;
	}

	public String getVendorProductNumberValueTag() {
		return vendorProductNumberValueTag;
	}

	public String getVendorProductNumberTypeTag() {
		return vendorProductNumberTypeTag;
	}

	public String getVendorProductNumberTypeValue() {
		return vendorProductNumberTypeValue;
	}

	public static final class Builder {

		private String filePath;

		private String encoding;

		private String containingTag;

		private String uniqueIdentifierTag;

		private String uniqueIdentifierContainerTag;

		private String dependencyTag;

		private String dependencyContainerTag;

		private int dependencyContainerTagStackDistance;

		private int uniqueIdentifierContainerTagStackDistance;

		private String rootTag;

		private String rootTagValue;

		private String vendorProductNumberContainingTag;

		private String vendorProductNumberValueTag;

		private String vendorProductNumberTypeTag;

		private String vendorProductNumberTypeValue;

		public static Builder xmlWoodStockConfig() {return new Builder();}

		public Builder withFilePath(String filePath) {
			this.filePath = filePath;
			return this;
		}

		public Builder withEncoding(String encoding) {
			this.encoding = encoding;
			return this;
		}

		public Builder withContainingTag(String containingTag) {
			this.containingTag = containingTag;
			return this;
		}

		public Builder withUniqueIdentifierTag(String uniqueIdentifierTag) {
			this.uniqueIdentifierTag = uniqueIdentifierTag;
			return this;
		}

		public Builder withUniqueIdentifierContainerTag(String uniqueIdentifierContainerTag) {
			this.uniqueIdentifierContainerTag = uniqueIdentifierContainerTag;
			return this;
		}

		public Builder withDependencyTag(String dependencyTag) {
			this.dependencyTag = dependencyTag;
			return this;
		}

		public Builder withDependencyContainerTag(String dependencyContainerTag) {
			this.dependencyContainerTag = dependencyContainerTag;
			return this;
		}

		public Builder withDependencyContainerTagStackDistance(int dependencyContainerTagStackDistance) {
			this.dependencyContainerTagStackDistance = dependencyContainerTagStackDistance;
			return this;
		}

		public Builder withUniqueIdentifierContainerTagStackDistance(int uniqueIdentifierContainerTagStackDistance) {
			this.uniqueIdentifierContainerTagStackDistance = uniqueIdentifierContainerTagStackDistance;
			return this;
		}

		public Builder withRootTag(String rootTag) {
			this.rootTag = rootTag;
			return this;
		}

		public Builder withRootTagValue(String rootTagValue) {
			this.rootTagValue = rootTagValue;
			return this;
		}

		public Builder withVendorProductNumberContainingTag(String vendorProductNumberContainingTag) {
			this.vendorProductNumberContainingTag = vendorProductNumberContainingTag;
			return this;
		}

		public Builder withVendorProductNumberValueTag(String vendorProductNumberValueTag) {
			this.vendorProductNumberValueTag = vendorProductNumberValueTag;
			return this;
		}

		public Builder withVendorProductNumberTypeTag(String vendorProductNumberTypeTag) {
			this.vendorProductNumberTypeTag = vendorProductNumberTypeTag;
			return this;
		}

		public Builder withVendorProductNumberTypeValue(String vendorProductNumberTypeValue) {
			this.vendorProductNumberTypeValue = vendorProductNumberTypeValue;
			return this;
		}

		public XmlWoodStockConfig build() {
			return new XmlWoodStockConfig(this);
		}
	}
}
