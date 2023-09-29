package it.ipzs.qeeaissuer.util;

import org.springframework.stereotype.Component;

import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.service.SRService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ResponseCodetUtil {

	private final SRService srService;


	public String generateResponseCode(SessionInfo sessionInfo) {

		// TODO validation
		return srService.generateRandomByByteLength(32);
	}

}
