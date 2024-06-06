package it.ipzs.pidprovider.exception;

public class SessionInfoByClientIdNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5828286914384310499L;
	
	public SessionInfoByClientIdNotFoundException(Exception e) {
		super(e);
	}

	public SessionInfoByClientIdNotFoundException(String message) {
		super(message);
	}

}
