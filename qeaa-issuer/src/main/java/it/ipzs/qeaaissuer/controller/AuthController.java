package it.ipzs.qeaaissuer.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
import it.ipzs.qeaaissuer.dto.CredentialFormat;
import it.ipzs.qeaaissuer.dto.CredentialResponse;
import it.ipzs.qeaaissuer.dto.ParResponse;
import it.ipzs.qeaaissuer.dto.ProofRequest;
import it.ipzs.qeaaissuer.dto.TokenResponse;
import it.ipzs.qeaaissuer.exception.AuthResponseJwtGenerationException;
import it.ipzs.qeaaissuer.exception.MalformedCredentialRequestParamException;
import it.ipzs.qeaaissuer.exception.MissingOrBlankParamException;
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

		log.info("Pushed Authorization Request received: clientId {} - codeChallenge {}", client_id, code_challenge);

		log.trace("/as/par params");
		log.trace("-> response_type {} - code_challenge_method {} - client_assertion_type {}", response_type,
				code_challenge_method, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> code_challenge {}", code_challenge);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> request {}", request);

		validateParams(response_type, client_id, code_challenge, code_challenge_method, client_assertion_type,
				client_assertion, request);
		Object cnf = parService.validateClientAssertionAndRetrieveCnf(client_assertion);
		ParResponse response = parService.generateRequestUri(request, cnf, client_assertion);
		log.trace("par response: {}", response);

		log.info("Pushed Authorization Request accepted: {}", response);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping(path = "/authorize")
	public ModelAndView authorize(@RequestParam("client_id") String client_id,
			@RequestParam("request_uri") String request_uri, HttpServletResponse response) throws Exception {

		log.info("Authorize request received: clientId {} - requestUri {}", client_id, request_uri);

		validateParams(client_id, request_uri);

		ModelAndView mav = new ModelAndView("form_post");
		log.trace("/authorize params");
		log.trace("client_id {} - request_uri {}", client_id, request_uri);

		SessionInfo si = null;
		String responseDirectPost = "";
		String uri = "";
		si = authService.retrieveSessionByClientId(client_id, request_uri);

		uri = redirectUrl.concat("?id=" + si.getState());
		log.trace("authorize response: {}", uri);
		String transactionId = authService.generateTransactionIdAndReturnSessionInfo(client_id);
		if (StringUtil.isBlank(transactionId)) {
			log.error("Transaction ID not created!");
		}
		try {
			responseDirectPost = qeaaIssuerService.generateAuthResponseJwt(client_id, uri);
		} catch (JOSEException | ParseException e) {
			log.error("Exception in authorize jwt response generation", e);
			throw new AuthResponseJwtGenerationException(e);
		}
		log.trace("/authorize response: {}", responseDirectPost);
		mav.addObject("response", responseDirectPost);
		mav.addObject("clientUri", si.getRedirectUri());
		response.addCookie(new Cookie("transaction_id", transactionId));

		log.info("Authorize request accepted: transactionId {}", transactionId);
		return mav;
	}

	@GetMapping("/request_uri")
	public ResponseEntity<?> requestUri(@RequestHeader("DPoP") String wiaDpop,
			@RequestHeader("Authorization") String wiaAuth, @RequestParam("id") String id) {
		log.trace("/request_uri params");
		log.trace("-> DPoP {}", wiaDpop);
		log.trace("-> Authorization {}", wiaAuth);
		log.trace("-> id {}", id);

		validateParams(id);
		validateHeaders(wiaAuth, wiaDpop);

		log.info("Request uri : id {}", id);

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
			log.info("--> /request_uri - success");
			return ResponseEntity.ok().body(responseBody);
		} else {
			log.error("--> requestobject is null");
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/callback")
	public ResponseEntity<?> postCallback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {

		log.trace("/postCallback params:");
		String directPostParam = params.get("response");
		log.trace("response {}", directPostParam);

		validateParams(directPostParam);

		log.info("Post Callback request received");
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
			log.info("Post callback request accepted");
			return ResponseEntity.ok(responseBody);
		} else {
			log.error("Post callback response code not generated");
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

		log.info("Token request: client_id {}", client_id);

		validateParams(grant_type, client_id, code, code_verifier, client_assertion, client_assertion_type,
				redirect_uri);
		validateHeaders(dpop);
		tokenService.validateClientAssertion(client_assertion_type, client_assertion);
		tokenService.checkDpop(dpop);
		tokenService.checkParams(client_id, code, code_verifier);
		
		TokenResponse response = tokenService.generateTokenResponse(client_id, dpop);
		log.trace("token response: {}", response);
		log.info("--> /token - success");
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/credential", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CredentialResponse> credential(
			@FormParam("credential_definition") String credential_definition, @FormParam("format") String format,
			@FormParam("proof") String proof, @RequestHeader("DPoP") String dpop,
			@RequestHeader("Authorization") String authorization) throws JOSEException, ParseException {

		log.trace("/credential params: ");
		log.trace("-> credential_definition {} - format {}", credential_definition, format);
		log.trace("-> proof {}", proof);
		log.trace("-> DPoP header {}", dpop);
		log.trace("-> Authorization header {}", authorization);

		log.info("Credential request received: credential_definition {} - format {}", credential_definition, format);

		validateParams(credential_definition, format, proof);
		validateHeaders(dpop, authorization);

		ProofRequest proofReq = null;
		CredentialDefinitionDto credDefinition = null;
		credentialService.checkDpop(dpop);

		ObjectMapper om = new ObjectMapper();
		try {
			proofReq = om.readValue(proof, ProofRequest.class);
			credDefinition = om.readValue(credential_definition, CredentialDefinitionDto.class);
			credentialService.validateProofRequest(proofReq, dpop);
		} catch (JsonProcessingException e) {
			log.error("proof or credential_definition malformed", e);
			throw new MalformedCredentialRequestParamException("proof or credential_definition malformed");
		}
		credentialService.checkAuthorizationAndProof(authorization, proofReq);

		CredentialResponse response;
		try {
			if (CredentialFormat.SD_JWT.value().equals(format)) {
				response = credentialService.generateSdCredentialResponse(dpop, proofReq, credDefinition);
			} else {
				response = credentialService.generateMdocCborCredentialResponse(dpop, proofReq, credDefinition);
			}

			log.trace("credential response: {}", response);
			log.info("--> /credential - success");
			return ResponseEntity.ok(response);
		} catch (JOSEException e) {
			log.error("Error in credential generation", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (ParseException e) {
			log.error("Error in credential generation", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

	}

	private void validateParams(String... params) {
		if (Stream.of(params).anyMatch(Objects::isNull) || Stream.of(params).anyMatch(String::isBlank)) {
			log.error("Missing or blank param");
			throw new MissingOrBlankParamException("Missing or blank param");
		}
	}

	private void validateHeaders(String... headers) {
		if (Stream.of(headers).anyMatch(Objects::isNull) || Stream.of(headers).anyMatch(String::isBlank)) {
			log.error("Missing or blank header");
			throw new MissingOrBlankParamException("Missing or blank header");
		}
	}
}
