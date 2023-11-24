package it.ipzs.qeaaissuer.dto;

public enum CredentialFormat {

	MDOC_CBOR("vc+mdoc-cbor"), SD_JWT("vc+sd-jwt");

	private final String value;

	private CredentialFormat(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value();
	}

}
