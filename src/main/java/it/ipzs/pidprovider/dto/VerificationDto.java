package it.ipzs.pidprovider.dto;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class VerificationDto implements Serializable {

	private static final long serialVersionUID = -7579733851455483972L;

	@JsonProperty("trust_framework")
	private String trustFramework;

	@JsonProperty("assurance_level")
	private String assuranceLevel;

	private Collection<Object> _sd;

}
