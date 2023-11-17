package it.ipzs.qeaaissuer.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import it.ipzs.qeaaissuer.dto.CredentialDefinitionDto;
import it.ipzs.qeaaissuer.dto.CredentialResponse;
import it.ipzs.qeaaissuer.dto.ParResponse;
import it.ipzs.qeaaissuer.dto.ProofRequest;
import it.ipzs.qeaaissuer.dto.TokenResponse;
import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.service.AuthorizationService;
import it.ipzs.qeaaissuer.service.CredentialService;
import it.ipzs.qeaaissuer.service.ParService;
import it.ipzs.qeaaissuer.service.QeaaIssuerService;
import it.ipzs.qeaaissuer.service.TokenService;
import it.ipzs.qeaaissuer.util.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.FormParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

	private final ParService parService;
	private final TokenService tokenService;
	private final CredentialService credentialService;
	private final AuthorizationService authService;
	private final QeaaIssuerService qeaaIssuerService;

	@Value("${auth-controller.redirect-url}")
	private String redirectUrl;

	@Value("${auth-controller.client-url}")
	private String clientUrl;

	@PostMapping(path = "/as/par", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ParResponse> parRequest(@FormParam("response_type") String response_type,
			@FormParam("client_id") String client_id, @FormParam("code_challenge") String code_challenge,
			@FormParam("code_challenge_method") String code_challenge_method,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("request") String request) {

		log.trace("/as/par params");
		log.trace("-> response_type {} - code_challenge_method {} - client_assertion_type {}",
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

	@GetMapping(path = "/authorize")
	public ModelAndView authorize(@RequestParam("client_id") String client_id,
			@RequestParam("request_uri") String request_uri, HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView("form_post");
		log.trace("/authorize params");
		log.trace("client_id {} - request_uri {}", client_id, request_uri);

		SessionInfo si = null;
		String responseDirectPost = "";
		String uri = "";
		try {
			si = authService.retrieveSessionByClientId(client_id, request_uri);

			uri = redirectUrl.concat("?id=").concat(si.getState());
			log.trace("authorize response: {}", uri);
		} catch (Exception e) {
			log.error("", e);
			throw e;
		}
		String transactionId = authService.generateTransactionIdAndReturnSessionInfo(client_id);
		if (StringUtil.isBlank(transactionId)) {
			log.error("Transaction ID not created!");
		}
		try {
			responseDirectPost = qeaaIssuerService.generateAuthResponseJwt(client_id, uri);
		} catch (JOSEException | ParseException e) {
			log.error("", e);
			throw e;
		}
		log.trace("/authorize response: {}", responseDirectPost);
		mav.addObject("response", responseDirectPost);
		mav.addObject("clientUri", si.getRedirectUri());
		response.addCookie(new Cookie("transaction_id", transactionId));

		return mav;
	}

	@GetMapping("/request_uri")
	public ResponseEntity<?> requestUri(@RequestHeader("DPoP") String wiaDpop,
			@RequestHeader("Authorization") String wiaAuth, @RequestParam("id") String id) {
		log.trace("/request_uri params");
		log.trace("-> DPoP {}", wiaDpop);
		log.trace("-> Authorization {}", wiaAuth);
		log.trace("-> id {}", id);
		
		String clientAssertion = wiaAuth.replace("DPoP ", "");
		qeaaIssuerService.checkDpop(wiaDpop, clientAssertion);
		SessionInfo sessionInfo = qeaaIssuerService.retrieveSessionAndCheckWia(clientAssertion);
		if (sessionInfo == null) {
			log.error("--> /request_uri - no session found");
			return ResponseEntity.badRequest().build();
		}
		String requestObjectJwt = qeaaIssuerService.generateRequestObjectResponse(sessionInfo);
		if (requestObjectJwt != null) {
			log.trace("--> requestobject response: {}", requestObjectJwt);
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

		log.trace("/postCallback params:");
		String directPostParam = params.get("response");
		log.trace("response {}", directPostParam);
		SessionInfo sessionInfo = null;
		try {
			sessionInfo = qeaaIssuerService.checkAndReadDirectPostResponse(directPostParam);
		} catch (Exception e) {
			log.error("", e);
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("message", e.getMessage());
			return ResponseEntity.internalServerError().body(responseBody);
		}
		String responseCode = qeaaIssuerService.generateResponseCode(sessionInfo);
		if (responseCode != null) {
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("status", "OK");
			responseBody.put("response_code", responseCode);
			log.trace("postCallback response {}", responseBody);
			return ResponseEntity.ok(responseBody);
		} else
			return ResponseEntity.internalServerError().build();
	}

	@GetMapping(path = "/callback", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<?> getCallback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {

		// TODO manage params from eIDAS LoA High callback and redirect to app universal
		// url
		log.trace("/getCallback params:");
		String state = params.get("state");
		log.trace("state {}", state);
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
			String generateCallbackResponseJwt = qeaaIssuerService.generateCallbackResponseJwt(si, state, clientUrl);

			log.trace("-> getCallback response JWT: {}", generateCallbackResponseJwt);
			return ResponseEntity.status(HttpStatus.OK).body("response=".concat(generateCallbackResponseJwt));
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

		log.trace("/token params: ");
		log.trace("-> grant_type {} - client_assertion_type {}", grant_type, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> code_verifier {}", code_verifier);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> redirect_uri {}", redirect_uri);
		log.trace("-> code {}", code);
		log.trace("-> DPoP header {}", dpop);

		tokenService.checkDpop(dpop);
		try {
			tokenService.checkParams(client_id, code, code_verifier);
		} catch (Exception e) {
			log.error("", e);
//			return ResponseEntity.badRequest().build();
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

		log.trace("/credential params: ");
		log.trace("-> credential_definition {} - format {}", credential_definition, format);
		log.trace("-> proof {}", proof);
		log.trace("-> DPoP header {}", dpop);
		log.trace("-> Authorization header {}", authorization);

		ProofRequest proofReq = null;
		CredentialDefinitionDto credDefinition = null;
		try {
			credentialService.checkDpop(dpop);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		ObjectMapper om = new ObjectMapper();
		try {
			proofReq = om.readValue(proof, ProofRequest.class);
			credDefinition = om.readValue(credential_definition, CredentialDefinitionDto.class);
		} catch (JsonProcessingException e) {
			log.error("", e);
		}
		try {
			credentialService.checkAuthorizationAndProof(authorization, proofReq);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		CredentialResponse response;
		try {
			if ("vc+sd-jwt".equals(format)) {
				response = credentialService.generateSdCredentialResponse(dpop, proofReq, credDefinition);
			} else {
				response = credentialService.generateMdocCborCredentialResponse(dpop, proofReq, credDefinition);
			}

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
