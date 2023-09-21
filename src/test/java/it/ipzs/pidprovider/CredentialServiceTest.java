package it.ipzs.pidprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.authlete.sd.Disclosure;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.pidprovider.dto.CredentialResponse;
import it.ipzs.pidprovider.dto.ProofRequest;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.oidclib.exception.OIDCException;
import it.ipzs.pidprovider.service.CredentialService;
import it.ipzs.pidprovider.service.SRService;
import it.ipzs.pidprovider.util.AccessTokenUtil;
import it.ipzs.pidprovider.util.CredentialProofUtil;
import it.ipzs.pidprovider.util.DpopUtil;
import it.ipzs.pidprovider.util.SdJwtUtil;
import it.ipzs.pidprovider.util.SessionUtil;

class CredentialServiceTest {

	@Mock
	private SRService srService;

	@Mock
	private DpopUtil dpopUtil;

	@Mock
	private AccessTokenUtil accessTokenUtil;

	@Mock
	private CredentialProofUtil proofUtil;

	@Mock
	private SdJwtUtil sdJwtUtil;

	@Mock
	private SessionUtil sessionUtil;

	private CredentialService credentialService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		credentialService = new CredentialService(srService, dpopUtil, accessTokenUtil, proofUtil, sdJwtUtil,
				sessionUtil);
	}

	@Test
	void testGenerateSdCredentialResponse()
			throws JOSEException, ParseException, NoSuchAlgorithmException, OIDCException {
		// Mock SRService
		when(srService.generateRandomByByteLength(anyInt())).thenReturn("nonce");
		when(sdJwtUtil.generateGenericDisclosure(anyString(), any())).thenReturn(new Disclosure("test", "abc"));

		ProofRequest proofClaims = mock(ProofRequest.class);
		SessionInfo si = mock(SessionInfo.class);
		when(sessionUtil.getSessionInfo(any())).thenReturn(si);
		// Test
		CredentialResponse response = credentialService.generateSdCredentialResponse(null, proofClaims);

		// Verify
		assertNotNull(response);
		assertEquals("vc+sd-jwt", response.getFormat());
		assertEquals("nonce", response.getNonce());
		assertEquals(86400, response.getNonceExpiresIn());
		verify(srService, times(1)).generateRandomByByteLength(32);
	}


	@Test
	void testCheckDpop() throws ParseException, JOSEException {
		// Mock DpopUtil
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(dpopUtil.parse(anyString())).thenReturn(jwtClaimsSet);
		when(dpopUtil.getHtuClaim(jwtClaimsSet)).thenReturn("https://example.com/credential");
		when(dpopUtil.getHtmClaim(jwtClaimsSet)).thenReturn("POST");

		// Test
		credentialService.checkDpop("DPoP");

		// Verify
		verify(dpopUtil, times(1)).parse("DPoP");
		verify(dpopUtil, times(1)).getHtuClaim(jwtClaimsSet);
		verify(dpopUtil, times(1)).getHtmClaim(jwtClaimsSet);
	}

	@Test
	void testCheckDpop_MissingClaim() throws ParseException, JOSEException {
		// Mock DpopUtil
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(dpopUtil.parse(anyString())).thenReturn(jwtClaimsSet);
		when(dpopUtil.getHtuClaim(jwtClaimsSet)).thenReturn("https://example.com/test");
		when(dpopUtil.getHtmClaim(jwtClaimsSet)).thenReturn("GET");

		// Test
		assertThrows(RuntimeException.class, () -> credentialService.checkDpop("DPoP"));

		// Verify
		verify(dpopUtil, times(1)).parse("DPoP");
		verify(dpopUtil, times(1)).getHtuClaim(jwtClaimsSet);
		verify(dpopUtil, times(1)).getHtmClaim(jwtClaimsSet);
	}

	@Test
	void testCheckDpop_parseError() throws ParseException, JOSEException {
		// Mock DpopUtil
		when(dpopUtil.parse(anyString())).thenThrow(new ParseException("Parse Error", 0));

		// Test and Verify
		assertThrows(RuntimeException.class, () -> credentialService.checkDpop("DPoP"));
	}

	@Test
	void testCheckAuthorizationAndProof() throws ParseException, JOSEException {
		// Mock AccessTokenUtil
		JWTClaimsSet tokenClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).issuer("abc123")
				.claim("nonce", "abc").build();
		when(accessTokenUtil.parse(anyString())).thenReturn(tokenClaims);

		// Mock ProofRequest
		ProofRequest proof = new ProofRequest();
		proof.setJwt("jwt");

		// Mock ProofUtil
		JWTClaimsSet proofClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).issuer("abc123")
				.claim("nonce", "abc").build();
		when(proofUtil.parse(anyString())).thenReturn(proofClaims);

		// Mock SessionUtil
		SessionInfo sessionInfo = mock(SessionInfo.class);
		when(sessionUtil.getSessionInfo(anyString())).thenReturn(sessionInfo);
		when(sessionInfo.getNonce()).thenReturn("abc");

		// Test
		credentialService.checkAuthorizationAndProof("authorization", proof);

		// Verify
		verify(accessTokenUtil, times(1)).parse("authorization");
		verify(proofUtil, times(1)).parse("jwt");
		verify(sessionUtil, times(1)).getSessionInfo("abc123");
		verify(sessionInfo, times(1)).getNonce();
	}

	@Test
	void testCheckAuthorizationAndProof_missingParameter() throws ParseException, JOSEException {
		// Mock AccessTokenUtil
		JWTClaimsSet tokenClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(accessTokenUtil.parse(anyString())).thenReturn(tokenClaims);

		// Mock ProofRequest
		ProofRequest proof = new ProofRequest();
		proof.setJwt("jwt");

		// Mock ProofUtil
		JWTClaimsSet proofClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();
		when(proofUtil.parse("jwt")).thenReturn(proofClaims);

		// Test and Verify
		assertThrows(IllegalArgumentException.class,
				() -> credentialService.checkAuthorizationAndProof("authorization", proof));
	}

	@Test
	void testCheckAuthorizationAndProof_nonceMismatch() throws ParseException, JOSEException {
		// Mock AccessTokenUtil
		JWTClaimsSet tokenClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).issuer("abc123").claim("nonce", "abc").build();
		when(accessTokenUtil.parse(anyString())).thenReturn(tokenClaims);

		// Mock ProofRequest
		ProofRequest proof = new ProofRequest();
		proof.setJwt("jwt");

		// Mock ProofUtil
		JWTClaimsSet proofClaims = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).issuer("abc123").claim("nonce", "cba").build();
		when(proofUtil.parse("jwt")).thenReturn(proofClaims);

		// Mock SessionUtil
		SessionInfo sessionInfo = mock(SessionInfo.class);
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(sessionInfo);
		when(sessionInfo.getNonce()).thenReturn("nonce");

		// Test and Verify
		assertThrows(IllegalArgumentException.class,
				() -> credentialService.checkAuthorizationAndProof("authorization", proof));
	}
}
