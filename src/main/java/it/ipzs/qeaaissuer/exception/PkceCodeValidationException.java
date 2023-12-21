package it.ipzs.qeaaissuer.exception;

public class PkceCodeValidationException extends RuntimeException {

	private static final long serialVersionUID = 8661827774555517334L;

	public PkceCodeValidationException(String message) {
		super(message);
	}
	

}
