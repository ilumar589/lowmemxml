package parser;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CatalogIdentifier {

	private String barcode;

	private String vendorProductNumber;

	private String packaging;

	public CatalogIdentifier(String barcode, String vendorProductNumber, String packaging) {
		this.barcode = barcode;
		this.vendorProductNumber = vendorProductNumber;
		this.packaging = packaging;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getVendorProductNumber() {
		return vendorProductNumber;
	}

	public void setVendorProductNumber(String vendorProductNumber) {
		this.vendorProductNumber = vendorProductNumber;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CatalogIdentifier that = (CatalogIdentifier) o;

		return new EqualsBuilder()
				.append(barcode, that.barcode)
				.append(vendorProductNumber, that.vendorProductNumber)
				.append(packaging, that.packaging)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(barcode)
				.append(vendorProductNumber)
				.append(packaging)
				.toHashCode();
	}

	@Override
	public String toString() {
		return "CatalogIdentifier{" +
				"barcode='" + barcode + '\'' +
				", vendorProductNumber='" + vendorProductNumber + '\'' +
				", packaging='" + packaging + '\'' +
				'}';
	}
}
