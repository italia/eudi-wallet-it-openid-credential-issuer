package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse implements Serializable {

	private static final long serialVersionUID = -7979455405188597114L;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("expires_in")
	private int expiresIn;

	@JsonProperty("c_nonce")
	private String nonce;

	@JsonProperty("c_nonce_expires_in")
	private int nonceExpiresIn;

}
