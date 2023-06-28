package it.ipzs.pidprovider.model;

import lombok.Data;

@Data
public class SessionInfo {

	private String requestUri;

	private String state;

	private String codeChallenge;

	private String clientId;

	private String redirectUri;

	private String code;

	private boolean verified;

	private String nonce;
}
