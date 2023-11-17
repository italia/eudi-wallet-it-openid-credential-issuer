package it.ipzs.qeaaissuer.dto;

public class MdocDocument {

	private String docType;

	private IssuerSignedDto issuerSigned;

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public IssuerSignedDto getIssuerSigned() {
		return issuerSigned;
	}

	public void setIssuerSigned(IssuerSignedDto issuerSigned) {
		this.issuerSigned = issuerSigned;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MdocDocument [docType=");
		builder.append(docType);
		builder.append(", issuerSigned=");
		builder.append(issuerSigned);
		builder.append("]");
		return builder.toString();
	}

}
