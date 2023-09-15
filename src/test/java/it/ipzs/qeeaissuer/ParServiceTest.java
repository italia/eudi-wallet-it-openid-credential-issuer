package it.ipzs.qeeaissuer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.qeeaissuer.dto.Cnf;
import it.ipzs.qeeaissuer.dto.ParResponse;
import it.ipzs.qeeaissuer.service.ParService;
import it.ipzs.qeeaissuer.util.ParRequestJwtUtil;
import it.ipzs.qeeaissuer.util.SessionUtil;
import it.ipzs.qeeaissuer.util.WalletInstanceUtil;

class ParServiceTest {

	@Mock
	private WalletInstanceUtil walletInstanceUtil;

	@Mock
	private ParRequestJwtUtil parRequestJwtUtil;

	@Mock
	private SessionUtil sessionUtil;

	private ParService parService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		parService = new ParService(walletInstanceUtil, parRequestJwtUtil, sessionUtil);
	}

	@Test
	void testValidateClientAssertion() throws ParseException, JOSEException {
		// Mock WalletInstanceUtil
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(walletInstanceUtil.parse(anyString())).thenReturn(jwtClaimsSet);

		// Test
		parService.validateClientAssertionAndRetrieveCnf("clientAssertion");

		// Verify
		verify(walletInstanceUtil, times(1)).parse("clientAssertion");
	}

	@Test
	void testGenerateRequestUri_validRequest() throws ParseException, JOSEException {
		// Mock ParRequestJwtUtil

		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").claim("redirect_uri", "http://localhost")
				.claim("client_id", "http://localhost").claim("state", "http://localhost")
				.claim("code_challenge", "http://localhost")

				.build();
		when(parRequestJwtUtil.parse(anyString())).thenReturn(jwtClaimsSet);
		Cnf cnf = mock(Cnf.class);


		// Test
		ParResponse response = parService.generateRequestUri("validRequest", cnf);

		// Verify
		assertNotNull(response);
		assertNotNull(response.getRequestUri());
		assertEquals(60, response.getExpiresIn());
	}

	@Test
	void testGenerateRequestUri_missingParameter() throws ParseException, JOSEException {
		// Mock ParRequestJwtUtil
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(parRequestJwtUtil.parse(anyString())).thenReturn(jwtClaimsSet);
		Cnf cnf = mock(Cnf.class);

		// Test and Verify
		assertThrows(IllegalArgumentException.class,
				() -> parService.generateRequestUri("missingParameterRequest", cnf));
		
	}

	@Test
	void testGenerateRequestUri_parseError() throws ParseException, JOSEException {
		// Mock ParRequestJwtUtil
		when(parRequestJwtUtil.parse(anyString())).thenThrow(new ParseException("Parse Error", 0));
		Cnf cnf = mock(Cnf.class);
		// Test and Verify
		assertThrows(RuntimeException.class, () -> parService.generateRequestUri("parseErrorRequest", cnf));
	}
}

