package it.ipzs.qeaaissuer.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.qeaaissuer.dto.PresentationCallbackDto;
import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.util.CallbackResponseJwtUtil;
import it.ipzs.qeaaissuer.util.DpopUtil;
import it.ipzs.qeaaissuer.util.ResponseCodetUtil;
import it.ipzs.qeaaissuer.util.ResponseObjectUtil;
import it.ipzs.qeaaissuer.util.SessionUtil;
import it.ipzs.qeaaissuer.util.VpTokenJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QeaaIssuerService {

	private final ResponseObjectUtil responseObjectUtil;
	private final SessionUtil sessionUtil;
	private final ResponseCodetUtil responseCodetUtil;
	private final CallbackResponseJwtUtil callbackResponseJwtUtil;
	private final DpopUtil dpopUtil;
	private final SRService srService;
	private final PidCredentialService pidService;
	private final VpTokenJwtUtil vpTokenUtil;

	@Value("${vp-token.aud-uri:https://api.eudi-wallet-it-issuer.it/callback}")
	private String audUriClaim;

	public String generateRequestObjectResponse(SessionInfo si) {
		log.debug("generating response object...");
		try {
			String nonce = srService.generateRandomByByteLength(32);
			si.setRequestUriNonce(nonce);
			sessionUtil.putSessionInfo(si);
			return responseObjectUtil.generateResponseObject(si);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return null;
		}
	}

	public String generateResponseCode(SessionInfo si) {
		return responseCodetUtil.generateResponseCode(si);
	}

	public String generateCallbackResponseJwt(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {
		return callbackResponseJwtUtil.generateDirectPostResponse(si, state, issuer);
	}

	public String generateAuthResponseJwt(String clientId, String requestUri) throws JOSEException, ParseException {
		return callbackResponseJwtUtil.generateAuthDirectPostResponse(clientId, requestUri);
	}

	public SessionInfo retrieveSessionAndCheckWia(String wia) {
		try {
			SessionInfo sessionInfoByWia = sessionUtil.getSessionInfoByWia(wia);
			if (sessionInfoByWia == null) {
				throw new RuntimeException("Session not found by wallet instance attestation");
			}
			String hashedWia = sessionInfoByWia.getHashedWia();
			String generatedHashedWia = generateHashedWia(wia);
			if (!generatedHashedWia.equals(hashedWia)) {
				throw new RuntimeException("Hashed wallet instance attestation doesn't match: wia in session "
						+ hashedWia + " - hashed wia generated " + generatedHashedWia);
			}
			return sessionInfoByWia;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public void checkDpop(String wiaDpop, String wiaAuth) {
		try {
			JWTClaimsSet claimsSet = dpopUtil.parse(wiaDpop);
			String htuClaim = dpopUtil.getHtuClaim(claimsSet);
			String htmClaim = dpopUtil.getHtmClaim(claimsSet);
			String ath = dpopUtil.getAthClaim(claimsSet);

			if (!htuClaim.contains("/request_uri") || !htmClaim.equals("GET")) {
				throw new IllegalArgumentException("Invalid claims: " + htmClaim + " - " + htuClaim);
			}
			String generateHashedWia = generateHashedWia(wiaAuth);
			if (ath == null) {
				throw new IllegalArgumentException("Missing ath claim");
			}
			if (!generateHashedWia.equals(ath)) {
				log.error("ath {} - hashed generated {}", ath, generateHashedWia);
				log.error("wia {}", wiaAuth);
				throw new IllegalArgumentException("Invalid ath claim: " + ath + " - hashedWia " + generateHashedWia);
			}
		} catch (ParseException | JOSEException e) {
			log.error("", e);
			throw new RuntimeException("", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
		}

	}

	public SessionInfo checkAndReadDirectPostResponse(String directPostParam) throws Exception {
		try {
			PresentationCallbackDto decryptedPresentation = callbackResponseJwtUtil.decrypt(directPostParam);
			SessionInfo sessionInfoByState = sessionUtil.getSessionInfoByState(decryptedPresentation.getState());
			validatePresentationNonce(decryptedPresentation, sessionInfoByState);
			JWTClaimsSet vpTokenClaims = vpTokenUtil.parse(decryptedPresentation.getVp_token());
			verifyVpTokenClaims(vpTokenClaims, sessionInfoByState);
			Map<String, Object> cnfPid = pidService.extractPidCredentialCnf(vpTokenClaims.getStringClaim("vp"));
			vpTokenUtil.validate(decryptedPresentation.getVp_token(), cnfPid);
			verifyPidCredentialsAndStorePidClaims(vpTokenClaims, sessionInfoByState);
			log.info("session found: {}", sessionInfoByState);
			return sessionInfoByState;
		} catch (JOSEException | ParseException | NoSuchAlgorithmException e) {
			log.error("", e);
			throw e;
		}

	}

	private void verifyPidCredentialsAndStorePidClaims(JWTClaimsSet vpTokenClaims, SessionInfo si)
			throws ParseException, JOSEException {
		String vpClaim = vpTokenClaims.getStringClaim("vp");
		if (vpClaim == null) {
			log.error("vp claim validation in vp_token failed - vp claim null");
			throw new RuntimeException("vc claim validation in vp_token failed");
		}
		pidService.validatePidCredential(vpClaim);
		Map<String, Object> pidCredentialInfo = pidService.extractPidCredentialInfo(vpClaim);
		si.setPidCredentialClaims(pidCredentialInfo);
		sessionUtil.putSessionInfo(si);

	}

	private void verifyVpTokenClaims(JWTClaimsSet vpTokenClaims, SessionInfo si) throws ParseException {
		Object audClaimObj = vpTokenClaims.getClaim("aud");
		if (audClaimObj instanceof Collection<?>) {
			List<String> audClaimList = ((Collection<?>) audClaimObj).stream()
					.filter(String.class::isInstance).map(String.class::cast).toList();

			if (!audClaimList.contains(audUriClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaimList);
				throw new RuntimeException("aud claim validation in vp_token failed");
			}
			String nonceClaim = vpTokenClaims.getStringClaim("nonce");
			if (!si.getRequestUriNonce().equals(nonceClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaimList);
				throw new RuntimeException("aud claim validation in vp_token failed");
			}
		} else {
			String audClaim = (String) audClaimObj;
			log.info("aud claim {}", audClaim);
			if (!audUriClaim.equals(audClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaim);
				throw new RuntimeException("aud claim validation in vp_token failed");
			}
			String nonceClaim = vpTokenClaims.getStringClaim("nonce");
			if (!si.getRequestUriNonce().equals(nonceClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaim);
				throw new RuntimeException("aud claim validation in vp_token failed");
			}
		}


	}

	private void validatePresentationNonce(PresentationCallbackDto pres, SessionInfo si) {
		String requestUriNonce = si.getRequestUriNonce();
		String nonceFromPresentation = pres.getNonce();
		if (!(Objects.nonNull(nonceFromPresentation) && Objects.nonNull(requestUriNonce)
				&& nonceFromPresentation.equals(requestUriNonce))) {
			log.error(
					"Nonce validation failed in authorization callback: nonce from session {} - nonce from callback request {}",
					requestUriNonce, nonceFromPresentation);
			throw new RuntimeException("Nonce validation failed in authorization callback");
		}

	}

	private String generateHashedWia(String clientAssertion) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = digest.digest(clientAssertion.getBytes(StandardCharsets.UTF_8));
		String encodeToString = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
		log.debug("-> generated hash from clientAssertion: {}", encodeToString);
		return encodeToString;
	}

}
