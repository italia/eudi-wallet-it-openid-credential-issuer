package it.ipzs.qeeaissuer.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.util.CallbackResponseJwtUtil;
import it.ipzs.qeeaissuer.util.DpopUtil;
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
	private final DpopUtil dpopUtil;

	public QeeaIssuerService(ResponseObjectUtil responseObjectUtil, ResponseCodetUtil responseCodetUtil,
			CallbackResponseJwtUtil callbackResponseJwtUtil,
			SessionUtil sessionUtil, DpopUtil dpopUtil) {
		super();
		this.responseObjectUtil = responseObjectUtil;
		this.responseCodetUtil = responseCodetUtil;
		this.callbackResponseJwtUtil = callbackResponseJwtUtil;
		this.sessionUtil = sessionUtil;
		this.dpopUtil = dpopUtil;
	}

	public String generateRequestObjectResponse(SessionInfo si) {
		log.debug("generating response object...");
		try {
			return responseObjectUtil.generateResponseObject(si);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return null;
		}
	}

	public String generateResponseCode() {
		log.debug("generating response code...");

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

	public void checkDpop(String wiaDpop) {
		try {
			JWTClaimsSet claimsSet = dpopUtil.parse(wiaDpop);
			String htuClaim = dpopUtil.getHtuClaim(claimsSet);
			String htmClaim = dpopUtil.getHtmClaim(claimsSet);

			if (!htuClaim.endsWith("/request_uri") || !htmClaim.equals("GET")) {
				throw new IllegalArgumentException("Invalid claims: " + htmClaim + " - " + htuClaim);
			}
		} catch (ParseException | JOSEException e) {
			log.error("", e);
			throw new RuntimeException("", e);
		}

	}

	public void checkAndReadDirectPostResponse(String directPostParam) {
		// TODO Auto-generated method stub

	}

}
