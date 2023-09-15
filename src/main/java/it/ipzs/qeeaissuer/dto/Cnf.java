package it.ipzs.qeeaissuer.dto;

import java.io.Serializable;

public class Cnf implements Serializable {
	private static final long serialVersionUID = 4211723201991928614L;

	private JWKDto jwk;

	public JWKDto getJwk() {
		return jwk;
	}

	public void setJwk(JWKDto jwk) {
		this.jwk = jwk;
	}
}

