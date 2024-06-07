package it.ipzs.pidprovider.util;

import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.oidclib.OidcWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CallbackJwtUtil {

	private final OidcWrapper oidcWrapper;


	public String generateCallbackJwtResponse(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {

		log.info("state {}", state);
		log.info("code {}", si.getCode());

		JWK jwk = oidcWrapper.getCredentialIssuerJWK();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
				.keyID(jwk.getKeyID())
				.build();


		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer).claim("state", state).claim("code", si.getCode())
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);

		JWSSigner signer = null;

		if (jwk.getKeyType().equals(KeyType.EC))
			signer = new ECDSASigner(jwk.toECKey());
		else if (jwk.getKeyType().equals(KeyType.RSA))
			signer = new RSASSASigner(jwk.toRSAKey());
		else
			log.error("No valid key type from Wallet Instance Attestation");

		jwt.sign(signer);

		return jwt.serialize();
	}

}
