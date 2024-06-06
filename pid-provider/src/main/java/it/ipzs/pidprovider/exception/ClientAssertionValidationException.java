package it.ipzs.pidprovider.exception;

public class ClientAssertionValidationException extends RuntimeException {

	private static final long serialVersionUID = 4708458094539802001L;
	
	public ClientAssertionValidationException(Exception e) {
		super(e);
	}

}
