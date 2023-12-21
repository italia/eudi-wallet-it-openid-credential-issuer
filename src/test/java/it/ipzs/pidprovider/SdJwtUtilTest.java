package it.ipzs.pidprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.authlete.sd.Disclosure;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;

import it.ipzs.pidprovider.dto.VerifiedClaims;
import it.ipzs.pidprovider.util.SdJwtUtil;

@SpringBootTest
class SdJwtUtilTest {

	@Mock
	private ECKeyGenerator ecKeyGenerator;

	@Mock
	private RSASSASigner rsassasigner;

	@Mock
	private JWSSigner ecdsaSigner;

	@Autowired
	private SdJwtUtil sdJwtUtil;

	@Test
	void testGenerateGenericDisclosure_withSalt() {
		// Test
		Disclosure disclosure = sdJwtUtil.generateGenericDisclosure("salt", "claimName", "claimValue");

		// Verify
		assertNotNull(disclosure);
		assertEquals("salt", disclosure.getSalt());
		assertEquals("claimName", disclosure.getClaimName());
		assertEquals("claimValue", disclosure.getClaimValue());
	}

	@Test
	void testGenerateGenericDisclosure_withoutSalt() {
		// Test
		Disclosure disclosure = sdJwtUtil.generateGenericDisclosure("claimName", "claimValue");

		// Verify
		assertNotNull(disclosure);
		assertNotNull(disclosure.getSalt());
		assertEquals("claimName", disclosure.getClaimName());
		assertEquals("claimValue", disclosure.getClaimValue());
	}

	@Test
	void testGenerateKeyBindingJwt() throws JOSEException {
		// Test
		String keyBindingJwt = sdJwtUtil.generateKeyBindingJwt("nonce", "kid");

		// Verify
		assertNotNull(keyBindingJwt);
	}

	@Test
	void testGenerateCredentialt() throws JOSEException, ParseException, NoSuchAlgorithmException {
		// Test
		VerifiedClaims vc = new VerifiedClaims();
		vc.setClaims(Map.of("test", "value"));
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair keyPair = gen.generateKeyPair();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date validityEndDate = cal.getTime();

		JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate()).keyUse(KeyUse.SIGNATURE)
				.issueTime(new Date()).expirationTime(validityEndDate)
				.keyIDFromThumbprint().build();

		String credJwt = sdJwtUtil.generateCredential(vc, jwk);

		// Verify
		assertNotNull(credJwt);
	}

}

