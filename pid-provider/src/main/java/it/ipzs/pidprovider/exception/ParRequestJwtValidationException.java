package it.ipzs.pidprovider.exception;

public class ParRequestJwtValidationException extends RuntimeException {

	private static final long serialVersionUID = -2888873269529654426L;
	
	public ParRequestJwtValidationException(Exception e) {
		super(e);
	}

}
