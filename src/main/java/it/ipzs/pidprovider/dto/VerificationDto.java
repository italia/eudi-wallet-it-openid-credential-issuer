package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class VerificationDto implements Serializable {

	private static final long serialVersionUID = -7579733851455483972L;

	// snake_case is required by SD-JWT RFC

	private String trust_framework;

	private String assurance_level;

	private Object _sd;

}
