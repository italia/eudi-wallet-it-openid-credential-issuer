package it.ipzs.pidprovider.exception;

public class DpopJwtMissingClaimException extends RuntimeException {

	private static final long serialVersionUID = -3682750668853124778L;

	public DpopJwtMissingClaimException(String message) {
		super(message);
	}
	

}
