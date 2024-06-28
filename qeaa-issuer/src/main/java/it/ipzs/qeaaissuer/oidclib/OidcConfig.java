package it.ipzs.qeaaissuer.oidclib;

import it.ipzs.qeaaissuer.oidclib.schemas.OIDCProfile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "oidc")
public class OidcConfig extends BaseConfig {

	private String defaultTrustAnchor;
	private List<String> trustAnchors = new ArrayList<>();
	private RelyingParty relyingParty = new RelyingParty();
	private Hosts hosts = new Hosts();
	private List<ProviderInfo> spidProviders = new ArrayList<>();
	private List<ProviderInfo> cieProviders = new ArrayList<>();
	private OpenIdCredentialIssuer openidCredentialIssuer = new OpenIdCredentialIssuer();
	private FederationEntity federationEntity = new FederationEntity();
	private String federationTrustChainUrl;

	public String getDefaultTrustAnchor() {
		return defaultTrustAnchor;
	}

	public List<ProviderInfo> getCieProviders() {
		return cieProviders;
	}

	public List<ProviderInfo> getSpidProviders() {
		return spidProviders;
	}

	public Map<String, String> getIdentityProviders(OIDCProfile profile) {
		Map<String, String> result = new HashMap<>();

		if (OIDCProfile.CIE.equals(profile)) {
			for (ProviderInfo provider : cieProviders) {
				result.put(provider.getSubject(), provider.getTrustAnchor());
			}
		}
		else if (OIDCProfile.SPID.equals(profile)) {
			for (ProviderInfo provider : spidProviders) {
				result.put(provider.getSubject(), provider.getTrustAnchor());
			}
		}

		return result;
	}

	public List<String> getTrustAnchors() {
		return trustAnchors;
	}

	public RelyingParty getRelyingParty() {
		return relyingParty;
	}

	public Hosts getHosts() {
		return hosts;
	}

	public void setDefaultTrustAnchor(String defaultTrustAnchor) {
		this.defaultTrustAnchor = defaultTrustAnchor;
	}

	public void setTrustAnchors(List<String> trustAnchors) {
		this.trustAnchors = trustAnchors;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("defaultTrustAnchor", defaultTrustAnchor);
		json.put("trustAnchors", trustAnchors);
		json.put("relyingParty", relyingParty.toJSON());
		json.put("spidProviders", new JSONArray(spidProviders));
		json.put("cieProviders", new JSONArray(cieProviders));
		json.put("hosts", hosts.toJSON());

		return json;
	}

	public OpenIdCredentialIssuer getOpenidCredentialIssuer() {
		return openidCredentialIssuer;
	}

	public void setOpenidCredentialIssuer(OpenIdCredentialIssuer openidCredentialIssuer) {
		this.openidCredentialIssuer = openidCredentialIssuer;
	}

	public FederationEntity getFederationEntity() {
		return federationEntity;
	}

	public void setFederationEntity(FederationEntity federationEntity) {
		this.federationEntity = federationEntity;
	}


	public String getFederationTrustChainUrl() {
		return federationTrustChainUrl;
	}

	public void setFederationTrustChainUrl(String federationTrustChainUrl) {
		this.federationTrustChainUrl = federationTrustChainUrl;
	}


	public static class Hosts extends BaseConfig {

		public String getTrustAnchor() {
			return trustAnchor;
		}

		public String getCieProvider() {
			return cieProvider;
		}

		public String getRelyingParty() {
			return relyingParty;
		}

		public void setTrustAnchor(String trustAnchor) {
			this.trustAnchor = trustAnchor;
		}

		public void setCieProvider(String cieProvider) {
			this.cieProvider = cieProvider;
		}

		public void setRelyingParty(String relyingParty) {
			this.relyingParty = relyingParty;
		}

		public JSONObject toJSON() {
			return new JSONObject()
					.put("trust-anchor", trustAnchor)
					.put("cie-provider", cieProvider)
					.put("relying-party", relyingParty);
		}

		private String trustAnchor = "127.0.0.1";
		private String cieProvider = "127.0.0.1";
		private String relyingParty = "127.0.0.1";

	}

	public static class ProviderInfo extends BaseConfig {

		public String getSubject() {
			return subject;
		}

		public String getTrustAnchor() {
			return trustAnchor;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public void setTrustAnchor(String trustAnchor) {
			this.trustAnchor = trustAnchor;
		}

		public JSONObject toJSON() {
			return new JSONObject()
					.put("subject", subject)
					.put("trust-anchor", trustAnchor);
		}

		private String subject;
		private String trustAnchor;

	}

	public static class RelyingParty {

		public String getApplicationName() {
			return applicationName;
		}

		public String getApplicationType() {
			return applicationType;
		}

		public Set<String> getContacts() {
			return Collections.unmodifiableSet(contacts);
		}

		public Set<String> getScope() {
			return Collections.unmodifiableSet(scope);
		}

		public String getClientId() {
			return clientId;
		}

		public Set<String> getRedirectUris() {
			return Collections.unmodifiableSet(redirectUris);
		}

		public Set<String> getRequestUris() {
			return Collections.unmodifiableSet(requestUris);
		}


		public String getJwkFilePath() {
			return jwkFilePath;
		}

		public String getTrustMarksFilePath() {
			return trustMarksFilePath;
		}

		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}

