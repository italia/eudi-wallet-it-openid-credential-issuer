package it.ipzs.pidprovider.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.authlete.sd.Disclosure;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.pidprovider.dto.VerifiedClaims;
import it.ipzs.pidprovider.oidclib.OidcWrapper;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SdJwtUtil {

	private final OidcWrapper oidcWrapper;

	public Disclosure generateGenericDisclosure(String salt, String claimName, Object claimValueObject) {
		return new Disclosure(salt, claimName, claimValueObject);
	}

	public Disclosure generateGenericDisclosure(String claimName, Object claimValueObject) {
		return new Disclosure(claimName, claimValueObject);
	}

	public String generateKeyBindingJwt(String nonce, String kid) throws JOSEException {


		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("kb+jwt")).build();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issueTime(new Date())
				.audience(List.of("https://api.eudi-wallet-it-pid-provider.it")).claim("nonce", nonce).build();


		SignedJWT jwt = new SignedJWT(header, claimsSet);

		ECKey privateKey = new ECKeyGenerator(Curve.P_256).keyID(kid).generate();

		JWSSigner signer = new ECDSASigner(privateKey);

		jwt.sign(signer);

		return jwt.serialize();
	}

	public String generateCredential(VerifiedClaims vc, JWK dpopJwk)
			throws JOSEException, ParseException {


		JWK jwk = oidcWrapper.getCredentialIssuerJWK();
		List<String> trustChain = oidcWrapper.getCredentialIssuerTrustChain();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("vc+sd-jwt"))
				.keyID(jwk.getKeyID())
				.customParam("trust_chain", trustChain)
				.build();

		JSONObject cnfObj = new JSONObject();
		cnfObj.put("jwk", dpopJwk.toPublicJWK().toJSONObject());

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date validityEndDate = cal.getTime();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.issuer("https://api.eudi-wallet-it-pid-provider.it")
				.subject(dpopJwk.computeThumbprint().toString())
				.jwtID("urn:uuid:".concat(UUID.randomUUID().toString()))
				.expirationTime(validityEndDate)
				.claim("verified_claims", vc)
				.claim("_sd_alg", "sha-256")
				.claim("status", "https://api.eudi-wallet-it-pid-provider.it/status") // TODO implementation
				.claim("type", "PersonIdentificationData")
				.claim("cnf", cnfObj.toMap())
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);


		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

}
