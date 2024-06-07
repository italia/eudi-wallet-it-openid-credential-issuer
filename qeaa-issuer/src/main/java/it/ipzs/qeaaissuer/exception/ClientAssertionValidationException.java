package it.ipzs.qeaaissuer.exception;

public class ClientAssertionValidationException extends RuntimeException {

	private static final long serialVersionUID = 5739121965664170901L;

	public ClientAssertionValidationException(Exception e) {
		super(e);
	}

}
