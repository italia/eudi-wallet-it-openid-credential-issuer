package it.ipzs.qeaaissuer.service;

import org.springframework.stereotype.Service;

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
		if (sessionInfo != null && sessionInfo.getRequestUri().equals(requestUri) && !sessionInfo.isVerified())
			return sessionInfo.getState();
		else
			throw new IllegalArgumentException("Client ID or request uri unknown");
	}

	public SessionInfo retrieveSessionByClientId(String clientId, String requestUri) {
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

	public String generateTransactionIdAndReturnSessionInfo(String clientId) {
		SessionInfo sessionInfo = sessionUtil.getSessionInfo(clientId);
		if (sessionInfo != null && !sessionInfo.isVerified()) {
			sessionInfo.setTransactionId(generateTransactionId());
			sessionUtil.putSessionInfo(sessionInfo);
		} else {
			log.debug("sessionInfo {} - clientId {}", sessionInfo, clientId);
			throw new IllegalArgumentException("clientId param unknown");
		}

		return sessionInfo.getTransactionId();

	}
}
