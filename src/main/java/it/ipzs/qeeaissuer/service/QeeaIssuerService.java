package it.ipzs.qeeaissuer.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;

import it.ipzs.qeeaissuer.util.ResponseCodetUtil;
import it.ipzs.qeeaissuer.util.ResponseObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QeeaIssuerService {

	private final ResponseObjectUtil responseObjectUtil;

	private final ResponseCodetUtil responseCodetUtil;

	public QeeaIssuerService(ResponseObjectUtil responseObjectUtil, ResponseCodetUtil responseCodetUtil) {
		super();
		this.responseObjectUtil = responseObjectUtil;
		this.responseCodetUtil = responseCodetUtil;
	}

	public String generateRequestObjectResponse() {
		log.debug("generating response object...");

		// TODO implementation
		try {
			return responseObjectUtil.generateResponseObject(null);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return null;
		}
	}

	public String generateResponseCode() {
		log.debug("generating response code...");

		// TODO implementation
		try {
			return responseCodetUtil.generateResponseCode(null);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return null;
		}
	}
}
