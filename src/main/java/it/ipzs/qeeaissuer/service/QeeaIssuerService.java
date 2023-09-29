package it.ipzs.qeeaissuer.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;

import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.util.CallbackResponseJwtUtil;
import it.ipzs.qeeaissuer.util.ResponseCodetUtil;
import it.ipzs.qeeaissuer.util.ResponseObjectUtil;
import it.ipzs.qeeaissuer.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QeeaIssuerService {

	private final ResponseObjectUtil responseObjectUtil;
	private final SessionUtil sessionUtil;
	private final ResponseCodetUtil responseCodetUtil;
	private final CallbackResponseJwtUtil callbackResponseJwtUtil;

	public QeeaIssuerService(ResponseObjectUtil responseObjectUtil, ResponseCodetUtil responseCodetUtil,
			CallbackResponseJwtUtil callbackResponseJwtUtil,
			SessionUtil sessionUtil) {
		super();
		this.responseObjectUtil = responseObjectUtil;
		this.responseCodetUtil = responseCodetUtil;
		this.callbackResponseJwtUtil = callbackResponseJwtUtil;
		this.sessionUtil = sessionUtil;
	}

	public String generateRequestObjectResponse(SessionInfo si) {
		log.debug("generating response object...");

		// TODO implementation
		try {
			return responseObjectUtil.generateResponseObject(si);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return null;
		}
	}

	public String generateResponseCode() {
		log.debug("generating response code...");

		// TODO implementation
		return responseCodetUtil.generateResponseCode(null);
	}

	public String generateCallbackResponseJwt(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {
		return callbackResponseJwtUtil.generateDirectPostResponse(si, state, issuer);
	}

	public SessionInfo retrieveSesssion(String hashedWia) {
		try {
			return sessionUtil.getSessionInfoByHashedWia(hashedWia);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

}
