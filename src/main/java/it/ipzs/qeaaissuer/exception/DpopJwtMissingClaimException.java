package it.ipzs.qeaaissuer.exception;

public class DpopJwtMissingClaimException extends RuntimeException {

	private static final long serialVersionUID = -3725953753396456079L;

	public DpopJwtMissingClaimException(String message) {
		super(message);
	}
	

}
