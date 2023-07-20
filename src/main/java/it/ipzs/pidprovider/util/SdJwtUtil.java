package it.ipzs.pidprovider.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.pidprovider.dto.VerifiedClaims;

@Component
public class SdJwtUtil {

	public Disclosure generateGenericDisclosure(String salt, String claimName, Object claimValueObject) {
		return new Disclosure(salt, claimName, claimValueObject);
	}

	public Disclosure generateGenericDisclosure(String claimName, Object claimValueObject) {
		return new Disclosure(claimName, claimValueObject);
	}

	public String generateKeyBindingJwt(String nonce, String kid) throws JOSEException {


		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("kb+jwt")).build();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issueTime(new Date())
				.audience(List.of("http://localhost:8080")).claim("nonce", nonce).build();


		SignedJWT jwt = new SignedJWT(header, claimsSet);

		ECKey privateKey = new ECKeyGenerator(Curve.P_256).keyID(kid).generate();

		JWSSigner signer = new ECDSASigner(privateKey);

		jwt.sign(signer);

		return jwt.serialize();
	}

	public String generateCredential(String kid, VerifiedClaims vc)
			throws JOSEException, NoSuchAlgorithmException {

		// TODO implement trust chain

		List<String> localTrustChain = List.of(
				"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaWQtcHJvdmlkZXIiLCJpYXQiOjE2ODc3MDQzNjgsImV4cCI6MTc4Nzc0MDM2OH0.ZEE7hkHVCPwXGgo7035865YZl2MTf4o05NUTTQ-LRXc",
				"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cnVzdC1yZWdpc3RyeSIsImlhdCI6MTY4NzcwNDM2OCwiZXhwIjoxNzg3NzQwMzY4fQ.V-yHsWkfdCiK7trm4xLJLpdxi5LJThvzJNM_tWYzzQE");
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("vc+sd-jwt")).keyID(kid)
				.customParam("trust_chain", localTrustChain)
				.build();

		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair keyPair = gen.generateKeyPair();

		JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate()).keyUse(KeyUse.SIGNATURE).keyID(kid)
				.issueTime(new Date()).keyIDFromThumbprint().build();


		Cnf cnf = new Cnf();
		JWKDto jwkdto = new JWKDto();

		jwkdto.setKid(jwk.getKeyID());
		jwkdto.setKty(jwk.getKeyType().getValue());
		jwkdto.setUse(jwk.getKeyUse().getValue());
		jwkdto.setN(jwk.toJSONObject().get("n").toString());
		jwkdto.setE(jwk.toJSONObject().get("e").toString());

		cnf.setJwk(jwkdto);
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.issuer("http://localhost:8080")
				.subject(jwk.computeThumbprint().toString())
				.jwtID("urn:uuid:".concat(UUID.randomUUID().toString()))
				.expirationTime(new Date(new Date().getTime() + 86400 * 1000))
				.claim("verified_claims", vc)
				.claim("_sd_alg", "sha-256")
				.claim("status", "http://localhost:8080/status")
				.claim("type", "PersonIdentificationData")
				.claim("cnf", cnf)
				.build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);


		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

}

class Cnf {
	private JWKDto jwk;

	public JWKDto getJwk() {
		return jwk;
	}

	public void setJwk(JWKDto jwk) {
		this.jwk = jwk;
	}
}

class JWKDto {
	private String kty;
	private String use;
	private String n;
	private String e;
	private String kid;

	public String getKty() {
		return kty;
	}

	public void setKty(String kty) {
		this.kty = kty;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getN() {
		return n;
	}

	public void setN(String n) {
		this.n = n;
	}

	public String getE() {
		return e;
	}

	public void setE(String e) {
		this.e = e;
	}

	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}
}
