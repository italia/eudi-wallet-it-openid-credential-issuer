package it.ipzs.qeaaissuer.exception;

public class AuthResponseJwtGenerationException extends RuntimeException {

	private static final long serialVersionUID = 6020534915110197540L;

	public AuthResponseJwtGenerationException(Exception e) {
		super(e);
	}
}
