package it.ipzs.qeaaissuer.util;

import org.springframework.stereotype.Component;

import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.service.SRService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ResponseCodetUtil {

	private final SRService srService;
	private final SessionUtil sessionUtil;


	public String generateResponseCode(SessionInfo sessionInfo) {
		String responseCode = srService.generateRandomByByteLength(32);
		sessionInfo.setResponseCode(responseCode);
		sessionUtil.putSessionInfo(sessionInfo);
		return responseCode;
	}

}
