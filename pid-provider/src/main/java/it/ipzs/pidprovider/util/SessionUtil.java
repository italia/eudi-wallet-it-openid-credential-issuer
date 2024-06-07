package it.ipzs.pidprovider.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import it.ipzs.pidprovider.exception.SessionInfoByStateNotFoundException;
import it.ipzs.pidprovider.model.SessionInfo;


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
				.orElseThrow(() -> new SessionInfoByStateNotFoundException("No state found"));
	}

	public void removeSessionInfo(SessionInfo sessionInfo) {
		sessionMap.remove(sessionInfo.getClientId());
	}
}
