package it.ipzs.qeeaissuer.util;

import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.oidclib.OidcWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CallbackResponseJwtUtil {

	private final OidcWrapper oidcWrapper;

	public String generateDirectPostResponse(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {

		log.info("state {}", state);
		log.info("code {}", si.getCode());

		JWK jwk = oidcWrapper.getJWK();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer(issuer).claim("state", state)
				.claim("code", si.getCode()).build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);

		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

}
