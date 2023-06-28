package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VerificationDto implements Serializable {

	private static final long serialVersionUID = -7579733851455483972L;

	@JsonProperty("trust_framework")
	private String trustFramework;

	@JsonProperty("assurance_level")
	private String assuranceLevel;

	private EvidenceDto evidence;

}
