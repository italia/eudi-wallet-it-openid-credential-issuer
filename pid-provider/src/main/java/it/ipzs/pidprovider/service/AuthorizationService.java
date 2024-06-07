package it.ipzs.pidprovider.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;

import it.ipzs.pidprovider.exception.AuthorizationRequestValidationException;
import it.ipzs.pidprovider.exception.CallbackRequestValidationException;
import it.ipzs.pidprovider.exception.SessionInfoByClientIdNotFoundException;
import it.ipzs.pidprovider.exception.SessionInfoByStateNotFoundException;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.util.CallbackJwtUtil;
import it.ipzs.pidprovider.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

	private final SRService srService;
	private final SessionUtil sessionUtil;
	private final CallbackJwtUtil dpJwtUtil;

	public String generateCode() {
		return srService.generateRandomByByteLength(32);
	}

	public SessionInfo retrieveStateParam(String clientId, String requestUri) {
		log.debug("clientId {} - requestUri {}", clientId, requestUri);
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		log.debug("sessionInfo {}", sessionInfo);
		if (sessionInfo != null) {
			if (sessionInfo.getRequestUri().equals(requestUri) && !sessionInfo.isVerified()) {
				return sessionInfo;
			} else {
				log.error(
						"Authorization request parameter validation exception: reqeustUri {} - sessioInfo isVerified {}",
						requestUri, sessionInfo.isVerified());
				throw new AuthorizationRequestValidationException(
						"Authorization request parameter validation exception");
			}
		} else {
			log.error("Client id unknown");
			throw new SessionInfoByClientIdNotFoundException("Client ID or request uri unknown");
		}
	}

	public SessionInfo checkStateParamAndReturnSessionInfo(String state) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfoByState(state);
		if (sessionInfo != null) {
			if (!sessionInfo.isVerified()) {
				sessionInfo.setCode(generateCode());
				sessionInfo.setVerified(true);
				sessionUtil.putSessionInfo(sessionInfo);

			} else {
				log.error("Callback request parameter validation exception: sessionInfo isVerified {}", sessionInfo.isVerified());
				throw new CallbackRequestValidationException("Callback request parameter validation exception");
			}
		} else {
			log.error("sessionInfo {} - state {}", sessionInfo, state);
			throw new SessionInfoByStateNotFoundException("State param unknown");

		}

		return sessionInfo;

	}

	public String generateDirectPostAuthorizeResponse(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {
		return dpJwtUtil.generateCallbackJwtResponse(si, state, issuer);
	}
}
