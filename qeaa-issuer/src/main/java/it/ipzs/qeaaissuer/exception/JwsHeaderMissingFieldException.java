package it.ipzs.qeaaissuer.exception;

public class JwsHeaderMissingFieldException extends RuntimeException {

	private static final long serialVersionUID = -1069428022562691427L;

	public JwsHeaderMissingFieldException(String message) {
		super(message);
	}
	

}
