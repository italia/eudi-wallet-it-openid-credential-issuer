package it.ipzs.qeaaissuer.exception;

public class RequestUriDpopValidationException extends RuntimeException {

	private static final long serialVersionUID = -4027451618198836933L;
	
	public RequestUriDpopValidationException(Exception e) {
		super(e);
	}

}
