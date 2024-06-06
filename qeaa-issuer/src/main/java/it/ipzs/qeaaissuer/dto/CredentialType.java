package it.ipzs.qeaaissuer.dto;

public enum CredentialType {

	EDC("EuropeanDisabilityCard"), MDL("mDL"), EHIC("EuropeanHealthInsuranceCard");

	private final String value;

	private CredentialType(String value) {
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
