package it.ipzs.qeeaissuer.model;

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
}
