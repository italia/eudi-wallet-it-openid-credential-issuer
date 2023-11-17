package it.ipzs.qeaaissuer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeaaissuer.util.DpopUtil;

class DpopUtilTest {

	@Mock
	private JWSVerifier jwsVerifier;

	private DpopUtil dpopUtil;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		dpopUtil = new DpopUtil();
	}

	@Test
	void testParse_validDPoP() throws ParseException, JOSEException {
		// Mock SignedJWT
		SignedJWT signedJWT = mock(SignedJWT.class);
		when(signedJWT.getHeader()).thenReturn(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("kid").build());
		when(signedJWT.getJWTClaimsSet()).thenReturn(new JWTClaimsSet.Builder().claim("htm", "POST")
				.claim("htu", "https://localhost/token").claim("iat", "1234567890").claim("jti", "abc123").build());
		when(signedJWT.verify(jwsVerifier)).thenReturn(true);

		// Test
		JWTClaimsSet claimsSet = dpopUtil.parse(
				"eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7Imt0eSI6IkVDIiwieCI6IkRKM2JLV3JlVXBleWV6dmYzcEs4WVQ2Q1FYSTJMUWp4cVVMQU5PRHVNMEEiLCJ5IjoiWUdKSGJGdVFOTjlZa2R1RjFEZHBMZ2FyY3R4OXRuNlZ1TENiZnpadGFmWSIsImNydiI6IlAtMjU2In0sImtpZCI6ImM0NmY5NjFmYTVlMjFlMzgwODQ2NjM4YTE2ZTY5ZDA1In0.eyJqdGkiOiItQndDM0VTYzZhY2MybFRjIiwiaHRtIjoiUE9TVCIsImh0dSI6Imh0dHBzOi8vcGlkLXByb3ZpZGVyLmV4YW1wbGUub3JnL3Rva2VuIiwiaWF0IjoxNTYyMjYyNjE2fQ.6pzzq8QKpjP1DrG1YrkNFV7LJbi0GtBLPQ6XJ6eWBbAkBZSYonujqhMnCiwmG0cWXEooZmTojZdX3RZeWHkQhg");

		// Verify
		assertNotNull(claimsSet);
		assertEquals("-BwC3ESc6acc2lTc", dpopUtil.getJtiClaim(claimsSet));
		assertEquals("POST", dpopUtil.getHtmClaim(claimsSet));
		assertEquals("https://pid-provider.example.org/token", dpopUtil.getHtuClaim(claimsSet));

	}

	@Test
	void testParse_invalidDPoP() throws ParseException, JOSEException {

		// Test and Verify
		assertThrows(IllegalArgumentException.class, (() -> dpopUtil.parse(
				"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.5mhBHqs5_DTLdINd9p5m7ZJ6XD0Xc55kIaCRY5r6HRA")));
	}

	@Test
	void testParse_missingClaims() throws ParseException, JOSEException {
		// Mock SignedJWT
		SignedJWT signedJWT = mock(SignedJWT.class);
		when(signedJWT.getHeader()).thenReturn(null);
		when(signedJWT.getJWTClaimsSet()).thenReturn(new JWTClaimsSet.Builder().claim("htu", "https://localhost/token")
				.claim("iat", "1234567890").claim("jti", "abc123").build());
		when(signedJWT.verify(jwsVerifier)).thenReturn(true);

		// Test and Verify
		assertThrows(IllegalArgumentException.class, (() -> dpopUtil.parse(
				"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")));
	}

	@Test
	void testGetJtiClaim() throws ParseException {
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).jwtID("abc123").build();

		// Test
		String jtiClaim = dpopUtil.getJtiClaim(claimsSet);

		// Verify
		assertEquals("abc123", jtiClaim);
	}

	@Test
	void testGetHtmClaim() throws ParseException {

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).claim("htm", "POST").build();

		// Test
		String htmClaim = dpopUtil.getHtmClaim(claimsSet);

		// Verify
		assertEquals("POST", htmClaim);
	}

	@Test
	void testGetHtuClaim() throws ParseException {

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(new Date())
				.jwtID(UUID.randomUUID().toString()).claim("htu", "https://localhost/token").build();

		// Test
		String htuClaim = dpopUtil.getHtuClaim(claimsSet);

		// Verify
		assertEquals("https://localhost/token", htuClaim);
	}

	@Test
	void testGetIatClaim() throws ParseException {
		// Mock JWTClaimsSet
		Date date = new Date();
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test").issueTime(date)
				.jwtID(UUID.randomUUID().toString()).build();

		// Test
		Date iatClaim = dpopUtil.getIatClaim(claimsSet);

		// Verify
		assertEquals(date, iatClaim);
	}

	@Test
	void testGetKid() throws ParseException {

		// Test
		String kid = dpopUtil.getKid(
				"eyJ0eXAiOiJkcG9wK2p3dCIsImFsZyI6IkVTMjU2IiwiandrIjp7Imt0eSI6IkVDIiwieCI6IkRKM2JLV3JlVXBleWV6dmYzcEs4WVQ2Q1FYSTJMUWp4cVVMQU5PRHVNMEEiLCJ5IjoiWUdKSGJGdVFOTjlZa2R1RjFEZHBMZ2FyY3R4OXRuNlZ1TENiZnpadGFmWSIsImNydiI6IlAtMjU2In0sImtpZCI6ImM0NmY5NjFmYTVlMjFlMzgwODQ2NjM4YTE2ZTY5ZDA1In0.eyJqdGkiOiItQndDM0VTYzZhY2MybFRjIiwiaHRtIjoiUE9TVCIsImh0dSI6Imh0dHBzOi8vcGlkLXByb3ZpZGVyLmV4YW1wbGUub3JnL3Rva2VuIiwiaWF0IjoxNTYyMjYyNjE2fQ.6pzzq8QKpjP1DrG1YrkNFV7LJbi0GtBLPQ6XJ6eWBbAkBZSYonujqhMnCiwmG0cWXEooZmTojZdX3RZeWHkQhg");

		// Verify
		assertEquals("c46f961fa5e21e380846638a16e69d05", kid);
	}
}
