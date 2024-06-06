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
import it.ipzs.qeaaissuer.exception.AudClaimValidationException;
import it.ipzs.qeaaissuer.exception.HashedWalletInstanceAttestationGenerationException;
import it.ipzs.qeaaissuer.exception.HashedWiaNotMatchException;
import it.ipzs.qeaaissuer.exception.InvalidAthException;
import it.ipzs.qeaaissuer.exception.InvalidHtmAndHtuClaimsException;
import it.ipzs.qeaaissuer.exception.ParseDirectPostParamException;
import it.ipzs.qeaaissuer.exception.PresentationNonceValidationException;
import it.ipzs.qeaaissuer.exception.RequestUriDpopValidationException;
import it.ipzs.qeaaissuer.exception.ResponseUriJwtGenerationException;
import it.ipzs.qeaaissuer.exception.SessionInfoByWiaNotFoundException;
import it.ipzs.qeaaissuer.exception.VpClaimValidationException;
import it.ipzs.qeaaissuer.exception.VpTokenNonceValidationException;
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
			log.error("Error in response uri jwt generation", e);
			throw new ResponseUriJwtGenerationException(e.getMessage());
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
				throw new SessionInfoByWiaNotFoundException("Session not found by wallet instance attestation");
			}
			String hashedWia = sessionInfoByWia.getHashedWia();
			String generatedHashedWia = generateHashedWia(wia);
			if (!generatedHashedWia.equals(hashedWia)) {
				log.error("Hashed wallet instance attestation doesn't match");
				log.trace("wia in session {}", hashedWia, " - hashed wia generated {}", generatedHashedWia);
				throw new HashedWiaNotMatchException("Hashed wallet instance attestation doesn't match");
			}
			return sessionInfoByWia;
		} catch (NoSuchAlgorithmException e) {
			log.error("Error in wallet instance attestation hash generation", e);
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
				throw new InvalidHtmAndHtuClaimsException("Invalid claims: " + htmClaim + " - " + htuClaim);
			}
			String generateHashedWia = generateHashedWia(wiaAuth);
			if (ath == null) {
				log.error("ath claim is null");
				throw new InvalidAthException("Missing ath claim");
			}
			if (!generateHashedWia.equals(ath)) {
				log.debug("ath {} - hashed generated {}", ath, generateHashedWia);
				log.debug("wia {}", wiaAuth);
				log.error("invalid ath claim");
				throw new InvalidAthException("Invalid ath claim");
			}
		} catch (ParseException | JOSEException e) {
			log.error("request uri dpop validation failed", e);
			throw new RequestUriDpopValidationException(e);
		} catch (NoSuchAlgorithmException e) {
			log.error("hashed wallet instance attestation generation failed", e);
			throw new HashedWalletInstanceAttestationGenerationException(e);
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
			log.debug("session found: {}", sessionInfoByState);
			log.info("Pid credential verified and stored");
			return sessionInfoByState;
		} catch (JOSEException | ParseException | NoSuchAlgorithmException e) {
			log.error("", e);
			throw new ParseDirectPostParamException("Error during direct post param parsing");
		}

	}

	private void verifyPidCredentialsAndStorePidClaims(JWTClaimsSet vpTokenClaims, SessionInfo si)
			throws ParseException, JOSEException {
		String vpClaim = vpTokenClaims.getStringClaim("vp");
		if (vpClaim == null) {
			log.error("vp claim validation in vp_token failed - vp claim null");
			throw new VpClaimValidationException("vp claim validation in vp_token failed - vp claim is null");
		}
		pidService.validatePidCredential(vpClaim);
		Map<String, Object> pidCredentialInfo = pidService.extractPidCredentialInfo(vpClaim);
		si.setPidCredentialClaims(pidCredentialInfo);
		sessionUtil.putSessionInfo(si);

	}

	private void verifyVpTokenClaims(JWTClaimsSet vpTokenClaims, SessionInfo si) throws ParseException {
		Object audClaimObj = vpTokenClaims.getClaim("aud");
		if (audClaimObj instanceof Collection<?>) {
			List<String> audClaimList = ((Collection<?>) audClaimObj).stream().filter(String.class::isInstance)
					.map(String.class::cast).toList();

			if (!audClaimList.contains(audUriClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaimList);
				throw new AudClaimValidationException("aud claim validation in vp_token failed");
			}
			String nonceClaim = vpTokenClaims.getStringClaim("nonce");
			if (!si.getRequestUriNonce().equals(nonceClaim)) {
				log.error("nonce claim validation in vp_token failed - nonce expected {} - nonce received {}", si.getRequestUriNonce(),
						nonceClaim);
				throw new VpTokenNonceValidationException("nonce claim validation in vp_token failed");
			}
		} else {
			String audClaim = (String) audClaimObj;
			log.info("aud claim {}", audClaim);
			if (!audUriClaim.equals(audClaim)) {
				log.error("aud claim validation in vp_token failed - aud expected {} - aud received {}", audUriClaim,
						audClaim);
				throw new AudClaimValidationException("aud claim validation in vp_token failed");
			}
			String nonceClaim = vpTokenClaims.getStringClaim("nonce");
			if (!si.getRequestUriNonce().equals(nonceClaim)) {
				log.error("nonce claim validation in vp_token failed - nonce expected {} - nonce received {}", si.getRequestUriNonce(),
						nonceClaim);
				throw new VpTokenNonceValidationException("nonce claim validation in vp_token failed");
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
			throw new PresentationNonceValidationException("Nonce validation failed in authorization callback");
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
