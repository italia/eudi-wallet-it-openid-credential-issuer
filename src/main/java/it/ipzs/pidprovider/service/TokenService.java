package it.ipzs.pidprovider.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

import it.ipzs.pidprovider.dto.TokenResponse;
import it.ipzs.pidprovider.exception.InvalidHtmAndHtuClaimsException;
import it.ipzs.pidprovider.exception.PkceCodeValidationException;
import it.ipzs.pidprovider.exception.SessionInfoByClientIdNotFoundException;
import it.ipzs.pidprovider.exception.TokenCodeValidationException;
import it.ipzs.pidprovider.exception.TokenDpopParsingException;
import it.ipzs.pidprovider.exception.WalletInstanceAttestationVerificationException;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.util.DpopUtil;
import it.ipzs.pidprovider.util.SessionUtil;
import it.ipzs.pidprovider.util.WalletInstanceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

	private final SRService srService;
	private final DpopUtil dpopUtil;
	private final SessionUtil sessionUtil;
	private final WalletInstanceUtil walletInstanceUtil;
	
	@Value("${base-url}")
	private String baseUrl;

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
		String url = "https://".concat(baseUrl).concat("/token");

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject("sub_pairwise")
				.issueTime(new Date())
				.jwtID(UUID.randomUUID().toString())
				.audience(url) // TODO must match client_id
				.claim("client_id", url) // TODO https url that identifies RP
				.claim("nonce", nonce)
				.claim("jkt", jkt).expirationTime(new Date(new Date().getTime() + 3600 * 1000)) // TODO config expiration time
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
				log.error("Invaild claims: htmCaim {} - htuClaim {}", htmClaim, htuClaim);
				throw new InvalidHtmAndHtuClaimsException("Invalid claims: htmClaim " + htmClaim + " - htuClaim " + htuClaim);
			}
		} catch (ParseException | JOSEException e) {
			log.error("Token request Dpop parse error", e);
			throw new TokenDpopParsingException("Token request Dpop parse error");

		}
	}

	public void checkParams(String clientId, String code, String codeVerifier) {
		log.debug("clientId {} - code {} - codeVerifier {}", clientId, code, codeVerifier);
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		log.debug("sessionInfo {}", sessionInfo);
		if (sessionInfo != null) {
			if (sessionInfo.getCode().equals(code)) {
				String calculatedCodeChallenge = "";
				try {
					calculatedCodeChallenge = encodeForPkceVerify(codeVerifier);
				} catch (NoSuchAlgorithmException e) {
					log.error("Error in encoding codeVerifier string for PKCE check", e);
					throw new PkceCodeValidationException("Error with PKCE algorithm");
				}

				if (!calculatedCodeChallenge.equals(sessionInfo.getCodeChallenge())) {
					log.error(
							"Error pkce validation: codeChallenge calculated {} - codeChallenge in session {} - codeVerifier {}",
							calculatedCodeChallenge, sessionInfo.getCodeChallenge(), codeVerifier);
					throw new PkceCodeValidationException("No match between code verifier and code challenge");
				}
			} else
				throw new TokenCodeValidationException("No match between code and client id");
		} else {
			log.error("Session info null, clientId unknown");
			throw new SessionInfoByClientIdNotFoundException("Session info null, clientId unknown");

		}

	}

	private String encodeForPkceVerify(final String clearText) throws NoSuchAlgorithmException {
		byte[] bytes = clearText.getBytes(StandardCharsets.US_ASCII);
		MessageDigest instance = MessageDigest.getInstance("SHA-256");
		instance.update(bytes, 0, bytes.length);
		String encoded = new String(Base64.getUrlEncoder().encode(instance.digest()));
		return encoded.replaceAll("=$", "");
	}

	public void validateClientAssertion(String client_assertion_type, String client_assertion) {
		if ("urn:ietf:params:oauth:client-assertion-type:jwt-client-attestation".equals(client_assertion_type)) {
			try {
				walletInstanceUtil.parse(client_assertion);
			} catch (ParseException | JOSEException e) {
				log.error("", e);
				throw new WalletInstanceAttestationVerificationException("Malformed Wallet Instance Attestation JWT");
			}
		}
	}
}
