package it.ipzs.pidprovider.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import it.ipzs.pidprovider.exception.WIAConfigurationRetrievalException;
import it.ipzs.pidprovider.exception.WalletInstanceAttestationVerificationException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WalletInstanceUtil {

	public JWTClaimsSet parse(String walletInstance) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(walletInstance);
		validateWalletInstance(walletInstance);

		return jwt.getJWTClaimsSet();

	}

	public void validateWalletInstance(String walletInstance) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(walletInstance);

		JWK jwk = retrieveSigningKey(jwt.getJWTClaimsSet().getIssuer(), jwt.getHeader().getKeyID());

		if (jwk != null && jwk instanceof ECKey ecKey) {
			try {
				Jwts.parser().verifyWith(ecKey.toPublicKey()).build().parseSignedClaims(walletInstance);

			} catch (Exception e) {
				log.error("Wallet Instance Attestation JWT not verified", e);
				throw new WalletInstanceAttestationVerificationException(
						"Wallet Instance Attestation JWT not verified");
			}
		} else if (jwk != null && jwk instanceof RSAKey rsaKey) {
			try {
				Jwts.parser().verifyWith(rsaKey.toPublicKey()).build().parseSignedClaims(walletInstance);

			} catch (Exception e) {
				log.error("Wallet Instance Attestation JWT not verified", e);
				throw new WalletInstanceAttestationVerificationException(
						"Wallet Instance Attestation JWT not verified");
			}
		} else {
			log.error("Wallet Instance Attestation JWT not verified");
			throw new WalletInstanceAttestationVerificationException("Wallet Instance Attestation JWT not verified");
		}

	}

	public JWK retrieveSigningKey(String baseUrl, String kid) throws ParseException {
		RestTemplate restTemplate = new RestTemplate();
		JWK key = null;
		JSONObject pidConfig = null;
		try {
			ResponseEntity<String> entity = restTemplate
					.getForEntity(new URI(baseUrl.concat("/.well-known/openid-federation")), String.class);
			String result = entity.getBody();
			SignedJWT jwt = SignedJWT.parse(result);
			pidConfig = new JSONObject(jwt.getJWTClaimsSet().toJSONObject());

		} catch (RestClientException | URISyntaxException e) {
			log.error("Error in WIA config retrieval", e);
			throw new WIAConfigurationRetrievalException(e);
		}

		JSONObject oidcObj = pidConfig.getJSONObject("metadata").getJSONObject("wallet_provider");
		JSONArray keys = oidcObj.getJSONObject("jwks").getJSONArray("keys");
		boolean found = false;
		for (int i = 0; i < keys.length() && !found; i++) {
			JSONObject object = (JSONObject) keys.get(i);
			if (object.get("kid") != null && kid.equals(object.get("kid"))) {
				key = JWK.parse(object.toMap());
				found = true;
			}
		}

		return key;
	}
}
