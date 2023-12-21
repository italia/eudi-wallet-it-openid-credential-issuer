package it.ipzs.qeaaissuer.exception;

public class HashedWalletInstanceAttestationGenerationException extends RuntimeException {

	private static final long serialVersionUID = -5273054693200079174L;

	public HashedWalletInstanceAttestationGenerationException(Exception e) {
		super(e);
	}

}
