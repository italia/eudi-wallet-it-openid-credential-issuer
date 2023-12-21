package it.ipzs.qeaaissuer.exception;

public class InvalidHtmAndHtuClaimsException extends RuntimeException {

	private static final long serialVersionUID = 3635590034852030287L;

	public InvalidHtmAndHtuClaimsException(Exception e) {
		super(e);
	}

	public InvalidHtmAndHtuClaimsException(String message) {
		super(message);
	}

	
	

}
