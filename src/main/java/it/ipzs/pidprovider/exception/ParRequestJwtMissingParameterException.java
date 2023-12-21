package it.ipzs.pidprovider.exception;

public class ParRequestJwtMissingParameterException extends RuntimeException {

	private static final long serialVersionUID = 5241618678246564637L;
	
	public ParRequestJwtMissingParameterException(Exception e) {
		super(e);
	}

	public ParRequestJwtMissingParameterException(String message) {
		super(message);
	}

}
