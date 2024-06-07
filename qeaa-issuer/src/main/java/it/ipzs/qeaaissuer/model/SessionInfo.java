package it.ipzs.qeaaissuer.model;

import java.util.Map;

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

	private Object cnf;

	private String vpToken;

	private String transactionId;

	private String walletInstanceAttestation;

	private String hashedWia;

	private String requestUriNonce;

	private Map<String, Object> pidCredentialClaims;

	private String responseCode;

	private boolean credentialGenerated;
}
