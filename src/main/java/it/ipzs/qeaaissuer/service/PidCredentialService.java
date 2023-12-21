package it.ipzs.qeaaissuer.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeaaissuer.exception.PidCredentialVerifyException;
import it.ipzs.qeaaissuer.exception.PidProviderConfigurationRetrievalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PidCredentialService {

	@Value("${pid-provider.url}")
	private String pidProviderConfig;

	private static final List<String> CLAIMS_REQUIRED = List.of("given_name", "family_name", "birthdate",
			"place_of_birth, fiscal_code");

	public void validatePidCredential(String pidCredential) throws ParseException, JOSEException {
		SDJWT sdJwt = SDJWT.parse(pidCredential);
		String credentialJwtString = sdJwt.getCredentialJwt();
		SignedJWT signedJwt = SignedJWT.parse(credentialJwtString);
		RSAKey pidJwk = retrievePidSigningKey();
		JWSVerifier verifier = new RSASSAVerifier(pidJwk);
		if (Boolean.FALSE.equals(signedJwt.verify(verifier))) {
			log.error("PID Credential JWT not verified");
			throw new PidCredentialVerifyException("PID Credential JWT not verified");
		}

	}

	public RSAKey retrievePidSigningKey() throws ParseException {
		RestTemplate restTemplate = new RestTemplate();
		RSAKey key = null;
		JSONObject pidConfig = null;
		try {
			ResponseEntity<String> entity = restTemplate.getForEntity(new URI(pidProviderConfig), String.class);
			String result = entity.getBody();
			pidConfig = new JSONObject(result);

		} catch (RestClientException | URISyntaxException e) {
			log.error("Error in pid provider config retrieval", e);

			throw new PidProviderConfigurationRetrievalException(e);
		}

		JSONObject oidcObj = pidConfig.getJSONObject("metadata").getJSONObject("openid_credential_issuer");
		JSONArray keys = oidcObj.getJSONObject("jwks").getJSONArray("keys");
		boolean found = false;
		for (int i = 0; i < keys.length() && !found; i++) {
			JSONObject object = (JSONObject) keys.get(i);
			if (object.get("use") != null && "sig".equals(object.get("use"))) {
				JWK jwk = JWK.parse(object.toMap());
				key = jwk.toRSAKey();
				found = true;
			}
		}

		return key;
	}

	public Map<String, Object> extractPidCredentialInfo(String pidCredential) {
		// align sd-jwt with the expected format
		if (!pidCredential.endsWith("~")) {
			pidCredential = pidCredential + "~";
		}
		SDJWT sdJwt = SDJWT.parse(pidCredential);
		List<Disclosure> disclosures = sdJwt.getDisclosures();
		return disclosures.stream().filter(ds -> CLAIMS_REQUIRED.contains(ds.getClaimName()))
				.collect(Collectors.toMap(Disclosure::getClaimName, Disclosure::getClaimValue));

	}

	public Map<String, Object> extractPidCredentialCnf(String pidCredential) throws ParseException {
		SDJWT sdJwt = SDJWT.parse(pidCredential);
		String credentialJwtString = sdJwt.getCredentialJwt();
		SignedJWT signedJwt = SignedJWT.parse(credentialJwtString);
		return signedJwt.getJWTClaimsSet().getJSONObjectClaim("cnf");
	}

}
