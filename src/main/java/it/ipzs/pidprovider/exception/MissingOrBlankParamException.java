package it.ipzs.pidprovider.exception;

public class MissingOrBlankParamException extends RuntimeException {

	private static final long serialVersionUID = 6449087797051606105L;

	public MissingOrBlankParamException(String message) {
		super(message);
	}

}
