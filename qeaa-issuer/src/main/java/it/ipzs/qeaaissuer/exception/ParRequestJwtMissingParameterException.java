package it.ipzs.qeaaissuer.exception;

public class ParRequestJwtMissingParameterException extends RuntimeException {

	private static final long serialVersionUID = -2450055903483149660L;

	public ParRequestJwtMissingParameterException(Exception e) {
		super(e);
	}

	public ParRequestJwtMissingParameterException(String message) {
		super(message);
	}
	
	

}
