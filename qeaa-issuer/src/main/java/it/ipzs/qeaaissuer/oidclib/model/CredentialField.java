package it.ipzs.qeaaissuer.oidclib.model;

import java.util.List;

import lombok.Builder;

@Builder
public class CredentialField {
	private boolean mandatory;
	private List<DisplayConf> display;

	public List<DisplayConf> getDisplay() {
		return display;
	}

	public void setDisplay(List<DisplayConf> display) {
		this.display = display;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

}