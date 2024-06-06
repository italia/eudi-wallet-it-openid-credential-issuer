package it.ipzs.qeaaissuer.exception;

public class HashedWiaNotMatchException extends RuntimeException {

	private static final long serialVersionUID = 2569451704449561314L;
	
	public HashedWiaNotMatchException(Exception e) {
		super(e);
	}

	public HashedWiaNotMatchException(String message) {
		super(message);
	}

}
