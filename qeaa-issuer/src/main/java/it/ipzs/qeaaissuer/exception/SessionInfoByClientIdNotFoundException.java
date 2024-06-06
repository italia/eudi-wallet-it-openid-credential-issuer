package it.ipzs.qeaaissuer.exception;

public class SessionInfoByClientIdNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 3534053002396518272L;

	public SessionInfoByClientIdNotFoundException(Exception e) {
		super(e);
	}

	public SessionInfoByClientIdNotFoundException(String message) {
		super(message);
	}

}
