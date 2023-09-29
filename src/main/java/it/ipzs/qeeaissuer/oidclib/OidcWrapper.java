package it.ipzs.qeeaissuer.oidclib;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.jwk.JWK;

import it.ipzs.qeeaissuer.oidclib.callback.RelyingPartyLogoutCallback;
import it.ipzs.qeeaissuer.oidclib.exception.OIDCException;
import it.ipzs.qeeaissuer.oidclib.handler.OidcHandler;
import it.ipzs.qeeaissuer.oidclib.model.CredentialDefinition;
import it.ipzs.qeeaissuer.oidclib.model.CredentialField;
import it.ipzs.qeeaissuer.oidclib.model.CredentialSubject;
import it.ipzs.qeeaissuer.oidclib.model.CredentialType;
import it.ipzs.qeeaissuer.oidclib.model.DisplayConf;
import it.ipzs.qeeaissuer.oidclib.model.LogoConf;
import it.ipzs.qeeaissuer.oidclib.persistence.H2PersistenceImpl;
import it.ipzs.qeeaissuer.oidclib.schemas.OIDCProfile;
import it.ipzs.qeeaissuer.oidclib.schemas.ProviderButtonInfo;
import it.ipzs.qeeaissuer.oidclib.schemas.WellKnownData;
import it.ipzs.qeeaissuer.oidclib.util.Validator;
import jakarta.annotation.PostConstruct;

@Component
public class OidcWrapper {

	private static Logger logger = LoggerFactory.getLogger(OidcWrapper.class);

	@Autowired
	private OidcConfig oidcConfig;

	@Autowired
	private H2PersistenceImpl persistenceImpl;

	private OidcHandler oidcHandler;

	public String getAuthorizeURL(
			String spidProvider, String trustAnchor, String redirectUri, String scope,
			String profile, String prompt)
			throws OIDCException {

		return oidcHandler.getAuthorizeURL(
				spidProvider, trustAnchor, redirectUri, scope, profile, prompt);
	}

	public List<ProviderButtonInfo> getProviderButtonInfos(OIDCProfile profile)
			throws OIDCException {

		return oidcHandler.getProviderButtonInfos(profile);
	}

	public JSONObject getUserInfo(String state, String code)
			throws OIDCException {

		return oidcHandler.getUserInfo(state, code);
	}

	public String getUserKey(JSONObject userInfo) {
		String userKey = userInfo.optString("https://attributes.spid.gov.it/email");

		if (Validator.isNullOrEmpty(userKey)) {
			userKey = userInfo.optString("email", "");
		}

		return userKey;
	}

	public WellKnownData getWellKnownData(String requestURL, boolean jsonMode)
			throws OIDCException {

		return oidcHandler.getWellKnownData(requestURL, jsonMode);
	}

	public WellKnownData getWellKnownData(boolean jsonMode) throws OIDCException {

		return oidcHandler.getWellKnownData(jsonMode);
	}

	public WellKnownData getFederationEntityData()
			throws OIDCException {

		return oidcHandler.getWellKnownData(true);
	}

	public String performLogout(String userKey, RelyingPartyLogoutCallback callback)
			throws OIDCException {

		return oidcHandler.performLogout(userKey, callback);
	}

	public void reloadHandler() throws OIDCException {
		logger.info("reload handler");

		postConstruct();
	}

	public JWK getJWK() throws ParseException {
		String jwk = oidcHandler.retrieveJWK();
		JWK parsedJWK = null;
		try {
			parsedJWK = JWK.parse(jwk);
		} catch (ParseException e) {
			logger.error("", e);
			throw e;
		}

		return parsedJWK;
	}

	public JWK getCredentialIssuerJWK() throws ParseException {
		String credJwk = oidcHandler.getCredentialOptions().getJwk();
		JWK parsedJWK = null;
		try {
			parsedJWK = JWK.parse(credJwk);
		} catch (ParseException e) {
			logger.error("", e);
			throw e;
		}

		return parsedJWK;
	}

	public String getCredentialIssuerTrustMarks() {
		return oidcHandler.getCredentialOptions().getTrustMarks();
	}

	public List<String> getCredentialIssuerTrustChain() {
		return generateTrustChain();
	}

