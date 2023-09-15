package it.ipzs.qeeaissuer.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeeaissuer.dto.TokenResponse;
import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.util.DpopUtil;
import it.ipzs.qeeaissuer.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

	private final SRService srService;
	private final DpopUtil dpopUtil;
	private final SessionUtil sessionUtil;

	public TokenResponse generateTokenResponse(String clientId, String dpop) throws JOSEException, ParseException {
		String nonce = srService.generateRandomByByteLength(32);
		TokenResponse response = TokenResponse.builder().tokenType("DPoP").expiresIn(3600).nonceExpiresIn(86400)
				.nonce(nonce).accessToken(generateAccessTokenFromDpop(dpop, nonce)).build();

		saveNonce(clientId, nonce);
		return response;
	}

	private void saveNonce(String clientId, String nonce) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		sessionInfo.setNonce(nonce);
		sessionUtil.putSessionInfo(sessionInfo);

	}

	private String generateAccessTokenFromDpop(String dpop, String nonce) throws JOSEException, ParseException {

		ECKey ecJWK = new ECKeyGenerator(Curve.P_256).keyID(dpopUtil.getKid(dpop)).generate(); // TODO kid check
		String jkt = ecJWK.computeThumbprint().toString();
		JWSSigner signer = new ECDSASigner(ecJWK);

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject("sub_pairwise")
				.issueTime(new Date())
				.jwtID(UUID.randomUUID().toString())
				.audience("https://api.eudi-wallet-it-pid-provider.it/token") // TODO must match client_id
				.claim("client_id", "https://api.eudi-wallet-it-pid-provider.it/token") // TODO https url that identifies RP
				.claim("nonce", nonce)
				.claim("jkt", jkt)
				.expirationTime(new Date(new Date().getTime() + 3600 * 1000)) // TODO config expiration time
				.build();

		SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecJWK.getKeyID()).build(),
				claimsSet);
		signedJWT.sign(signer);

		return signedJWT.serialize();
	}

	public void checkDpop(String dpop) {
		try {
			JWTClaimsSet claimsSet = dpopUtil.parse(dpop);
			String htuClaim = dpopUtil.getHtuClaim(claimsSet);
			String htmClaim = dpopUtil.getHtmClaim(claimsSet);

			// TODO better check uri
			if (!htuClaim.endsWith("/token") || !htmClaim.equals("POST")) {
				throw new IllegalArgumentException("Invalid claims: " + htmClaim + " - " + htuClaim);
			}
		} catch (ParseException | JOSEException e) {
			log.error("", e);
			throw new RuntimeException("", e);
		}
	}

	public void checkParams(String clientId, String code, String codeVerifier) throws NoSuchAlgorithmException {
		log.debug("clientId {} - code {} - codeVerifier {}", clientId, code, codeVerifier);
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		log.debug("sessionInfo {}", sessionInfo);
		if (sessionInfo != null && sessionInfo.getCode().equals(code)) {
			String calculatedCodeChallenge = encodeForPkceVerify(codeVerifier);
			if (!calculatedCodeChallenge.equals(sessionInfo.getCodeChallenge())) {
				log.error(
						"Error pkce validation: codeChallenge calculated {} - codeChallenge in session {} - codeVerifier {}",
						calculatedCodeChallenge, sessionInfo.getCodeChallenge(), codeVerifier);
				throw new IllegalArgumentException("No match between code verifier and code challenge");
			}
		} else
			throw new IllegalArgumentException("No match between code and client id");

	}

	private String encodeForPkceVerify(final String clearText) throws NoSuchAlgorithmException {
		byte[] bytes = clearText.getBytes(StandardCharsets.US_ASCII);
		MessageDigest instance = MessageDigest.getInstance("SHA-256");
		instance.update(bytes, 0, bytes.length);
		String encoded = new String(Base64.getUrlEncoder()
				.encode(instance.digest()));
		return encoded.replaceAll("=$", "");
	}

}
