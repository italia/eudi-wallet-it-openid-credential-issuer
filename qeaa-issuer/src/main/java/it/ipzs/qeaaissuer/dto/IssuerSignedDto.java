package it.ipzs.qeaaissuer.dto;

import java.util.List;
import java.util.Map;

public class IssuerSignedDto {

	private MobileSecurityObjectPayload issuerAuth;

	private Map<String, List<IssuerSignedItemDto>> nameSpaces;

	public MobileSecurityObjectPayload getIssuerAuth() {
		return issuerAuth;
	}

	public void setIssuerAuth(MobileSecurityObjectPayload issuerAuth) {
		this.issuerAuth = issuerAuth;
	}

	public Map<String, List<IssuerSignedItemDto>> getNameSpaces() {
		return nameSpaces;
	}

	public void setNameSpaces(Map<String, List<IssuerSignedItemDto>> nameSpaces) {
		this.nameSpaces = nameSpaces;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IssuerSignedDto [issuerAuth=");
		builder.append(issuerAuth);
		builder.append(", nameSpaces=");
		builder.append(nameSpaces);
		builder.append("]");
		return builder.toString();
	}

}
