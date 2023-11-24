package it.ipzs.qeaaissuer.oidclib.model;

import lombok.Data;

@Data
public class CredentialEHICSubject implements CredentialSubjectDef {

	private CredentialField given_name;

	private CredentialField family_name;

	private CredentialField birthdate;

	private CredentialField place_of_birth;

	private CredentialField fiscal_code;

	private CredentialField province;

	private CredentialField sex;

	private CredentialField expiry_date;

	private CredentialField nation;

	private CredentialField document_number_team;

	private CredentialField institution_number_team;

}
