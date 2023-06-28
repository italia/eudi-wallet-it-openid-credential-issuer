package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CredentialResponse implements Serializable {

	private static final long serialVersionUID = -7608698829415542129L;

	private String format;
	private String credential;

	@JsonProperty("c_nonce")
	private String nonce;

	@JsonProperty("c_nonce_expires_in")
	private int nonceExpiresIn;
}
