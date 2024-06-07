package it.ipzs.pidprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.pidprovider.dto.TokenResponse;
import it.ipzs.pidprovider.exception.InvalidHtmAndHtuClaimsException;
import it.ipzs.pidprovider.exception.PkceCodeValidationException;
import it.ipzs.pidprovider.exception.TokenCodeValidationException;
import it.ipzs.pidprovider.exception.TokenDpopParsingException;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.service.SRService;
import it.ipzs.pidprovider.service.TokenService;
import it.ipzs.pidprovider.util.DpopUtil;
import it.ipzs.pidprovider.util.SessionUtil;

@SpringBootTest
@TestPropertySource(properties = { "base-url=api.eudi-wallet-it-pid-provider.it" })
class TokenServiceTest {

	@MockBean
	private SRService srService;

	@MockBean
	private DpopUtil dpopUtil;

	@MockBean
	private SessionUtil sessionUtil;

	@Autowired
	private TokenService tokenService;

	@Test
	void testGenerateTokenResponse() throws ParseException, JOSEException {
		// Mock SRService
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setRequestUri("requestUri");
		sessionInfo.setVerified(false);
		sessionInfo.setState("state");
		sessionInfo.setClientId("clientId");
		when(srService.generateRandomByByteLength(32)).thenReturn("mockedNonce");
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(sessionInfo);
		when(dpopUtil.getKid(anyString())).thenReturn(UUID.randomUUID().toString());
		// Test
		TokenResponse response = null;
		response = tokenService.generateTokenResponse("clientId", "dpop");

		// Verify
		assertNotNull(response);
		assertEquals("DPoP", response.getTokenType());
		assertEquals(3600, response.getExpiresIn());
		assertEquals(86400, response.getNonceExpiresIn());
		assertEquals("mockedNonce", response.getNonce());
		assertNotNull(response.getAccessToken());
		verify(srService, times(1)).generateRandomByByteLength(32);
	}

	@Test
	void testCheckDpop() throws ParseException, JOSEException {
		// Mock DpopUtil
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).audience("https://localhost/token")
				.claim("htu", "https://localhost/token").claim("htm", "POST")
				.expirationTime(new Date(new Date().getTime() + 3600 * 1000)).build();

		when(dpopUtil.parse("dpop")).thenReturn(claimsSet);
		when(dpopUtil.getHtuClaim(claimsSet)).thenReturn("https://localhost/token");
		when(dpopUtil.getHtmClaim(claimsSet)).thenReturn("POST");

		// Test
		tokenService.checkDpop("dpop");

		// Verify (using argument captor to check log error)
		verify(dpopUtil, times(1)).parse("dpop");
		verify(dpopUtil, times(1)).getHtuClaim(claimsSet);
		verify(dpopUtil, times(1)).getHtmClaim(claimsSet);

	}

	@Test
	void testCheckDpopWrongClaims() throws ParseException, JOSEException {
		// Mock DpopUtil
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).audience("https://localhost/token")
				.claim("htu", "https://localhost/token").claim("htm", "POST")
				.expirationTime(new Date(new Date().getTime() + 3600 * 1000)).build();

		when(dpopUtil.parse("dpop")).thenReturn(claimsSet);
		when(dpopUtil.getHtuClaim(claimsSet)).thenReturn("https://localhost/test");
		when(dpopUtil.getHtmClaim(claimsSet)).thenReturn("GET");

		// Test
		assertThrows(InvalidHtmAndHtuClaimsException.class, () -> tokenService.checkDpop("dpop"));

	}

	@Test
	void testCheckDpopFailsParsing() throws ParseException, JOSEException {
		// Mock DpopUtil

		when(dpopUtil.parse("dpop")).thenThrow(ParseException.class);

		// Test
		assertThrows(TokenDpopParsingException.class, () -> tokenService.checkDpop("dpop"));

	}

	@Test
	void testCheckParams() throws NoSuchAlgorithmException {
		// Mock SessionUtil
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setCode("code");
		sessionInfo.setCodeChallenge("N1E4yRMD7xixn_oFyO_W3htYN3rY7-HMDKJe6z6r928");
		sessionInfo.setClientId("clientId");
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(sessionInfo);

		// Test
		tokenService.checkParams("clientId", "code", "codeVerifier");

		// Verify (using argument captor to check log error)
		verify(sessionUtil, times(1)).getSessionInfo("clientId");
	}

	@Test
	void testCheckParamsFails() throws NoSuchAlgorithmException {
		// Mock SessionUtil
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setCode("code");
		sessionInfo.setCodeChallenge("test");
		sessionInfo.setClientId("clientId");
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(sessionInfo);

		// Test
		assertThrows(PkceCodeValidationException.class,
				() -> tokenService.checkParams("clientId", "code", "codeVerifier"));

		// Test
		sessionInfo.setCode("test");
		assertThrows(TokenCodeValidationException.class,
				() -> tokenService.checkParams("clientId", "code", "codeVerifier"));

		// Verify (using argument captor to check log error)
		verify(sessionUtil, times(2)).getSessionInfo("clientId");
	}

}
