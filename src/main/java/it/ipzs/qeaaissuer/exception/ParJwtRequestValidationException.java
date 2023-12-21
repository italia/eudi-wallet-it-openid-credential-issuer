package it.ipzs.qeaaissuer.exception;

public class ParJwtRequestValidationException extends RuntimeException {

	private static final long serialVersionUID = 7850538829117898656L;

	public ParJwtRequestValidationException(Exception e) {
		super(e);
	}

}
