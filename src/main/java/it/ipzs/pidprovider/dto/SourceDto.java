package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SourceDto implements Serializable {

	private static final long serialVersionUID = -5567010555974595264L;

	@JsonProperty("organization_name")
	private String organizationName;

	@JsonProperty("organization_id")
	private String organizationId;

	@JsonProperty("country_code")
	private String countryCode;
}
