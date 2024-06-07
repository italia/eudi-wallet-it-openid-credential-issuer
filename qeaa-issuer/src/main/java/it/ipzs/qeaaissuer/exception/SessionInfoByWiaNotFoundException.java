package it.ipzs.qeaaissuer.exception;

public class SessionInfoByWiaNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2761091262009185390L;

	public SessionInfoByWiaNotFoundException(Exception e) {
		super(e);
	}

	public SessionInfoByWiaNotFoundException(String message) {
		super(message);
	}
	

}
