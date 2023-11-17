package it.ipzs.qeaaissuer.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CredentialClaimsDto implements Serializable {

	private static final long serialVersionUID = 4019897014751749994L;

	@JsonProperty("given_name")
	private String givenName;

	@JsonProperty("unique_id")
	private String uniqueId;

	@JsonProperty("family_name")
	private String familyName;

	@JsonProperty("tax_id_number")
	private String taxIdNumber;

	@JsonProperty("birth_date")
	private String birthDate;

	@JsonProperty("place_of_birth")
	private PlaceOfBirthDto placeOfBirth;
}
