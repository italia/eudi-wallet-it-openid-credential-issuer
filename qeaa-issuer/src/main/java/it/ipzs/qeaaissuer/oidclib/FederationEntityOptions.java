package it.ipzs.qeaaissuer.oidclib;

import org.json.JSONObject;

public class FederationEntityOptions {

	private String organization_name;

	private String homepage_uri;

	private String policy_uri;

	private String tos_uri;

	private String logo_uri;

	public String getOrganization_name() {
		return organization_name;
	}

	public void setOrganization_name(String organization_name) {
		this.organization_name = organization_name;
	}

	public String getHomepage_uri() {
		return homepage_uri;
	}

	public void setHomepage_uri(String homepage_uri) {
		this.homepage_uri = homepage_uri;
	}

	public String getPolicy_uri() {
		return policy_uri;
	}

	public void setPolicy_uri(String policy_uri) {
		this.policy_uri = policy_uri;
	}

	public String getTos_uri() {
		return tos_uri;
	}

	public void setTos_uri(String tos_uri) {
		this.tos_uri = tos_uri;
	}

	public String getLogo_uri() {
		return logo_uri;
	}

	public void setLogo_uri(String logo_uri) {
		this.logo_uri = logo_uri;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("organization_name", organization_name);
		json.put("homepage_uri", homepage_uri);
		json.put("policy_uri", policy_uri);
		json.put("tos_uri", tos_uri);
		json.put("logo_uri", logo_uri);

		return json;
	}
}