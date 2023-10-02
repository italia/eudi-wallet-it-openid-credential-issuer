package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ParResponse implements Serializable {

	private static final long serialVersionUID = -883374915611532910L;

	@JsonProperty("request_uri")
	private String requestUri;

	@JsonProperty("expires_in")
	private int expiresIn;

}
