package it.ipzs.qeeaissuer.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import it.ipzs.qeeaissuer.model.SessionInfo;


@Component
public class SessionUtil {

	private ConcurrentMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

	public void putSessionInfo(SessionInfo sessionInfo) {

		sessionMap.put(sessionInfo.getClientId(), sessionInfo);
	}

	public SessionInfo getSessionInfo(String key) {
		return sessionMap.get(key);
	}

	public SessionInfo getSessionInfoByState(String state) {
		return sessionMap.values().stream().filter(si -> si.getState().equals(state)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No state found"));
	}

	public void removeSessionInfo(SessionInfo sessionInfo) {
		sessionMap.remove(sessionInfo.getClientId());
	}

	public SessionInfo getSessionInfoByHashedWia(String hashedWia) {
		return sessionMap.values().stream().filter(si -> si.getHashedWia().equals(hashedWia)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No hashed Wallet Instance Attestation found"));
	}
}
