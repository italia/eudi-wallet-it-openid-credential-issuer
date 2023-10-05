package it.ipzs.qeeaissuer.controller;

import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import it.ipzs.qeeaissuer.dto.CredentialResponse;
import it.ipzs.qeeaissuer.dto.ParResponse;
import it.ipzs.qeeaissuer.dto.ProofRequest;
import it.ipzs.qeeaissuer.dto.TokenResponse;
import it.ipzs.qeeaissuer.model.SessionInfo;
import it.ipzs.qeeaissuer.service.AuthorizationService;
import it.ipzs.qeeaissuer.service.CredentialService;
import it.ipzs.qeeaissuer.service.ParService;
import it.ipzs.qeeaissuer.service.QeeaIssuerService;
import it.ipzs.qeeaissuer.service.TokenService;
import it.ipzs.qeeaissuer.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AuthController {

	private final ParService parService;
	private final TokenService tokenService;
	private final CredentialService credentialService;
	private final AuthorizationService authService;
	private final QeeaIssuerService qeeaIssuerService;

	@Value("${auth-controller.redirect-url}")
	private String redirectUrl;

	@Value("${auth-controller.client-url}")
	private String clientUrl;

	public AuthController(ParService parService, TokenService tokenService, CredentialService credentialService,
			AuthorizationService authService, QeeaIssuerService qeeaIssuerService) {
		this.parService = parService;
		this.tokenService = tokenService;
		this.credentialService = credentialService;
		this.authService = authService;
		this.qeeaIssuerService = qeeaIssuerService;
	}

	@PostMapping(path = "/as/par", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ParResponse> parRequest(@FormParam("response_type") String response_type,
			@FormParam("client_id") String client_id, @FormParam("code_challenge") String code_challenge,
			@FormParam("code_challenge_method") String code_challenge_method,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("request") String request) {

		log.trace("/as/par params: response_type {} - code_challenge_method {} - client_assertion_type {}",
				response_type, code_challenge_method, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> code_challenge {}", code_challenge);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> request {}", request);

		// TODO check validity client assertion
		Object cnf = parService.validateClientAssertionAndRetrieveCnf(client_assertion);
		ParResponse response = parService.generateRequestUri(request, cnf, client_assertion);
		log.trace("par response: {}", response);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/authorize")
	public ResponseEntity<?> authorize(@RequestParam("client_id") String client_id,
			@RequestParam("request_uri") String request_uri) {

		// TODO check client_id/request_uri
		log.trace("/authorize params: client_id {} - request_uri {}", client_id, request_uri);
		String stateParam = authService.retrieveStateParam(client_id, request_uri);
		String uri = redirectUrl.concat("?id=").concat(stateParam);
		log.trace("authorize response: {}", uri);
		String transactionId = authService.generateTransactionIdAndReturnSessionInfo(client_id);
		if (StringUtil.isBlank(transactionId)) {
			log.error("Transaction ID not created!");
		}
		String responseUri = "eudiw://authorize?client_id=".concat(clientUrl).concat("&request_uri=").concat(uri);
		HttpCookie cookie = ResponseCookie.from("transaction_id", transactionId).path("/").build();
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.SET_COOKIE, cookie.toString())
				.location(URI.create(responseUri)).build();
	}

	@GetMapping("/request_uri")
	public ResponseEntity<?> requestUri(@RequestHeader("DPoP") String wiaDpop,
			@RequestHeader("Authorization") String wiaAuth, @RequestParam("id") String id) {
		
		log.trace("-> DPoP {}", wiaDpop);
		log.trace("-> Authorization {}", wiaAuth);
		log.trace("-> id {}", id);
		
		qeeaIssuerService.checkDpop(wiaDpop);
		SessionInfo sessionInfo = qeeaIssuerService.retrieveSesssion(wiaAuth.replace("DPoP ", ""));
		String requestObjectJwt = qeeaIssuerService.generateRequestObjectResponse(sessionInfo);
		if (requestObjectJwt != null) {
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("response", requestObjectJwt);
			return ResponseEntity.ok().body(responseBody);
		}

		else
			return ResponseEntity.internalServerError().build();
	}

	@PostMapping("/callback")
	public ResponseEntity<?> postCallback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {

		// TODO validation encrypted and signed jwt
		String directPostParam = params.get("response");
		log.trace("-> response directPostParam {}", directPostParam);
		qeeaIssuerService.checkAndReadDirectPostResponse(directPostParam);
		String responseCode = qeeaIssuerService.generateResponseCode();
		if (responseCode != null) {
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("status", "OK");
			responseBody.put("response_code", responseCode);
			return ResponseEntity.ok(responseBody);
		} else
			return ResponseEntity.internalServerError().build();
	}

	@GetMapping(path = "/callback", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<?> getCallback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {

		// TODO manage params from eIDAS LoA High callback and redirect to app universal
		// url

		String state = params.get("state");
		SessionInfo si = null;
		try {
			si = authService.checkStateParamAndReturnSessionInfo(state);
		} catch (Exception e) {
			log.error("", e);
			return ResponseEntity.badRequest().build();
		}
		String uri = si.getRedirectUri().concat("?code=").concat(si.getCode()).concat("&state=").concat(state)
				.concat("&iss=https%3A%2F%2Fpid-provider.example.org");
		log.trace("callback response: {}", uri);
		try {
			String generateCallbackResponseJwt = qeeaIssuerService.generateCallbackResponseJwt(si, state, clientUrl);
			return ResponseEntity.status(HttpStatus.OK).body("resonse=".concat(generateCallbackResponseJwt));
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			return ResponseEntity.internalServerError().build();
		}

	}

	@PostMapping(path = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TokenResponse> token(@FormParam("grant_type") String grant_type,
			@FormParam("client_id") String client_id, @FormParam("code") String code,
			@FormParam("code_verifier") String code_verifier,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("redirect_uri") String redirect_uri,
			@RequestHeader("DPoP") String dpop) throws JOSEException, ParseException {

		log.trace("/token params: grant_type {} - client_assertion_type {}", grant_type, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> code_verifier {}", code_verifier);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> redirect_uri {}", redirect_uri);
		log.trace("-> code_verifier {}", code_verifier);
		log.trace("-> code {}", code);
		log.trace("-> DPoP header {}", dpop);

		tokenService.checkDpop(dpop);
		try {
			tokenService.checkParams(client_id, code, code_verifier);
		} catch (Exception e) {
			log.error("", e);
			// TODO return error message code after integration
		}
		TokenResponse response = tokenService.generateTokenResponse(client_id, dpop);
		log.trace("token response: {}", response);
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/credential", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CredentialResponse> credential(
			@FormParam("credential_definition") String credential_definition, @FormParam("format") String format,
			@FormParam("proof") String proof,
			@RequestHeader("DPoP") String dpop, @RequestHeader("Authorization") String authorization)
			throws JOSEException, ParseException {

		log.trace("/credential params: credential_definition {} - format {}", credential_definition, format);
		log.trace("-> proof {}", proof);
		log.trace("-> DPoP header {}", dpop);
		log.trace("-> Authorization header {}", authorization);

		ProofRequest proofReq = null;
		try {
			credentialService.checkDpop(dpop);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		try {
			ObjectMapper om = new ObjectMapper();
			try {
				proofReq = om.readValue(proof, ProofRequest.class);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			credentialService.checkAuthorizationAndProof(authorization, proofReq);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		CredentialResponse response;
		try {
			response = credentialService.generateSdCredentialResponse(dpop, proofReq);
			log.trace("credential response: {}", response);
			return ResponseEntity.ok(response);
		} catch (JOSEException e) {
			log.error("", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (ParseException e) {
			log.error("", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
	}

}
