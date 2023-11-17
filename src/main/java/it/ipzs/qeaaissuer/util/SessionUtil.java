package it.ipzs.qeaaissuer.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import it.ipzs.qeaaissuer.model.SessionInfo;


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

	public SessionInfo getSessionInfoByWia(String wia) {
		return sessionMap.values().stream().filter(si -> wia.equals(si.getWalletInstanceAttestation())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No Wallet Instance Attestation found"));
	}
}
