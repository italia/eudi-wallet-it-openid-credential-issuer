package it.ipzs.qeaaissuer.exception;

public class SessionRequestUriNotMatchedException extends RuntimeException {

	private static final long serialVersionUID = -5633009425809236385L;

	public SessionRequestUriNotMatchedException(Exception e) {
		super(e);
	}

	public SessionRequestUriNotMatchedException(String message) {
		super(message);
	}

}
