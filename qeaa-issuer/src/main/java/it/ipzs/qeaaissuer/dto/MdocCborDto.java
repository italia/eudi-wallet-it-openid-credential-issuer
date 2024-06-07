package it.ipzs.qeaaissuer.dto;

import java.util.List;

public class MdocCborDto {

	private int status;
	private String version;
	private List<MdocDocument> documents;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<MdocDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<MdocDocument> documents) {
		this.documents = documents;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MdocCborDto [status=");
		builder.append(status);
		builder.append(", version=");
		builder.append(version);
		builder.append(", documents=");
		builder.append(documents);
		builder.append("]");
		return builder.toString();
	}

}
