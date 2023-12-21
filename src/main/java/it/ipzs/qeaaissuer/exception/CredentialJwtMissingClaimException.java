package it.ipzs.qeaaissuer.exception;

public class CredentialJwtMissingClaimException extends RuntimeException {

	private static final long serialVersionUID = -6200238940346866823L;

	public CredentialJwtMissingClaimException(String message) {
		super(message);
	}
	
	
}
