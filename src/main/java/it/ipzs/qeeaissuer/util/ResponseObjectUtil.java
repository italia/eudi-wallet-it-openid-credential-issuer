package it.ipzs.qeeaissuer.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
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

@Component
@AllArgsConstructor
public class ResponseObjectUtil {

	private final OidcWrapper oidcWrapper;


	public String generateResponseObject(SessionInfo sessionInfo)
			throws JOSEException, ParseException {

		JWK jwk = oidcWrapper.getJWK();
		List<String> trustChain = oidcWrapper.getCredentialIssuerTrustChain();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("jwt"))
				.keyID(jwk.getKeyID())
				.customParam("trust_chain", trustChain)
				.build();


		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date validityEndDate = cal.getTime();

		// TODO generate JWT with real data
		if (sessionInfo == null) {
			// TODO remove after implementation
			sessionInfo = new SessionInfo();
			sessionInfo.setState(UUID.randomUUID().toString());
			sessionInfo.setNonce(UUID.randomUUID().toString());
		} else if (sessionInfo.getNonce() == null) {
			// TODO remove after implementation
			sessionInfo.setNonce(UUID.randomUUID().toString());
		}

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.issuer("https://api.eudi-wallet-it-pid-provider.it")
				.jwtID("urn:uuid:".concat(UUID.randomUUID().toString()))
				.expirationTime(validityEndDate)
				.claim("scope", "eu.europa.ec.eudiw.pid.it.1 pid-sd-jwt:unique_id+given_name+family_name")
				.claim("client_id_scheme", "entity_id")
				.claim("client_id", "https://api.eudi-wallet-it-pid-provider.it")
				.claim("response_mode", "direct_post.jwt")
				.claim("response_type", "vp_token")
				.claim("response_uri", "https://api.eudi-wallet-it-pid-provider.it/callback")
				.claim("state", sessionInfo.getState())
				.claim("nonce", sessionInfo.getNonce())
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);


		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

}
