package it.ipzs.qeaaissuer.oidclib.model;

public class CredentialMDLSubject implements CredentialSubjectDef {

	private CredentialField given_name;

	private CredentialField family_name;

	private CredentialField birthdate;

	private CredentialField expiry_date;

	private CredentialField issue_date;

	private CredentialField issuing_authority;

	private CredentialField driving_privileges;

	private CredentialField issuing_country;

	private CredentialField un_distinguishing_sign;

	private CredentialField portrait;

	private CredentialField document_number;

	public CredentialField getGiven_name() {
		return given_name;
	}

	public void setGiven_name(CredentialField given_name) {
		this.given_name = given_name;
	}

	public CredentialField getFamily_name() {
		return family_name;
	}

	public void setFamily_name(CredentialField family_name) {
		this.family_name = family_name;
	}

	public CredentialField getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(CredentialField birthdate) {
		this.birthdate = birthdate;
	}

	public CredentialField getExpiry_date() {
		return expiry_date;
	}

	public void setExpiry_date(CredentialField expiry_date) {
		this.expiry_date = expiry_date;
	}

	public CredentialField getIssue_date() {
		return issue_date;
	}

	public void setIssue_date(CredentialField issue_date) {
		this.issue_date = issue_date;
	}

	public CredentialField getIssuing_authority() {
		return issuing_authority;
	}

	public void setIssuing_authority(CredentialField issuing_authority) {
		this.issuing_authority = issuing_authority;
	}

	public CredentialField getDriving_privileges() {
		return driving_privileges;
	}

	public void setDriving_privileges(CredentialField driving_privileges) {
		this.driving_privileges = driving_privileges;
	}

	public CredentialField getIssuing_country() {
		return issuing_country;
	}

	public void setIssuing_country(CredentialField issuing_country) {
		this.issuing_country = issuing_country;
	}

	public CredentialField getUn_distinguishing_sign() {
		return un_distinguishing_sign;
	}

	public void setUn_distinguishing_sign(CredentialField un_distinguishing_sign) {
		this.un_distinguishing_sign = un_distinguishing_sign;
	}

	public CredentialField getPortrait() {
		return portrait;
	}

	public void setPortrait(CredentialField portrait) {
		this.portrait = portrait;
	}

	public CredentialField getDocument_number() {
		return document_number;
	}

	public void setDocument_number(CredentialField document_number) {
		this.document_number = document_number;
	}

}