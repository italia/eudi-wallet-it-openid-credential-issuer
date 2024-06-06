package it.ipzs.qeaaissuer.exception;

public class SessionInfoByStateNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 3534053002396518272L;

	public SessionInfoByStateNotFoundException(Exception e) {
		super(e);
	}

	public SessionInfoByStateNotFoundException(String message) {
		super(message);
	}

}
