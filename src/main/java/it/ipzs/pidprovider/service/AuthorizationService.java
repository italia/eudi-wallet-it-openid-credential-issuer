package it.ipzs.pidprovider.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;

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
		if (sessionInfo != null && sessionInfo.getRequestUri().equals(requestUri) && !sessionInfo.isVerified())
			return sessionInfo;
		else
			throw new IllegalArgumentException("Client ID or request uri unknown");
	}

	public SessionInfo checkStateParamAndReturnSessionInfo(String state) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfoByState(state);
		if (sessionInfo != null && !sessionInfo.isVerified()) {
			sessionInfo.setCode(generateCode());
			sessionInfo.setVerified(true);
			sessionUtil.putSessionInfo(sessionInfo);
		}
		else {
			log.debug("sessionInfo {} - state {}", sessionInfo, state);
			throw new IllegalArgumentException("State param unknown");
		}

		return sessionInfo;

	}

	public String generateDirectPostAuthorizeResponse(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {
		return dpJwtUtil.generateCallbackJwtResponse(si, state, issuer);
	}

}
