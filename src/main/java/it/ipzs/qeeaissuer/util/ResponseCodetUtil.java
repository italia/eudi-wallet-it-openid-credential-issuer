package it.ipzs.qeeaissuer.util;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
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
public class ResponseCodetUtil {

	private final OidcWrapper oidcWrapper;


	public String generateResponseCode(SessionInfo sessionInfo)
			throws JOSEException, ParseException {

		JWK jwk = oidcWrapper.getCredentialIssuerJWK();
		List<String> trustChain = oidcWrapper.getCredentialIssuerTrustChain();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("jwt"))
				.keyID(jwk.getKeyID())
				.customParam("trust_chain", trustChain)
				.build();

		// TODO generate JWT with real data
		if (sessionInfo == null) {
			// TODO remove fake data after implementation
			sessionInfo = new SessionInfo();
			sessionInfo.setState(UUID.randomUUID().toString());
			sessionInfo.setVpToken(
					"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImUwYmJmMmYxLThjM2EtNGVhYi1hOGFjLTJlOGYzNGRiOGE0NyJ9.eyJpc3MiOiJodHRwczovL3dhbGxldC1wcm92aWRlci5leGFtcGxlLm9yZy9pbnN0YW5jZS92YmVYSmtzTTQ1eHBodEFObkNpRzZtQ3l1VTRqZkdOem9wR3VLdm9nZzljIiwianRpIjoiMzk3ODM0NGYtODU5Ni00YzNhLWE5NzgtOGZjYWJhMzkwM2M1IiwiYXVkIjoiaHR0cHM6Ly92ZXJpZmllci5leGFtcGxlLm9yZy9jYWxsYmFjayIsImlhdCI6MTU0MTQ5MzcyNCwiZXhwIjoxNTczMDI5NzIzLCJub25jZSI6Im4tMFM2X1d6QTJNaiIsInZwIjoiPFNELUpXVD5-PERpc2Nsb3N1cmUgMT5-PERpc2Nsb3N1cmUgMj5-Li4ufjxEaXNjbG9zdXJlIE4-In0.uvSwmRgdFf2tQjKO-QHt0lNG15XI3P103a5tPcWj8jg");
		}

		JSONObject ps = new JSONObject();
		ps.put("definition_id", "32f54163-7166-48f1-93d8-ff217bdb0653");
		ps.put("id", "04a98be3-7fb0-4cf5-af9a-31579c8b0e7d");
		
		JSONArray ja = new JSONArray();
		
		JSONObject dm = new JSONObject();
		dm.put("id", "pid-sd-jwt:unique_id+given_name+family_name");
		dm.put("path", "$.vp_token.verified_claims.claims._sd[0]");
		dm.put("format", "vc+sd-jwt");
		ja.put(dm);
		
		ps.put("descriptor_map", ja.toList());
		
		
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.claim("presentation_submission", ps.toMap())
				.claim("vp_token", sessionInfo.getVpToken())
				.claim("state", sessionInfo.getState())
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);


		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

}
