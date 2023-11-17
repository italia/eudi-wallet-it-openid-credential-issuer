package it.ipzs.qeaaissuer.oidclib.model;

public class CredentialSubject implements CredentialSubjectDef {

	private CredentialField given_name;

	private CredentialField family_name;

	private CredentialField birthdate;

	private CredentialField expiration_date;

	private CredentialField serial_number;

	private CredentialField accompanying_person_right;

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

	public CredentialField getExpiration_date() {
		return expiration_date;
	}

	public void setExpiration_date(CredentialField expiration_date) {
		this.expiration_date = expiration_date;
	}

	public CredentialField getSerial_number() {
		return serial_number;
	}

	public void setSerial_number(CredentialField serial_number) {
		this.serial_number = serial_number;
	}

	public CredentialField getAccompanying_person_right() {
		return accompanying_person_right;
	}

	public void setAccompanying_person_right(CredentialField accompanying_person_right) {
		this.accompanying_person_right = accompanying_person_right;
	}

}