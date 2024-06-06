package it.ipzs.pidprovider.exception;

public class PkceCodeValidationException extends RuntimeException {

	private static final long serialVersionUID = -5468706845665116096L;

	public PkceCodeValidationException(String message) {
		super(message);
	}
	

}
