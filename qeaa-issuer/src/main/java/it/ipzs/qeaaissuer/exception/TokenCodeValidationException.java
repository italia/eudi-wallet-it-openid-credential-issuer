package it.ipzs.qeaaissuer.exception;

public class TokenCodeValidationException extends RuntimeException {

	private static final long serialVersionUID = -5581291114134915699L;

	public TokenCodeValidationException(String message) {
		super(message);
	}

}
