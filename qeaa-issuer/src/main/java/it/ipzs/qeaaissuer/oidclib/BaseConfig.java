package it.ipzs.qeaaissuer.oidclib;

import org.json.JSONObject;

public abstract class BaseConfig {

	public abstract JSONObject toJSON();

	public String toJSONString() {
		return toJSON().toString();
	}

	public String toJSONString(int indentFactor) {
		return toJSON().toString(indentFactor);
	}

}