package it.ipzs.pidprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.authlete.sd.Disclosure;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;

import it.ipzs.pidprovider.util.SdJwtUtil;

class SdJwtUtilTest {

	@Mock
	private ECKeyGenerator ecKeyGenerator;

	@Mock
	private RSASSASigner rsassasigner;

	@Mock
	private JWSSigner ecdsaSigner;

	private SdJwtUtil sdJwtUtil;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		sdJwtUtil = new SdJwtUtil();
	}

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

}

