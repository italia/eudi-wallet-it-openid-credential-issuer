package it.ipzs.pidprovider.exception;

public class InvalidHtmAndHtuClaimsException extends RuntimeException {

	private static final long serialVersionUID = 7588557756273953634L;
	
	public InvalidHtmAndHtuClaimsException(Exception e) {
		super(e);
	}

	public InvalidHtmAndHtuClaimsException(String message) {
		super(message);
	}

}
