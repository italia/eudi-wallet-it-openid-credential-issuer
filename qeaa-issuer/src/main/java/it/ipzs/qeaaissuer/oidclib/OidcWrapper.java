package it.ipzs.qeaaissuer.oidclib;

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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.jwk.JWK;

import it.ipzs.qeaaissuer.oidclib.callback.RelyingPartyLogoutCallback;
import it.ipzs.qeaaissuer.oidclib.exception.OIDCException;
import it.ipzs.qeaaissuer.oidclib.handler.OidcHandler;
import it.ipzs.qeaaissuer.oidclib.model.CredentialDefinition;
import it.ipzs.qeaaissuer.oidclib.model.CredentialEHICSubject;
import it.ipzs.qeaaissuer.oidclib.model.CredentialField;
import it.ipzs.qeaaissuer.oidclib.model.CredentialMDLSubject;
import it.ipzs.qeaaissuer.oidclib.model.CredentialSubject;
import it.ipzs.qeaaissuer.oidclib.model.CredentialType;
import it.ipzs.qeaaissuer.oidclib.model.DisplayConf;
import it.ipzs.qeaaissuer.oidclib.model.LogoConf;
import it.ipzs.qeaaissuer.oidclib.persistence.H2PersistenceImpl;
import it.ipzs.qeaaissuer.oidclib.schemas.OIDCProfile;
import it.ipzs.qeaaissuer.oidclib.schemas.ProviderButtonInfo;
import it.ipzs.qeaaissuer.oidclib.schemas.WellKnownData;
import it.ipzs.qeaaissuer.oidclib.util.Validator;
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

	public JWK getRelyingPartyJWK() throws ParseException {
		String jwk = oidcHandler.retrieveRelyingPartyJWK();
		JWK parsedJWK = null;
		try {
			parsedJWK = JWK.parse(jwk);
		} catch (ParseException e) {
			logger.error("", e);
			throw e;
		}

		return parsedJWK;
	}

	public JWK getRelyingPartyEncryptionJWK() throws ParseException {
		String jwk = oidcHandler.retrieveRelyingPartyEncryptionJWK();
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

	public JWK getMdocCredentialIssuerJWK() throws ParseException {
		String credJwk = oidcHandler.getCredentialOptions().getMdocJwk();
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
		return generateCredentialIssuerTrustChain();
	}

	private List<String> generateCredentialIssuerTrustChain() {
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> entity = restTemplate.getForEntity(new URI(oidcConfig.getFederationTrustChainUrl()),
					String.class);
			String fedTc = entity.getBody();

			WellKnownData wellKnown = getWellKnownData(false);

			return List.of(wellKnown.getValue(), fedTc);

		} catch (RestClientException | URISyntaxException | OIDCException e) {
			logger.error("Error in trust chain retrieval", e);

			throw new RuntimeException(e);
		}

	}

	public List<String> getRelyingPartyTrustChain() {
		return generateRelyingPartyTrustChain();
	}

	private List<String> generateRelyingPartyTrustChain() {
		logger.info("Trust chain retrieval");
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> entity = restTemplate.getForEntity(new URI(
							oidcConfig.getFederationTrustChainUrl()),
					String.class);
			String fedTc = entity.getBody();
			logger.info("> HTTP status code {}", entity.getStatusCode());

			WellKnownData wellKnown = getWellKnownData(false);

			return List.of(wellKnown.getValue(), fedTc);

		} catch (Exception e) {
			if (e instanceof HttpStatusCodeException rce) {
				logger.error("-> HTTP status code {}", rce.getStatusCode());
			} else {
				logger.error("", e);
			}

			throw new RuntimeException(e);
		}

	}

	@PostConstruct
	private void postConstruct() throws OIDCException {
		String jwk = readFile(oidcConfig.getRelyingParty().getJwkFilePath());
		String encrJwk = readFile(oidcConfig.getRelyingParty().getEncrJwkFilePath());
		String trustMarks = readFile(
				oidcConfig.getRelyingParty().getTrustMarksFilePath());

		logger.debug("final jwk: {}", jwk);
		logger.debug("final trust_marks: {}", trustMarks);

		RelyingPartyOptions options = new RelyingPartyOptions()
				.setDefaultTrustAnchor(oidcConfig.getDefaultTrustAnchor())
				.setCIEProviders(oidcConfig.getIdentityProviders(OIDCProfile.CIE))
				.setSPIDProviders(oidcConfig.getIdentityProviders(OIDCProfile.SPID))
				.setTrustAnchors(oidcConfig.getTrustAnchors())
				.setApplicationName(oidcConfig.getRelyingParty().getApplicationName())
				.setClientId(oidcConfig.getRelyingParty().getClientId())
				.setRedirectUris(oidcConfig.getRelyingParty().getRedirectUris())
				.setRequestUris(oidcConfig.getRelyingParty().getRequestUris())
				.setContacts(oidcConfig.getRelyingParty().getContacts())
				.setJWK(jwk)
				.setEncrJWK(encrJwk)
				.setTrustMarks(trustMarks);

		String credJwk = readFile(oidcConfig.getOpenidCredentialIssuer().getJwkFilePath());
		String mdocCredJwk = readFile(oidcConfig.getOpenidCredentialIssuer().getMdocJwkFilePath());

		OIDCCredentialIssuerOptions credentialOptions = OIDCCredentialIssuerOptions.builder()
				.pushedAuthorizationRequestEndpoint(
						oidcConfig.getOpenidCredentialIssuer().getPushedAuthorizationRequestEndpoint())
				.credentialEndpoint(oidcConfig.getOpenidCredentialIssuer().getCredentialEndpoint())
				.credentialIssueUrl(oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer())
				.tokenEndpoint(oidcConfig.getOpenidCredentialIssuer().getTokenEndpoint())
				.authorizationEndpoint(oidcConfig.getOpenidCredentialIssuer().getAuthorizationEndpoint())
				.credentialsSupported(generateCredentialSupportedList())
				.jwk(credJwk)
				.mdocJwk(mdocCredJwk)
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
		try {
			generateRelyingPartyTrustChain();
		} catch (Exception e) {
			logger.error("error in trust chain retrieval");
		}
	}

	private List<CredentialType> generateCredentialSupportedList() {
		List<CredentialType> credentialSupported = new ArrayList<>();

		CredentialType cedSdJwt = generateSdJwtCEDCredType();
		credentialSupported.add(cedSdJwt);

		CredentialType ehicSdJwt = generateSdJwtEHICCredType();
		credentialSupported.add(ehicSdJwt);

		CredentialType mDLSdJwt = generateMDLCredType("vc+sd-jwt");
		credentialSupported.add(mDLSdJwt);

		CredentialType mDLCbor = generateMDLCredType("vc+mdoc-cbor");
		credentialSupported.add(mDLCbor);

		return credentialSupported;
	}

	private CredentialType generateSdJwtEHICCredType() {

		CredentialType cred = new CredentialType();

		cred.setId(it.ipzs.qeaaissuer.dto.CredentialType.EHIC.value().toLowerCase() + "."
				+ oidcConfig.getOpenidCredentialIssuer().getId());

		cred.setFormat("vc+sd-jwt");
		DisplayConf d1 = DisplayConf.builder().name("QEAA Issuer").locale("it-IT").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
						"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();

		DisplayConf d2 = DisplayConf.builder().name("QEAA Issuer").locale("en-US").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
						"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();
		cred.setDisplay(List.of(d1, d2));

		CredentialDefinition credDef = new CredentialDefinition();
		credDef.getType().add(it.ipzs.qeaaissuer.dto.CredentialType.EHIC.value());
		CredentialEHICSubject credSubj = new CredentialEHICSubject();
		credSubj.setGiven_name(CredentialField.builder().mandatory(true)
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

		credSubj.setFiscal_code(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Codice Fiscale").locale("it-IT").build(),
						DisplayConf.builder().name("Fiscal Code").locale("en-US").build()))
				.build());

		credSubj.setProvince(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Provincia").locale("it-IT").build(),
						DisplayConf.builder().name("Province").locale("en-US").build()))
				.build());

		credSubj.setSex(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Sesso").locale("it-IT").build(),
						DisplayConf.builder().name("Sex").locale("en-US").build()))
				.build());

		credSubj.setExpiry_date(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di Scadenza").locale("it-IT").build(),
						DisplayConf.builder().name("Expiry Date").locale("en-US").build()))
				.build());

		credSubj.setNation(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Nazione").locale("it-IT").build(),
						DisplayConf.builder().name("Nation").locale("en-US").build()))
				.build());

		credSubj.setDocument_number_team(
				CredentialField.builder().mandatory(true)
						.display(List.of(
								DisplayConf.builder().name("Numero identificativo della tessera (TEAM)").locale("it-IT")
										.build(),
								DisplayConf.builder().name("Document Number (TEAM)").locale("en-US").build()))
						.build());

		credSubj.setInstitution_number_team(CredentialField.builder().mandatory(true)
				.display(List.of(
						DisplayConf.builder().name("Numero identificativo dell'istituzione (TEAM)").locale("it-IT")
								.build(),
						DisplayConf.builder().name("Institution Number (TEAM)").locale("en-US").build()))
				.build());

		credDef.setCredentialSubject(credSubj);

		cred.setCredential_definition(credDef);
		return cred;

	}

	private CredentialType generateSdJwtCEDCredType() {
		CredentialType cred = new CredentialType();

		cred.setId(it.ipzs.qeaaissuer.dto.CredentialType.EDC.value().toLowerCase() + "."
				+ oidcConfig.getOpenidCredentialIssuer().getId());

		cred.setFormat("vc+sd-jwt");
		DisplayConf d1 = DisplayConf.builder().name("QEAA Issuer").locale("it-IT").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder()
						.url("https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer()
								+ "/public/logo.svg")
						.alt_text("logo").build())
				.build();

		DisplayConf d2 = DisplayConf.builder().name("QEAA Issuer").locale("en-US").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
								"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();
		cred.setDisplay(List.of(d1,d2));

		CredentialDefinition credDef = new CredentialDefinition();
		credDef.getType().add(it.ipzs.qeaaissuer.dto.CredentialType.EDC.value());
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

		credSubj.setFiscal_code(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Codice Fiscale").locale("it-IT").build(),
						DisplayConf.builder().name("Fiscal Code").locale("en-US").build()))
				.build());

		credSubj.setExpiration_date(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di Scadenza").locale("it-IT").build(),
						DisplayConf.builder().name("Expiration Date").locale("en-US").build()))
				.build());

		credSubj.setSerial_number(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Numero Seriale").locale("it-IT").build(),
						DisplayConf.builder().name("Serial Number").locale("en-US").build()))
				.build());

		credSubj.setAccompanying_person_right(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Diritto all'accompagnatore").locale("it-IT").build(),
						DisplayConf.builder().name("Accompanying Person Right").locale("en-US").build()))
				.build());

		credDef.setCredentialSubject(credSubj);

		cred.setCredential_definition(credDef);
		return cred;
	}

	private CredentialType generateMDLCredType(String format) {
		CredentialType cred = new CredentialType();

		cred.setId(it.ipzs.qeaaissuer.dto.CredentialType.MDL.value().toLowerCase() + "."
				+ oidcConfig.getOpenidCredentialIssuer().getId());

		cred.setFormat(format);
		DisplayConf d1 = DisplayConf.builder().name("QEAA Issuer").locale("it-IT").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
						"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();

		DisplayConf d2 = DisplayConf.builder().name("QEAA Issuer").locale("en-US").background_color("#12107c")
				.text_color("#FFFFFF")
				.logo(LogoConf.builder().url(
						"https://" + oidcConfig.getOpenidCredentialIssuer().getCredentialIssuer() + "/public/logo.svg")
						.alt_text("logo").build())
				.build();
		cred.setDisplay(List.of(d1, d2));

		CredentialDefinition credDef = new CredentialDefinition();
		credDef.getType().add(it.ipzs.qeaaissuer.dto.CredentialType.MDL.value());
		CredentialMDLSubject credSubj = new CredentialMDLSubject();
		credSubj.setGiven_name(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Nome").locale("it-IT").build(),
						DisplayConf.builder().name("First Name").locale("en-US").build()))
				.build());

		credSubj.setFamily_name(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Cognome").locale("it-IT").build(),
						DisplayConf.builder().name("Family Name").locale("en-US").build()))
				.build());

		credSubj.setBirthdate(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di nascita").locale("it-IT").build(),
						DisplayConf.builder().name("Date of Birth").locale("en-US").build()))
				.build());

		credSubj.setIssuing_authority(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Autorità di rilascio").locale("it-IT").build(),
						DisplayConf.builder().name("Issuing Authority").locale("en-US").build()))
				.build());

		credSubj.setDriving_privileges(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Categorie di veicoli").locale("it-IT").build(),
						DisplayConf.builder().name("Driving Privileges").locale("en-US").build()))
				.build());

		credSubj.setIssuing_country(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Paese di rilascio").locale("it-IT").build(),
						DisplayConf.builder().name("Issuing Country").locale("en-US").build()))
				.build());

		credSubj.setIssue_date(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di rilascio").locale("it-IT").build(),
						DisplayConf.builder().name("Issue Date").locale("en-US").build()))
				.build());

		credSubj.setExpiry_date(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Data di scadenza").locale("it-IT").build(),
						DisplayConf.builder().name("Expiry Date").locale("en-US").build()))
				.build());

		credSubj.setUn_distinguishing_sign(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Segno distintivo UN").locale("it-IT").build(),
						DisplayConf.builder().name("UN Distinguishing Sign").locale("en-US").build()))
				.build());

		credSubj.setDocument_number(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Numero di documento").locale("it-IT").build(),
						DisplayConf.builder().name("Document Number").locale("en-US").build()))
				.build());

		credSubj.setPortrait(CredentialField.builder().mandatory(true)
				.display(List.of(DisplayConf.builder().name("Foto").locale("it-IT").build(),
						DisplayConf.builder().name("Portrait").locale("en-US").build()))
				.build());

		credDef.setCredentialSubject(credSubj);

		cred.setCredential_definition(credDef);
		return cred;
	}

	private String readFile(String filePath) {
		if (filePath != null) {
			try {
				File file = new File(filePath);

				if (file.isFile() && file.canRead()) {
					return Files.readString(file.toPath());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return "";
	}

	public void reloadKeys() {
		String jwk = readFile(oidcConfig.getRelyingParty().getJwkFilePath());
		String encrJwk = readFile(oidcConfig.getRelyingParty().getEncrJwkFilePath());
		String credJwk = readFile(oidcConfig.getOpenidCredentialIssuer().getJwkFilePath());
		String mdocCredJwk = readFile(oidcConfig.getOpenidCredentialIssuer().getMdocJwkFilePath());
		this.oidcHandler.getCredentialOptions().setJwk(credJwk);
		this.oidcHandler.getRelyingPartyOptions().setJWK(jwk);
		this.oidcHandler.getRelyingPartyOptions().setEncrJWK(encrJwk);
		this.oidcHandler.getCredentialOptions().setMdocJwk(mdocCredJwk);
		logger.debug("key reloaded!");
	}

}