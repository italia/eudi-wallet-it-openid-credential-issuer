package it.ipzs.qeaaissuer.exception;

public class AlreadyGeneratedCredentialException extends RuntimeException {

	private static final long serialVersionUID = 1289269335463078160L;

	public AlreadyGeneratedCredentialException(String message) {
		super(message);
	}

}
