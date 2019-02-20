package parser;

import java.util.List;

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

	private String vendorProductNumberIgnoredTypeValue;




	// refactored values
	private String barcodeTag;
	private String childBarcodeTag;
	private String vendorProductNumberTag;
	private String vendorProductNumberTypeTagAndValue;
	private String packagingTag;
	private String childIdentifierTag;

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

		// refactored values

		this.barcodeTag = builder.barcodeTag;

		this.childBarcodeTag = builder.childBarcodeTag;

		this.vendorProductNumberTag = builder.vendorProductNumberTag;

		this.vendorProductNumberTypeTagAndValue = builder.vendorProductNumberTypeTagAndValue;

		this.packagingTag = builder.packagingTag;

		this.childIdentifierTag = builder.childIdentifierTag;
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

	public String getBarcodeTag() {
		return barcodeTag;
	}

	public void setBarcodeTag(String barcodeTag) {
		this.barcodeTag = barcodeTag;
	}

	public String getChildBarcodeTag() {
		return childBarcodeTag;
	}

	public void setChildBarcodeTag(String childBarcodeTag) {
		this.childBarcodeTag = childBarcodeTag;
	}

	public String getVendorProductNumberTag() {
		return vendorProductNumberTag;
	}

	public void setVendorProductNumberTag(String vendorProductNumberTag) {
		this.vendorProductNumberTag = vendorProductNumberTag;
	}

	public String getVendorProductNumberTypeTagAndValue() {
		return vendorProductNumberTypeTagAndValue;
	}

	public void setVendorProductNumberTypeTagAndValue(String vendorProductNumberTypeTagAndValue) {
		this.vendorProductNumberTypeTagAndValue = vendorProductNumberTypeTagAndValue;
	}

	public String getPackagingTag() {
		return packagingTag;
	}

	public void setPackagingTag(String packagingTag) {
		this.packagingTag = packagingTag;
	}

	public String getChildIdentifierTag() {
		return childIdentifierTag;
	}

	public void setChildIdentifierTag(String childIdentifierTag) {
		this.childIdentifierTag = childIdentifierTag;
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

		// refactored values
		private String barcodeTag;
		private String childBarcodeTag;
		private String vendorProductNumberTag;
		private String vendorProductNumberTypeTagAndValue;
		private String packagingTag;
		private String childIdentifierTag;


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


		//refactored values
		public Builder withBarcodeTag(String barcodeTag) {
			this.barcodeTag = barcodeTag;
			return this;
		}

		public Builder withChildBarcodeTag(String childBarcodeTag) {
			this.childBarcodeTag = childBarcodeTag;
			return this;
		}

		public Builder withVendorProductNumberTag(String vendorProductNumberTag) {
			this.vendorProductNumberTag = vendorProductNumberTag;
			return this;
		}

		public Builder withVendorProductNumberTypeTagAndValue(String vendorProductNumberTypeTagAndValue) {
			this.vendorProductNumberTypeTagAndValue = vendorProductNumberTypeTagAndValue;
			return this;
		}

		public Builder withChildIdentifierTag(String childIdentifierTag) {
			this.childIdentifierTag = childIdentifierTag;
			return this;
		}

		public Builder withPackagingTag(String packagingTag) {
			this.packagingTag = packagingTag;
			return this;
		}

		public XmlWoodStockConfig build() {
			return new XmlWoodStockConfig(this);
		}
	}
}
