package it.ipzs.pidprovider.exception;

public class CredentialJwtMissingClaimException extends RuntimeException {

	private static final long serialVersionUID = 7644903445603564195L;

	public CredentialJwtMissingClaimException(String message) {
		super(message);
	}
	
	

}