	private List<String> generateTrustChain() {
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> entity = restTemplate.getForEntity(new URI(
							oidcConfig.getFederationTrustChainUrl()),
					String.class);
			String fedTc = entity.getBody();

			WellKnownData wellKnown = getWellKnownData(false);

			return List.of(wellKnown.getValue(), fedTc);

		} catch (RestClientException | URISyntaxException | OIDCException e) {
			logger.error("Error in trust chain retrieval", e);

			throw new RuntimeException(e);
		}

	}

	@PostConstruct
	private void postConstruct() throws OIDCException {
		String jwk = readFile(oidcConfig.getRelyingParty().getJwkFilePath());
		String encrJwk = readFile(oidcConfig.getRelyingParty().getEncrJwkFilePath());
		String trustMarks = readFile(
				oidcConfig.getRelyingParty().getTrustMarksFilePath());

		logger.info("final jwk: " + jwk);
		logger.info("final trust_marks: " + trustMarks);

		RelyingPartyOptions options = new RelyingPartyOptions()
				.setDefaultTrustAnchor(oidcConfig.getDefaultTrustAnchor())
				.setCIEProviders(oidcConfig.getIdentityProviders(OIDCProfile.CIE))
				.setSPIDProviders(oidcConfig.getIdentityProviders(OIDCProfile.SPID))
				.setTrustAnchors(oidcConfig.getTrustAnchors())
				.setApplicationName(oidcConfig.getRelyingParty().getApplicationName())
				.setClientId(oidcConfig.getRelyingParty().getClientId())
				.setRedirectUris(oidcConfig.getRelyingParty().getRedirectUris())
				.setContacts(oidcConfig.getRelyingParty().getContacts())
				.setJWK(jwk)
				.setEncrJWK(encrJwk)
				.setTrustMarks(trustMarks);

		String credJwk = readFile(oidcConfig.getOpenidCredentialIssuer().getJwkFilePath());

		OIDCCredentialIssuerOptions credentialOptions = OIDCCredentialIssuerOptions.builder()
				.pushedAuthorizationRequestEndpoint(
						oidcConfig.getOpenidCredentialIssuer().getPushedAuthorizationRequestEndpoint())
				.credentialEndpoint(oidcConfig.getOpenidCredentialIssuer().getCredentialEndpoint())
				.credentialIssueUrl(oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer())
				.tokenEndpoint(oidcConfig.getOpenidCredentialIssuer().getTokenEndpoint())
				.authorizationEndpoint(oidcConfig.getOpenidCredentialIssuer().getAuthorizationEndpoint())
				.credentialsSupported(generateCredentialSupportedList())
				.jwk(credJwk)
				.sub(oidcConfig.getOpenidCredentialIssuer().getSub())
				.trustChain(oidcConfig.getOpenidCredentialIssuer().getTrustChain())
				.build();

		FederationEntityOptions fedEntOptions = new FederationEntityOptions();
		fedEntOptions.setHomepage_uri(oidcConfig.getFederationEntity().getHomepageUri());
		fedEntOptions.setTos_uri(oidcConfig.getFederationEntity().getTosUri());
		fedEntOptions.setPolicy_uri(oidcConfig.getFederationEntity().getPolicyUri());
		fedEntOptions.setLogo_uri(oidcConfig.getFederationEntity().getLogoUri());
		fedEntOptions.setOrganization_name(oidcConfig.getFederationEntity().getOrganizationName());

		oidcHandler = new OidcHandler(options, persistenceImpl, credentialOptions, fedEntOptions);
	}

	private List<CredentialType> generateCredentialSupportedList() {
		List<CredentialType> credentialSupported = new ArrayList<>();

		CredentialType cred = new CredentialType();

		cred.setId(oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer());

		cred.setFormat("vc+sd-jwt");
		DisplayConf d1 = DisplayConf.builder().name("PID Provider italiano").locale("it-IT").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder()
						.url("https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer()
								+ "/public/logo.svg")
						.alt_text("logo").build())
				.build();

		DisplayConf d2 = DisplayConf.builder().name("Italian PID Provider").locale("en-US").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
								"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();
		cred.setDisplay(List.of(d1,d2));

		CredentialDefinition credDef = new CredentialDefinition();
		CredentialSubject credSubj = new CredentialSubject();
		credSubj.setGiven_name(CredentialField.builder()
				.mandatory(true)
				.display(List.of(DisplayConf.builder().name("Nome").locale("it-IT").build(),
						DisplayConf.builder().name("First Name").locale("en-US").build()))
				.build());

		credSubj.setFamily_name(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Cognome").locale("it-IT").build(),
						DisplayConf.builder().name("Family Name").locale("en-US").build()))
				.build());

		credSubj.setBirthdate(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di Nascita").locale("it-IT").build(),
						DisplayConf.builder().name("Date of Birth").locale("en-US").build()))
				.build());

		credSubj.setPlace_of_birth(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Luogo di Nascita").locale("it-IT").build(),
						DisplayConf.builder().name("Place of Birth").locale("en-US").build()))
				.build());

		credSubj.setTax_id_code(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Codice Fiscale").locale("it-IT").build(),
						DisplayConf.builder().name("Tax ID Number").locale("en-US").build()))
				.build());

		credSubj.setUnique_id(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Identificativo univoco").locale("it-IT").build(),
						DisplayConf.builder().name("Unique Identifier").locale("en-US").build()))
				.build());

		credDef.setCredentialSubject(credSubj);

		cred.setCredential_definition(List.of(credDef));

		credentialSupported.add(cred);

		return credentialSupported;
	}

	private String readFile(String filePath) {
		try {
			File file = new File(filePath);

			if (file.isFile() && file.canRead()) {
				return Files.readString(file.toPath());
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "";
	}

}