package it.ipzs.pidprovider.exception;

public class SessionInfoByStateNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5828286914384310499L;
	
	public SessionInfoByStateNotFoundException(Exception e) {
		super(e);
	}

	public SessionInfoByStateNotFoundException(String message) {
		super(message);
	}

}