		public void setApplicationType(String applicationType) {
			this.applicationType = applicationType;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public void setJwkFilePath(String jwkFilePath) {
			this.jwkFilePath = jwkFilePath;
		}

		public void setTrustMarksFilePath(String trustMarksFilePath) {
			this.trustMarksFilePath = trustMarksFilePath;
		}

		public void setContacts(Set<String> contacts) {
			this.contacts = contacts;
		}

		public void setScope(Set<String> scope) {
			this.scope = scope;
		}

		public void setRedirectUris(Set<String> redirectUris) {
			this.redirectUris = redirectUris;
		}

		public void setRequestUris(Set<String> requestUris) {
			this.requestUris = requestUris;
		}

		public JSONObject toJSON() {
			JSONObject json = new JSONObject();

			json.put("applicationName", applicationName);
			json.put("applicationType", applicationType);
			json.put("contacts", contacts);
			json.put("scope", scope);
			json.put("clientId", clientId);
			json.put("redirectUris", redirectUris);
			json.put("jwkFilePath", jwkFilePath);
			json.put("trustMarksFilePath", trustMarksFilePath);

			return json;
		}

		public String getEncrJwkFilePath() {
			return encrJwkFilePath;
		}

		public void setEncrJwkFilePath(String encrJwkFilePath) {
			this.encrJwkFilePath = encrJwkFilePath;
		}

		private String applicationName;
		private String applicationType;
		private Set<String> contacts = new HashSet<>();
		private Set<String> scope = new HashSet<>();
		private String clientId;
		private Set<String> redirectUris = new HashSet<>();
		private Set<String> requestUris = new HashSet<>();
		private String jwkFilePath;
		private String trustMarksFilePath;
		private String encrJwkFilePath;

	}

	public static class OpenIdCredentialIssuer {
		private String credentialIssuer;
		private String id;
		private String authorizationEndpoint;
		private String tokenEndpoint;
		private String pushedAuthorizationRequestEndpoint;
		private String credentialEndpoint;
		private Set<String> dpopSigningAlgValuesSupported = new HashSet<>();
		private String jwkFilePath;
		private String mdocJwkFilePath;
		private String encrJwkFilePath;
		private String mdocX5CFilePath;
		private String sub;
		private List<String> trustChain = new ArrayList<>();

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCredentialIssuer() {
			return credentialIssuer;
		}

		public void setCredentialIssuer(String credentialIssuer) {
			this.credentialIssuer = credentialIssuer;
		}

		public String getAuthorizationEndpoint() {
			return authorizationEndpoint;
		}

		public void setAuthorizationEndpoint(String authorizationEndpoint) {
			this.authorizationEndpoint = authorizationEndpoint;
		}

		public String getTokenEndpoint() {
			return tokenEndpoint;
		}

		public void setTokenEndpoint(String tokenEndpoint) {
			this.tokenEndpoint = tokenEndpoint;
		}

		public String getPushedAuthorizationRequestEndpoint() {
			return pushedAuthorizationRequestEndpoint;
		}

		public void setPushedAuthorizationRequestEndpoint(String pushedAuthorizationRequestEndpoint) {
			this.pushedAuthorizationRequestEndpoint = pushedAuthorizationRequestEndpoint;
		}

		public String getCredentialEndpoint() {
			return credentialEndpoint;
		}

		public void setCredentialEndpoint(String credentialEndpoint) {
			this.credentialEndpoint = credentialEndpoint;
		}

		public Set<String> getDpopSigningAlgValuesSupported() {
			return Collections.unmodifiableSet(dpopSigningAlgValuesSupported);
		}

		public void setDpopSigningAlgValuesSupported(Set<String> dpopSigningAlgValuesSupported) {
			this.dpopSigningAlgValuesSupported = dpopSigningAlgValuesSupported;
		}

		public String getJwkFilePath() {
			return jwkFilePath;
		}

		public void setJwkFilePath(String jwkFilePath) {
			this.jwkFilePath = jwkFilePath;
		}

		public String getMdocX5CFilePath() {
			return mdocX5CFilePath;
		}

		public void setMdocX5CFilePath(String mdocX5CFilePath) {
			this.mdocX5CFilePath = mdocX5CFilePath;
		}

		public String getMdocJwkFilePath() {
			return mdocJwkFilePath;
		}

		public void setMdocJwkFilePath(String mdocJwkFilePath) {
			this.mdocJwkFilePath = mdocJwkFilePath;
		}

		public String getEncrJwkFilePath() {
			return encrJwkFilePath;
		}

		public void setEncrJwkFilePath(String encrJwkFilePath) {
			this.encrJwkFilePath = encrJwkFilePath;
		}

		public String getSub() {
			return sub;
		}

		public void setSub(String sub) {
			this.sub = sub;
		}

		public List<String> getTrustChain() {
			return trustChain;
		}

		public void setTrustChain(List<String> trustChain) {
			this.trustChain = trustChain;
		}
	}

	public static class FederationEntity {
		private String homepageUri;
		private String organizationName;
		private String policyUri;
		private String tosUri;
		private String logoUri;
		public String getHomepageUri() {
			return homepageUri;
		}
		public void setHomepageUri(String homepageUri) {
			this.homepageUri = homepageUri;
		}
		public String getOrganizationName() {
			return organizationName;
		}
		public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
		}
		public String getPolicyUri() {
			return policyUri;
		}
		public void setPolicyUri(String policyUri) {
			this.policyUri = policyUri;
		}
		public String getTosUri() {
			return tosUri;
		}
		public void setTosUri(String tosUri) {
			this.tosUri = tosUri;
		}
		public String getLogoUri() {
			return logoUri;
		}
		public void setLogoUri(String logoUri) {
			this.logoUri = logoUri;
		}
	}

}