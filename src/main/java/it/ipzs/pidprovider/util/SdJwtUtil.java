package it.ipzs.pidprovider.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.authlete.sd.Disclosure;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import it.ipzs.pidprovider.config.KeyStoreConfig;
import it.ipzs.pidprovider.dto.Cnf;
import it.ipzs.pidprovider.dto.VerifiedClaims;
import it.ipzs.pidprovider.model.SessionInfo;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SdJwtUtil {

	private final KeyStoreConfig ksConfig;

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

	public String generateCredential(SessionInfo sessionInfo, VerifiedClaims vc) throws JOSEException {

		// TODO implement trust chain

		JWK jwk = ksConfig.getKey();
		List<String> localTrustChain = List.of(
				"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaWQtcHJvdmlkZXIiLCJpYXQiOjE2ODc3MDQzNjgsImV4cCI6MTc4Nzc0MDM2OH0.ZEE7hkHVCPwXGgo7035865YZl2MTf4o05NUTTQ-LRXc",
				"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cnVzdC1yZWdpc3RyeSIsImlhdCI6MTY4NzcwNDM2OCwiZXhwIjoxNzg3NzQwMzY4fQ.V-yHsWkfdCiK7trm4xLJLpdxi5LJThvzJNM_tWYzzQE");
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("vc+sd-jwt"))
				.keyID(jwk.getKeyID())
				.customParam("trust_chain", localTrustChain)
				.build();


		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date validityEndDate = cal.getTime();

		String cnfKidClaim = extractKidFromCnf(sessionInfo.getCnf());
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.issuer("https://api.eudi-wallet-it-pid-provider.it")
				.subject(cnfKidClaim)
				.jwtID("urn:uuid:".concat(UUID.randomUUID().toString()))
				.expirationTime(validityEndDate)
				.claim("verified_claims", vc)
				.claim("_sd_alg", "sha-256")
				.claim("status", "https://api.eudi-wallet-it-pid-provider.it/status") // TODO implementation
				.claim("type", "PersonIdentificationData")
				.claim("cnf", sessionInfo.getCnf())
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);


		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

	private String extractKidFromCnf(Object cnfObj) {
		ObjectMapper om = new ObjectMapper();
		Cnf cnf = om.convertValue(cnfObj, Cnf.class);
		return cnf.getJwk().getKid();
	}

}
