package it.ipzs.qeaaissuer.exception;

public class CredentialNonceNotMatchException extends RuntimeException {

	private static final long serialVersionUID = 2174953011664227840L;

	public CredentialNonceNotMatchException(String message) {
		super(message);
	}
	

}
