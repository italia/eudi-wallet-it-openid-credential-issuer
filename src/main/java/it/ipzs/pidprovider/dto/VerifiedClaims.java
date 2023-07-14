package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VerifiedClaims implements Serializable {

	private static final long serialVersionUID = -8031068776114743283L;

	private Object claims;

	private VerificationDto verification;
}
