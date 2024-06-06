package it.ipzs.qeaaissuer.oidclib.model;

import java.util.ArrayList;
import java.util.List;

public class CredentialDefinition {
	private List<String> type = new ArrayList<>();

	private CredentialSubjectDef credentialSubject;

	public List<String> getType() {
		return type;
	}

	public CredentialSubjectDef getCredentialSubject() {
		return credentialSubject;
	}

	public void setCredentialSubject(CredentialSubjectDef credentialSubject) {
		this.credentialSubject = credentialSubject;
	}

}