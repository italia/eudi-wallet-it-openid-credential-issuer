package it.ipzs.qeaaissuer.exception;

public class InvalidAthException extends RuntimeException {

	private static final long serialVersionUID = -9198159789557649074L;

	public InvalidAthException(Exception e) {
		super(e);
	}

	public InvalidAthException(String message) {
		super(message);
	}
	

}
