package it.ipzs.pidprovider.exception;

public class JwsHeaderMissingFieldException extends RuntimeException {

	private static final long serialVersionUID = 4060072973336431219L;

	public JwsHeaderMissingFieldException(String message) {
		super(message);
	}
	

}
