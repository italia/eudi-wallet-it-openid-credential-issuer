package it.ipzs.qeaaissuer.service;

import org.springframework.stereotype.Service;

import it.ipzs.qeaaissuer.exception.AuthorizationRequestValidationException;
import it.ipzs.qeaaissuer.exception.SessionInfoByClientIdNotFoundException;
import it.ipzs.qeaaissuer.exception.SessionInfoByStateNotFoundException;
import it.ipzs.qeaaissuer.exception.SessionRequestUriNotMatchedException;
import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

	private final SRService srService;
	private final SessionUtil sessionUtil;

	public String generateCode() {
		return srService.generateRandomByByteLength(32);
	}

	public String generateTransactionId() {
		return srService.generateRandomByByteLength(48);
	}

	public String retrieveStateParam(String clientId, String requestUri) {
		log.debug("clientId {} - requestUri {}", clientId, requestUri);
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		log.debug("sessionInfo {}", sessionInfo);
		if (sessionInfo != null) {
			if (sessionInfo.getRequestUri().equals(requestUri) && !sessionInfo.isVerified()) {
				return sessionInfo.getState();
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

	public SessionInfo retrieveSessionByClientId(String clientId, String requestUri) {
		log.debug("clientId {} - requestUri {}", clientId, requestUri);
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		log.debug("sessionInfo {}", sessionInfo);
		if (sessionInfo == null) {
			log.error("! client id unknown, no session found");
			throw new SessionInfoByClientIdNotFoundException("Client ID unknown");
		}
		if (sessionInfo.getRequestUri().equals(requestUri) && !sessionInfo.isVerified())
			return sessionInfo;
		else
			throw new SessionRequestUriNotMatchedException("Request URI unknown");
	}

	public SessionInfo checkStateParamAndReturnSessionInfo(String state) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfoByState(state);
		if (sessionInfo != null && !sessionInfo.isVerified()) {
			sessionInfo.setCode(generateCode());
			sessionInfo.setVerified(true);
			sessionUtil.putSessionInfo(sessionInfo);
		} else {
			log.debug("sessionInfo {} - state {}", sessionInfo, state);
			throw new SessionInfoByStateNotFoundException("State param unknown");
		}

		return sessionInfo;

	}

	public String generateTransactionIdAndReturnSessionInfo(String clientId) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		if (sessionInfo != null && !sessionInfo.isVerified()) {
			sessionInfo.setTransactionId(generateTransactionId());
			sessionUtil.putSessionInfo(sessionInfo);
		} else {
			log.debug("sessionInfo {} - clientId {}", sessionInfo, clientId);
			throw new SessionInfoByClientIdNotFoundException("clientId param unknown");
		}

		return sessionInfo.getTransactionId();

	}
}
