package it.ipzs.pidprovider.oidclib.model;

public class CredentialSubject {

	private CredentialField given_name;

	private CredentialField family_name;

	private CredentialField birthdate;

	private CredentialField place_of_birth;

	private CredentialField tax_id_code;

	private CredentialField unique_id;

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

	public CredentialField getPlace_of_birth() {
		return place_of_birth;
	}

	public void setPlace_of_birth(CredentialField place_of_birth) {
		this.place_of_birth = place_of_birth;
	}

	public CredentialField getTax_id_code() {
		return tax_id_code;
	}

	public void setTax_id_code(CredentialField tax_id_code) {
		this.tax_id_code = tax_id_code;
	}

	public CredentialField getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(CredentialField unique_id) {
		this.unique_id = unique_id;
	}

}