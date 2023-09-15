package it.ipzs.qeeaissuer.oidclib.model;

import java.util.List;

public class CredentialDefinition {
	private final List<String> type = List.of("PersonIdentificationData");

	private CredentialSubject credentialSubject;

	public List<String> getType() {
		return type;
	}

	public CredentialSubject getCredentialSubject() {
		return credentialSubject;
	}

	public void setCredentialSubject(CredentialSubject credentialSubject) {
		this.credentialSubject = credentialSubject;
	}

}